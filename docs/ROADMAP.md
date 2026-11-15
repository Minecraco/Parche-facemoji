# 🚧 Roadmap

## Hecho

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

- **Ajustes en la app** (pantalla de preferencias del parche) para elegir qué banderas VIP se
  tocan: ahora se tocan todas de golpe.
- **Compatibilidad de versiones**: cuando cambie la versión, volver a ejecutar
  `tools/analizar_apk.py --comprobar` para ver qué huellas siguen vivas.
- **Ocultar avisos internos** (valoración, actualización obligatoria): sus métodos suelen ser
  `void` sin parámetros y encajan con `blockVoidMethods`.
