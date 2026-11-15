# 🚧 Roadmap

## Hecho

- **Ajustes de Minecraco**: pantalla propia (interruptores de anuncios, VIP y rastreo) con una
  fila dentro de los ajustes de Facemoji, que es un `android.preference.PreferenceFragment`.
  Los parches son condicionales: el smali pregunta al ajuste antes de actuar.
- **Verificación del parcheo en CI**: `verificarParcheo` aplica el bundle a la APK real y un
  script comprueba manifiesto y bytecode del APK resultante.
- **Eliminar anuncios** (parcial): apaga el motor de **sugerencias patrocinadas** del teclado
  (`AdSuggestionUtils`), que es el anuncio que aparece mientras se escribe.
- **Desbloquear VIP**: fuentes de color (`CoolFontBean`), series de stickers (`SeriesStickerItem`,
  `StickerItem`), TextBomb (`TextBombBean`) y frases del teclado Rizz (`RizzConfigInfoDetail`,
  `RizzConfigInfoDetail2`, `RizzIceBreakerConfigInfoDetail`), incluidos los desbloqueos por vídeo.
- **Desactivar rastreo**: Firebase Analytics, Crashlytics, AppsFlyer (Facebook no está en la app).
- **Quitar permiso de ID de publicidad** (`AD_ID`).

## Cómo se hizo (3.7.0)

El análisis completo está en [`analisis/`](../analisis). Lo importante:

- El `RemoveAdsPatch` original apuntaba a la API pública de AdMob y **no coincidía con nada**
  (`--comprobar` daba 0/10): la app no usa `BaseAdView`, `InterstitialAd` ni `AdLoader`.
- La red real es **Pangle/CSJ (ByteDance)**, cuyas clases ha renombrado R8. No hay ningún nombre
  estable dentro del SDK para engancharse, así que el parche no puede bloquearlo desde dentro.
- El sistema VIP no está centralizado: cada catálogo lleva su propia bandera `isVip` y esas
  banderas conservan el nombre, que es lo que explota el parche.

## Hecho: entrada en la lista de ajustes principal

La pantalla que se ve al abrir *Ajustes* (Idiomas, Entrada, Estilo De Emoji, Seguimos, TikTok,
Comentarios, Preguntas Frecuentes, Acerca De) la construye `SettingsMainFragment`, que en 3.7.0
es la clase ofuscada `Lef/n`. Se localizó siguiendo `SettingsActivity`, que la instancia con la
etiqueta `"SettingsMainFragment"`.

Resultó ser también un `android.preference.PreferenceFragment` (como `SettingsAdvancedFragment`),
así que basta con añadir una `Preference` y moverla al primer puesto de la lista interna
(`mPreferenceList`, por reflexión) para que salga arriba del todo.

Queda anotado para la próxima versión: si R8 cambia el nombre de `Lef/n`, la huella deja de
coincidir y hay que volver a localizarla con
`--llamadas 'addPreferencesFromResource'` (sin restringir a `com/baidu/simeji`).

## Pendiente: publicidad a pantalla completa

Los anuncios de bienvenida y los emergentes los sirve Pangle. Opciones para atacarlos en el futuro:

1. **Encontrar el envoltorio propio de la app** que llama a Pangle. En 3.7.0 no hay llamadas
   directas (`--llamadas 'openadsdk'` da 0 resultados), así que o se hace por reflexión o vive en
   clases con nombre de una letra. Habría que seguir el flujo desde
   `com.baidu.simeji.skins.video.*VideoRewardActivity` con jadx.
2. **Parchear el arranque del SDK** por firma de método en vez de por clase (los nombres de método
   de Pangle también se renombraron, así que es frágil).
3. **Bloquear a nivel de red** (`hosts`/DNS): no es un parche de Morphe, pero es lo único que
   corta el 100 % de los anuncios sin tocar el bytecode.

## Ideas siguientes

- **Ajustes por bandera**: hoy el interruptor de VIP toca todas las banderas a la vez (fuentes,
  stickers, TextBomb, Rizz). Se podrían separar en cuatro interruptores.
- **Más pantallas de ajustes**: solo se ha enganchado `SettingsAdvancedFragment`; si hay otra
  pestaña de ajustes donde encaje mejor la fila, añadirla.
- **Compatibilidad de versiones**: cuando cambie la versión, volver a ejecutar
  `tools/analizar_apk.py --comprobar` para ver qué huellas siguen vivas.
- **Ocultar avisos internos** (valoración, actualización obligatoria): sus métodos suelen ser
  `void` sin parámetros y encajan con `blockVoidMethods`.
