# 🧩 Parche Facemoji

[![Build patches bundle](https://github.com/Minecraco/Parche-facemoji/actions/workflows/build.yml/badge.svg)](https://github.com/Minecraco/Parche-facemoji/actions/workflows/build.yml)

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

## 📦 Dónde está el `.mpp`

El archivo de parches se puede conseguir de dos formas:

1. **En cada release** (pestaña *Releases*): `patches-<versión>.mpp`, generado por
   `release.yml` con semantic-release. Las releases se crean al fusionar en `main`
   commits de tipo `feat:` o `fix:`.
2. **En cada push** (pestaña *Actions* → workflow **Build patches bundle**): artefacto
   `parche-facemoji-mpp`, sin necesidad de publicar release. También se puede lanzar a mano
   con *Run workflow*.

En ambos casos el `.mpp` se aplica en **Morphe Desktop** (*Patch bundle* → seleccionar el
fichero) o se añade como fuente local en Morphe Manager.

## 🩹 Parches

Versión objetivo del desarrollo: **Facemoji 3.7.0** (APKMirror la publica como *BUNDLE*,
es decir `.apkm`). El resto de versiones se aceptan como *experimental*.

| Parche | Por defecto | Qué hace |
| --- | --- | --- |
| **Eliminar anuncios** | ✅ Sí | Apaga las **sugerencias patrocinadas** que la app inserta en la barra de sugerencias del teclado. |
| **Desbloquear VIP** | ✅ Sí | Marca como gratuitas las fuentes de color, las series de stickers, los TextBomb y las frases del teclado Rizz que la app reserva a la suscripción, y da por hechos los desbloqueos por vídeo. |
| **Desactivar rastreo** | ❌ No | Neutraliza el envío de eventos de Firebase Analytics, Crashlytics, Facebook y AppsFlyer. |
| **Quitar permiso de ID de publicidad** | ❌ No | Elimina `com.google.android.gms.permission.AD_ID` del manifiesto, así la app no puede leer el identificador de publicidad del dispositivo. |

Los parches se aplican de forma **opcional y tolerante**: si un rastreador o un SDK no está en
tu versión de Facemoji, se ignora en vez de romper el parcheo. Los parches de anuncios y de VIP
sí fallan de forma explícita si no encuentran **ninguno** de sus puntos de enganche, para que no
te quedes con una app "parcheada" que sigue igual.

### 🔍 Qué se ha encontrado en la app real (3.7.0)

Los análisis completos están en [`analisis/`](analisis) y se generan con
`tools/analizar_apk.py` sobre la APK 3.7.0 de APKMirror. Resumen:

- **La publicidad no es de AdMob.** AdMob solo está como stub de mediación (no existen
  `BaseAdView`, `InterstitialAd` ni `AdLoader`). La red real es **Pangle/CSJ (ByteDance)**,
  con unas 2.000 referencias, pero R8 ha renombrado sus clases: no queda ni un nombre estable
  dentro del SDK al que engancharse.
- **Lo que sí se puede apagar** es el motor de **sugerencias patrocinadas** del teclado
  (`com.baidu.simeji.plutus.adsuggestion.AdSuggestionUtils`): sus dos métodos de decisión y las
  tres configuraciones remotas que los alimentan. Es el anuncio que aparece mientras escribes.
- **El sistema VIP no está centralizado**: cada catálogo lleva su propia bandera
  (`CoolFontBean.isVip`, `SeriesStickerItem.isSeriesVip`, `TextBombBean.isVip`, …). Esas
  banderas tienen nombre propio y son el punto de enganche del parche de VIP.

Verificación automática contra la APK real (`--comprobar`):

```text
=== Eliminar anuncios ===   => 5/5 huellas disponibles.
=== Desbloquear VIP ===     => 12/12 huellas disponibles.
=== Desactivar rastreo ===  => 8/9 huellas disponibles. (falta Facebook, que la app no incluye)
```

### ⚠️ Estado de verificación y limitaciones

- Las huellas se han comprobado contra la **APK 3.7.0 real** (las 15 + 12 + 8 coinciden), pero
  **no se ha probado en un dispositivo**. Por eso todas las versiones siguen apareciendo como
  *experimental* en Morphe Manager.
- **La publicidad a pantalla completa (bienvenida y emergentes) no se elimina**: la sirve Pangle
  con las clases renombradas y no hay un punto de enganche estable en 3.7.0. El parche de
  anuncios solo apaga las sugerencias patrocinadas del teclado.
- **El servidor sigue mandando**: si Facemoji comprueba la suscripción al descargar una fuente o
  un sticker, la descarga puede fallar aunque el candado ya no se vea.
- **La app no está ofuscada del todo**: los modelos de datos y los gestores conservan sus nombres
  (paquete `com.baidu.simeji.*`), pero la capa de UI, el motor de sugerencias por dentro y el SDK de
  anuncios sí están ofuscados. Los parches solo tocan lo que conserva nombre propio.

Si algo falla, abre una incidencia con el mensaje de error y la versión de la app, o ejecuta el
verificador incluido (ver [docs/VERIFICAR_HUELLAS.md](docs/VERIFICAR_HUELLAS.md)).

## 🩹 Lista de parches publicada

<!-- PATCHES_START EXPANDED -->
> **[v1.0.0](https://github.com/Minecraco/Parche-facemoji/releases/tag/v1.0.0)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;4 patches total
<details open>
<summary>📦 Facemoji AI Emoji Keyboard&nbsp;&nbsp;•&nbsp;&nbsp;4 patches</summary>
<br>

**Package name:** `com.simejikeyboard`

**🎯 Supported versions:**

| 🧪&nbsp;3.7.0 |
| :---: |
| Version objetivo. Huellas verificadas sobre la APK real; falta la prueba en dispositivo. | Cualquier otra version de Facemoji. Experimental: los objetivos pueden haber cambiado. Abre una incidencia si el parche falla. |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Desactivar rastreo](#desactivar-rastreo) | Deja de enviar los eventos de Firebase Analytics, Crashlytics, Facebook y AppsFlyer. Desactiva tambien el envio de registros de fallos. |  |
| [Desbloquear VIP](#desbloquear-vip) | Marca como gratuitos las fuentes de color, las series de stickers, los TextBomb y las frases del teclado Rizz que la app reserva a la suscripcion, y da por hechos los desbloqueos por video. |  |
| [Eliminar anuncios](#eliminar-anuncios) | Apaga las sugerencias patrocinadas que la app inserta en el teclado. En la version 3.7.0 la publicidad a pantalla completa la sirve Pangle con las clases renombradas, por lo que ese formato no se puede bloquear de forma estable. |  |
| [Quitar permiso de ID de publicidad](#quitar-permiso-de-id-de-publicidad) | Elimina el permiso AD_ID del manifiesto para que la app no pueda leer el identificador de publicidad del dispositivo. |  |

</details>

<!-- PATCHES_END -->
