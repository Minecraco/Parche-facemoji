# 🔍 Verificar y ajustar las huellas

Los parches buscan métodos por sus **huellas** (*fingerprints*). Si una huella no coincide
en tu versión de Facemoji, el parche no funciona. Esta guía explica cómo comprobarlo y cómo
arreglarlo.

## 1. Comprobación rápida con el verificador incluido

Solo necesitas Python 3 (sin Java, sin apktool):

```bash
python3 tools/analizar_apk.py Facemoji.apkm --comprobar
```

Salida de ejemplo:

```text
=== Eliminar anuncios ===
  [x] Lcom/google/android/gms/ads/BaseAdView;->loadAd
  [ ] Lcom/google/android/gms/ads/appopen/AppOpenAd;->show
  ...
  => 5/10 huellas disponibles.
```

- `[x]` la huella existe en tu APK: el parche la va a interceptar.
- `[ ]` no existe: o la app no usa ese SDK, o ha cambiado (hay que ajustar la huella).

Para un análisis exacto (clase + método, no solo cadenas) instala androguard:

```bash
pip install androguard
python3 tools/analizar_apk.py Facemoji.apkm --comprobar
```

## 2. Ver qué SDK trae la app

```bash
python3 tools/analizar_apk.py Facemoji.apkm --informe
```

Lista AdMob, Facebook Audience Network, Unity Ads, AppLovin, Pangle, ironSource,
Mintegral, Firebase, Crashlytics, AppsFlyer y Adjust. Si tu APK usa una red que no está en
`AD_SURFACES`, es el momento de añadirla (ver `docs/DESARROLLO.md`).

## 3. Ajustar una huella que ya no coincide

Las redes publicitarias y los SDK de analítica no ofuscan su API pública, pero a veces
cambian el paquete o la firma del método (por ejemplo, un `show()` que pasa a
`show(Activity, Callback)`).

1. Busca la clase real:

   ```bash
   python3 tools/analizar_apk.py Facemoji.apkm --clases com/google/android/gms/ads
   ```

2. Con [jadx](https://github.com/skylot/jadx) o `apktool d`, abre la clase y mira el método:

   ```bash
   jadx-gui Facemoji.apkm
   ```

3. Actualiza la huella en `patches/src/main/kotlin/app/parchefacemoji/patches/facemoji/ads/Fingerprints.kt`.
   Si el método ya no devuelve `void`, no puedes usar `return-void`: usa
   `returnEarly(<valor>)` o intercepta en otro punto.

## 4. Comprobar en el dispositivo

Con la app parcheada instalada y el teclado abierto:

```bash
adb logcat -s ParcheFacemoji
```

Deberías ver líneas como:

```text
D ParcheFacemoji: Anuncio bloqueado: AdMob: banner
```

Si el log aparece, la interceptación funciona y solo queda comprobar visualmente que el
anuncio ya no se muestra. Si no aparece ninguna línea, la huella no coincide en tu versión.

## 5. Localizar el código VIP (para el parche pendiente)

```bash
pip install androguard     # recomendado, lista también métodos
python3 tools/analizar_apk.py Facemoji.apkm --vip
```

Imprime dos listas:

- **Cadenas** que parecen claves de preferencias, campos JSON o SKUs
  (`is_vip`, `vip_status`, `premium_theme`…).
- **Métodos** cuyo nombre contiene `vip`/`premium`/`isPro`/`subscri` (si hay androguard).

Ese listado es lo que hace falta para escribir el parche de desbloqueo VIP.

## 6. Reportar

Abre una incidencia en
<https://github.com/Minecraco/Parche-facemoji/issues> con:

- Versión de Facemoji (`ajustes → acerca de` o el nombre del fichero APK).
- La salida de `python3 tools/analizar_apk.py tu.apkm --comprobar`.
- El mensaje de error de Morphe Manager, si lo hubo.
