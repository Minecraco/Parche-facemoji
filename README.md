# 🧩 Parche Facemoji

Parches [Morphe](https://morphe.software) en español para **Facemoji AI Emoji Keyboard**
(`com.simejikeyboard`), el teclado con emojis, fuentes y temas de Nexileap.

### Cómo usar estos parches

Añade esta fuente a Morphe Manager con el botón de abajo, o manualmente con la URL
`https://github.com/Minecraco/Parche-facemoji`:

<a href="https://morphe.software/add-source?github=Minecraco/Parche-facemoji">
  <img src=".github/assets/add-to-morphe.svg" alt="Add to Morphe" height="48">
</a>

Después:

1. Descarga la APK de Facemoji **desde APKMirror** (se publica como *BUNDLE*, es decir, `.apkm`).
2. En Morphe Manager abre *Patcher*, elige Facemoji y selecciona los parches.
3. Aplica y instala la APK parcheada.

## 🩹 Parches

| Parche | Por defecto | Qué hace |
| --- | --- | --- |
| **Eliminar anuncios** | ✅ Sí | Bloquea la carga y la visualización de anuncios de AdMob (banner, nativo, intersticial, *app open*, recompensado) y de las redes de mediación más habituales (Facebook, Unity Ads, AppLovin). |
| **Desactivar rastreo** | ❌ No | Neutraliza el envío de eventos de Firebase Analytics, Crashlytics, Facebook y AppsFlyer. |
| **Quitar permiso de ID de publicidad** | ❌ No | Elimina `com.google.android.gms.permission.AD_ID` del manifiesto, así la app no puede leer el identificador de publicidad del dispositivo. |

Los parches se aplican de forma **opcional y tolerante**: si una red publicitaria o un SDK
no está en tu versión de Facemoji, se ignora en vez de romper el parcheo. El parche de
anuncios sí falla de forma explícita si no encuentra **ninguna** red conocida, para que no
te quedes con una app "parcheada" que sigue mostrando anuncios.

### ⚠️ Estado de verificación

Estas huellas (*fingerprints*) se han escrito contra la **API pública de los SDK de terceros**
que la app incluye, según los metadatos públicos de Facemoji (AdMob, Facebook, Firebase
Analytics/Crashlytics y AppsFlyer). Eso las hace independientes del código ofuscado de la app y
resistentes a las actualizaciones, pero **todavía no se han validado contra una APK real**, por
lo que todas las versiones aparecen como *experimental* en Morphe Manager.

Si tu versión falla, abre una incidencia con el mensaje de error y la versión de la app, o
ejecuta el verificador incluido (ver [docs/VERIFICAR_HUELLAS.md](docs/VERIFICAR_HUELLAS.md)).

### 🚧 Pendiente

El desbloqueo de **VIP / Pro** y de los temas y fuentes de pago está en el roadmap:
necesita analizar el bytecode ofuscado de la app. Detalles en [docs/ROADMAP.md](docs/ROADMAP.md).

## 🩹 Lista de parches publicada

<!-- PATCHES_START EXPANDED -->

<!-- No modifiques esta sección a mano. La lista se genera cuando release.yml publica una versión.
     Si prefieres que la lista aparezca plegada, quita la palabra 'EXPANDED' del comentario de arriba.
     Si prefieres mantenerla a mano, elimina los comentarios PATCHES_START y PATCHES_END. -->

#### La lista de parches aparecerá aquí automáticamente tras la primera publicación.

&nbsp;

## 🛠️ Compilar en local

Requisitos: JDK 21 y un token de GitHub con permiso `read:packages`
(el proyecto descarga el plugin de Morphe desde GitHub Packages).

```bash
export GITHUB_ACTOR=tu-usuario
export GITHUB_TOKEN=ghp_xxx      # token con read:packages

./gradlew buildAndroid
```

El bundle queda en `patches/build/libs/patches-*.mpp` y se aplica con
[Morphe Desktop](https://github.com/MorpheApp/morphe-desktop) o Morphe Manager
(añadiéndolo como fuente local).

Más detalles en [docs/DESARROLLO.md](docs/DESARROLLO.md).

## 📁 Estructura

```text
patches/src/main/kotlin/app/parchefacemoji/patches/facemoji/
├── ads/         parche de anuncios + huellas de las redes publicitarias
├── tracking/    parche de analítica + huellas de los rastreadores
├── privacy/     cambios de manifiesto (permiso AD_ID)
└── shared/      compatibilidad de la app y utilidades de hooking

extensions/extension/   código Java que se inyecta en la app (PatchHooks)
tools/analizar_apk.py   verificador de huellas sobre una APK real
docs/                   documentación de desarrollo y verificación
```

## 📜 Licencia

GPLv3. Ver [LICENSE](LICENSE) y [NOTICE](NOTICE).
Este proyecto no está afiliado a Nexileap ni al proyecto Morphe.

<!-- El comentario de cierre está aquí a propósito para que la primera publicación limpie el README. -->
<!-- PATCHES_END -->
