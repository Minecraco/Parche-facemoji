# 🚧 Roadmap

## Hecho

- **Eliminar anuncios**: AdMob (banner, nativo, intersticial, *app open*, recompensado,
  recompensado intersticial) y mediación (Facebook, Unity Ads, AppLovin).
- **Desactivar rastreo**: Firebase Analytics, Crashlytics, Facebook, AppsFlyer.
- **Quitar permiso de ID de publicidad** (`AD_ID`).

## Pendiente: VIP / Pro y contenido de pago

Facemoji vende suscripción VIP y temas/fuentes de pago. Para parchearlo hace falta
analizar el bytecode **propio** de la app (no el de un SDK), que sí está ofuscado:

- La comprobación de VIP dentro de la app (probablemente un `isVip()` / `isPremium()` en
  un paquete ofuscado de una o dos letras, como `ek`, `a`, `b`…).
- El estado de compra que llega del servidor y se guarda en `SharedPreferences`/`DataStore`.
- La lista de temas y fuentes marcados como premium (normalmente por un campo `isVip`
  o `price > 0` en la respuesta JSON).

Riesgos que hay que confirmar antes de prometer nada:

1. **Entitlement en servidor**: si el servidor decide qué contenido se puede usar, parchear
   el cliente solo desbloquea la interfaz, no el contenido real.
2. **Comprobación de firma**: varias apps de este tipo validan la firma del APK antes de
   conceder privilegios. Habría que añadir `Spoof signature`/`Change package name`.
3. **Riesgo de baneo o de cuelgues**: interceptar la inicialización de la suscripción puede
   dejar el flujo de compra en un estado inconsistente.

### Qué hace falta para hacerlo bien

- Una **APK/APKM original** de la versión a soportar (no un mod) para obtener las huellas
  reales. Con ella se puede usar `tools/analizar_apk.py --buscar "vip|premium|subscribe"`
  para localizar las claves y luego confirmarlas con jadx.
- Probar en dispositivo: abrir temas premium, teclado y pantalla de cuenta.
- Decidir el alcance: ¿solo ocultar los avisos de "hazte VIP" o desbloquear los temas?

## Otras ideas (más sencillas)

- **Ocultar los avisos internos de la app** (banner "quitar anuncios", diálogo de valoración).
  Requiere huellas del código propio, pero suelen ser métodos `void` sin parámetros, fáciles
  de neutralizar.
- **Bloquear el diálogo de actualización obligatoria**: mismo enfoque.
- **Desactivar la analítica por completo** interceptando la inicialización de Firebase
  (`FirebaseApp.initializeApp`), en lugar de solo los eventos.
- **Ajustes en la app** (pantalla de preferencias del parche): Morphe permite añadir una
  pantalla de ajustes propia; útil si algún día hay más de tres parches.
