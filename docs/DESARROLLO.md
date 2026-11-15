# 🧑‍💻 Desarrollo

Guía para modificar, compilar y publicar los parches de este repositorio.

## 1. Preparar el entorno

1. Instala **JDK 21** (`java -version` debe responder 21).
2. Crea un *Personal Access Token* de GitHub con el permiso `read:packages`.
   El plugin `app.morphe.patches` y el patcher se descargan desde GitHub Packages.
3. Expórtalo antes de compilar:

   ```bash
   export GITHUB_ACTOR=tu-usuario
   export GITHUB_TOKEN=ghp_xxx
   ```

4. `./gradlew buildAndroid` genera el bundle en `patches/build/libs/patches-*.mpp`.

Alternativa para desarrollo: clona también `morphe-patcher` y `morphe-patches-library`
junto a este repositorio. `settings.gradle.kts` los detecta como *composite builds* y así
no hace falta publicar el plugin para probar cambios del patcher.

## 2. Estructura del proyecto

```text
patches/src/main/kotlin/app/parchefacemoji/patches/facemoji/
├── ads/
│   ├── Fingerprints.kt        puntos de entrada de cada red publicitaria
│   └── RemoveAdsPatch.kt      parche "Eliminar anuncios"
├── tracking/
│   ├── Fingerprints.kt        puntos de entrada de analítica y atribución
│   └── DisableTrackingPatch.kt
├── privacy/
│   └── RemoveAdIdPermissionPatch.kt
└── shared/
    ├── Constants.kt           compatibilidad con la app (paquete, versiones)
    └── Hooks.kt               ayuda para neutralizar métodos void

extensions/extension/src/main/java/app/parchefacemoji/extension/facemoji/
└── PatchHooks.java            código Java inyectado en la app (logcat)
```

## 3. Cómo funciona un parche de este repo

Los parches **no tocan el código ofuscado de Facemoji**: interceptan la API pública de los
SDK de terceros que la app incluye (AdMob, Firebase…). Esa API la conservan las reglas de
ofuscación de cada SDK, así que el parche sobrevive a las actualizaciones de la app.

Para bloquear un anuncio se reescribe el principio del método del SDK así:

```smali
const-string v0, "AdMob: banner"
invoke-static {v0}, Lapp/parchefacemoji/extension/facemoji/PatchHooks;->logBlocked(Ljava/lang/String;)V
return-void
```

El SDK nunca llega a descargar el anuncio y la app no recibe ningún callback, así que no
aparece ni el anuncio ni un mensaje de error. Las etiquetas se ven en logcat con:

```bash
adb logcat -s ParcheFacemoji
```

La implementación vive en `shared/Hooks.kt` (`Fingerprint.blockVoidMethods`), que resuelve
la huella con `matchAllOrNull()`: si el SDK no está en esa versión de la app, se ignora.

## 4. Añadir una red publicitaria nueva

1. Añade la huella en `ads/Fingerprints.kt`:

   ```kotlin
   internal object MiRedInterstitialFingerprint : Fingerprint(
       definingClass = "Lcom/mired/ads/Interstitial;",
       name = "show",
       returnType = "V",
   )
   ```

2. Regístrala en `AD_SURFACES` dentro de `RemoveAdsPatch.kt`.
3. Comprueba que existe en la APK real:

   ```bash
   python3 tools/analizar_apk.py Facemoji.apkm --comprobar
   ```

## 5. Reglas de estilo

- Nombra las huellas con lo que hacen y terminadas en `Fingerprint`.
- Los parches se nombran por lo que hacen ("Eliminar anuncios") y la descripción va en
  tercera persona, presente y termina en punto.
- Las etiquetas que se envían al smali deben ser **ASCII** (sin acentos) para evitar
  problemas de codificación en el `const-string`.
- Documenta *por qué* se parchea un método, no solo cómo.

## 6. Publicar

- Los commits deben ser semánticos: `feat:`, `fix:` o `chore:`.
  `feat:` y `fix:` generan nueva versión; `chore:` no.
- Trabaja en `dev` para pre-releases y fusiona en `main` (sin *squash*) para una versión estable.
- No edites a mano `patches-list.json`, `patches-bundle.json` ni `CHANGELOG.md`: los genera
  `release.yml`.
- Opcional pero recomendado: firma las releases para que Morphe Manager rechace un bundle
  manipulado.

  ```bash
  ./gradlew generateSigningKey   # crea morphe-signing.pub y morphe-signing.key
  ```

  Sube `morphe-signing.pub`, guarda `morphe-signing.key` como secreto `MORPHE_SIGNING_KEY`
  en el repositorio y borra el fichero.
