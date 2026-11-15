#!/usr/bin/env python3
"""Analiza una APK/APKM de Facemoji y comprueba las huellas (fingerprints) de los parches.

No necesita Java ni apktool: lee el ZIP y el grupo de cadenas de los ficheros DEX.
Si tienes `androguard` instalado (`pip install androguard`) las comprobaciones son
exactas (clase + metodo); si no, se hace una comprobacion por cadenas del DEX.

Ejemplos de uso:

    # Resumen de SDK de anuncios / analitica detectados
    python3 tools/analizar_apk.py Facemoji.apkm --informe

    # Comprueba si las huellas de los parches existen en esta version
    python3 tools/analizar_apk.py Facemoji.apkm --comprobar

    # Busca cadenas relacionadas con la suscripcion VIP / premium
    python3 tools/analizar_apk.py Facemoji.apkm --buscar "vip|premium|subscribe"

    # Lista clases de un paquete (requiere androguard)
    python3 tools/analizar_apk.py Facemoji.apkm --clases com/simejikeyboard
"""

from __future__ import annotations

import argparse
import re
import struct
import sys
import zipfile
from pathlib import Path

# --------------------------------------------------------------------------------------
# Huellas que usan los parches: (clase, metodo) que se espera encontrar en el DEX.
# Mantener sincronizado con patches/src/main/kotlin/app/parchefacemoji/patches/facemoji/*/
# --------------------------------------------------------------------------------------
HUELLAS = {
    "Eliminar anuncios": [
        ("Lcom/google/android/gms/ads/BaseAdView;", "loadAd"),
        ("Lcom/google/android/gms/ads/AdLoader;", "loadAd"),
        ("Lcom/google/android/gms/ads/interstitial/InterstitialAd;", "show"),
        ("Lcom/google/android/gms/ads/appopen/AppOpenAd;", "show"),
        ("Lcom/google/android/gms/ads/rewarded/RewardedAd;", "show"),
        ("Lcom/google/android/gms/ads/rewardedinterstitial/RewardedInterstitialAd;", "show"),
        ("Lcom/facebook/ads/AdView;", "loadAd"),
        ("Lcom/facebook/ads/InterstitialAd;", "show"),
        ("Lcom/unity3d/ads/UnityAds;", "show"),
        ("Lcom/applovin/adview/AppLovinAdView;", "loadNextAd"),
    ],
    "Desactivar rastreo": [
        ("Lcom/google/firebase/analytics/FirebaseAnalytics;", "logEvent"),
        ("Lcom/google/firebase/analytics/FirebaseAnalytics;", "setUserProperty"),
        ("Lcom/google/firebase/analytics/FirebaseAnalytics;", "setUserId"),
        ("Lcom/google/firebase/analytics/FirebaseAnalytics;", "setDefaultEventParameters"),
        ("Lcom/google/firebase/crashlytics/FirebaseCrashlytics;", "log"),
        ("Lcom/google/firebase/crashlytics/FirebaseCrashlytics;", "setUserId"),
        ("Lcom/google/firebase/crashlytics/FirebaseCrashlytics;", "recordException"),
        ("Lcom/facebook/appevents/AppEventsLogger;", "logEvent"),
        ("Lcom/appsflyer/AppsFlyerLib;", "logEvent"),
    ],
}

# Marcadores para el resumen de SDK.
MARCADORES = {
    "Google AdMob": ["Lcom/google/android/gms/ads/"],
    "Facebook Audience Network": ["Lcom/facebook/ads/"],
    "Facebook Core (eventos)": ["Lcom/facebook/appevents/"],
    "Unity Ads": ["Lcom/unity3d/ads/"],
    "AppLovin": ["Lcom/applovin/"],
    "Pangle / CSJ (ByteDance)": ["Lcom/bytedance/sdk/openadsdk/", "Lcom/pangle/"],
    "ironSource / LevelPlay": ["Lcom/ironsource/"],
    "Mintegral": ["Lcom/mbridge/msdk/"],
    "Vungle / Liftoff": ["Lcom/vungle/"],
    "Firebase Analytics": ["Lcom/google/firebase/analytics/"],
    "Firebase Crashlytics": ["Lcom/google/firebase/crashlytics/"],
    "AppsFlyer": ["Lcom/appsflyer/"],
    "Adjust": ["Lcom/adjust/sdk/"],
    "Google UMP (consentimiento)": ["Lcom/google/android/ump/"],
}


# --------------------------------------------------------------------------------------
# Lectura de la APK
# --------------------------------------------------------------------------------------
def dex_blobs(path: Path) -> dict[str, bytes]:
    """Devuelve {nombre: bytes} de todos los DEX, entrando en APKs si es un APKM/XAPK."""
    blobs: dict[str, bytes] = {}

    with zipfile.ZipFile(path) as zf:
        for info in zf.infolist():
            if info.filename.endswith(".dex"):
                blobs[info.filename] = zf.read(info)
            elif info.filename.endswith(".apk"):
                import io

                with zipfile.ZipFile(io.BytesIO(zf.read(info))) as inner:
                    for inner_info in inner.infolist():
                        if inner_info.filename.endswith(".dex"):
                            name = f"{info.filename}:{inner_info.filename}"
                            blobs[name] = inner.read(inner_info)

    if not blobs:
        sys.exit(f"No se ha encontrado ningun DEX dentro de {path}")
    return blobs


def _uleb128(data: bytes, offset: int) -> tuple[int, int]:
    result = 0
    shift = 0
    while True:
        byte = data[offset]
        offset += 1
        result |= (byte & 0x7F) << shift
        if not byte & 0x80:
            return result, offset
        shift += 7


def dex_strings(data: bytes):
    """Itera el grupo de cadenas (string_ids) de un DEX."""
    if len(data) < 0x70 or not data.startswith(b"dex\n"):
        return
    string_ids_size, string_ids_off = struct.unpack_from("<II", data, 0x38)
    for index in range(string_ids_size):
        string_off_loc = string_ids_off + 4 * index
        if string_off_loc + 4 > len(data):
            return
        string_off = struct.unpack_from("<I", data, string_off_loc)[0]
        _, start = _uleb128(data, string_off)
        end = data.find(b"\x00", start)
        if end < 0:
            continue
        yield data[start:end].decode("utf-8", "replace")


def cargar_androguard():
    try:
        from androguard.core.dex import DEX  # type: ignore
    except Exception:
        return None
    return DEX


# --------------------------------------------------------------------------------------
# Comprobaciones
# --------------------------------------------------------------------------------------
def informe(cadenas: set[str]) -> None:
    print("SDK detectados (busqueda por cadenas del DEX):\n")
    for nombre, marcadores in MARCADORES.items():
        encontrados = sum(1 for c in cadenas if any(c.startswith(m) for m in marcadores))
        if encontrados:
            print(f"  [x] {nombre}: {encontrados} referencias")
        else:
            print(f"  [ ] {nombre}: no encontrado")
    print(
        "\nNota: los marcadores son descriptores de tipo (Lpaquete/Clase;), por eso la "
        "busqueda es fiable aunque el DEX este ofuscado en el codigo propio de la app."
    )


def comprobar(cadenas: set[str], dex_por_nombre: dict[str, bytes]) -> int:
    """Comprueba las huellas. Devuelve el numero de parches que no encuentran ninguna."""
    DEX = cargar_androguard()
    exacto = DEX is not None
    print(f"Modo de comprobacion: {'exacto (androguard)' if exacto else 'por cadenas del DEX'}\n")

    metodos: set[tuple[str, str]] = set()
    if exacto:
        for nombre, blob in dex_por_nombre.items():
            try:
                dex = DEX(blob)
            except Exception as exc:  # DEX corrupto o comprimido
                print(f"  ! no se pudo leer {nombre}: {exc}")
                continue
            for clase in dex.get_classes():
                clase_desc = clase.get_name()  # formato Lpaquete/Clase;
                for metodo in clase.get_methods():
                    metodos.add((clase_desc, metodo.get_name()))

    parches_sin_huella = 0
    for parche, huellas in HUELLAS.items():
        print(f"=== {parche} ===")
        encontradas = 0
        for clase, metodo in huellas:
            if exacto:
                ok = (clase, metodo) in metodos
            else:
                ok = clase in cadenas and metodo in cadenas
            encontradas += ok
            print(f"  {'[x]' if ok else '[ ]'} {clase}->{metodo}")
        if encontradas == 0:
            parches_sin_huella += 1
            print("  => Ninguna huella coincide: este parche fallaria o no haria nada.")
        else:
            print(f"  => {encontradas}/{len(huellas)} huellas disponibles.")
        print()

    return parches_sin_huella


def buscar(cadenas: set[str], patron: str) -> None:
    try:
        regex = re.compile(patron, re.IGNORECASE)
    except re.error as exc:
        sys.exit(f"Expresion regular no valida: {exc}")

    coincidencias = sorted(c for c in cadenas if regex.search(c))
    print(f"{len(coincidencias)} coincidencias:\n")
    for cadena in coincidencias:
        print(f"  {cadena}")


def listar_clases(dex_por_nombre: dict[str, bytes], prefijo: str) -> None:
    DEX = cargar_androguard()
    if DEX is None:
        sys.exit("Esta opcion necesita androguard: pip install androguard")

    prefijo_desc = prefijo.replace(".", "/").lstrip("L")
    for nombre, blob in dex_por_nombre.items():
        try:
            dex = DEX(blob)
        except Exception:
            continue
        for clase in dex.get_classes():
            clase_desc = clase.get_name()
            if prefijo_desc in clase_desc:
                print(f"{clase_desc}  ({nombre})")


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Comprueba las huellas de los parches de Parche Facemoji sobre una APK.",
    )
    parser.add_argument("apk", type=Path, help="Ruta de la APK / APKM / XAPK de Facemoji")
    parser.add_argument("--informe", action="store_true", help="Resumen de SDK detectados")
    parser.add_argument("--comprobar", action="store_true", help="Comprueba las huellas de los parches")
    parser.add_argument("--buscar", metavar="REGEX", help="Busca cadenas del DEX (VIP, premium, ...)")
    parser.add_argument("--clases", metavar="PAQUETE", help="Lista clases que contengan el paquete")
    args = parser.parse_args()

    if not args.apk.is_file():
        sys.exit(f"No existe el fichero: {args.apk}")

    dex_por_nombre = dex_blobs(args.apk)
    cadenas: set[str] = set()
    for blob in dex_por_nombre.values():
        cadenas.update(dex_strings(blob))

    print(f"{len(dex_por_nombre)} ficheros DEX, {len(cadenas)} cadenas distintas\n")

    if not any((args.informe, args.comprobar, args.buscar, args.clases)):
        args.informe = True

    if args.informe:
        informe(cadenas)
    if args.comprobar:
        comprobar(cadenas, dex_por_nombre)
    if args.buscar:
        buscar(cadenas, args.buscar)
    if args.clases:
        listar_clases(dex_por_nombre, args.clases)


if __name__ == "__main__":
    main()
