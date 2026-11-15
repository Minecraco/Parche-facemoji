package app.parchefacemoji.patches.facemoji.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    /**
     * Facemoji AI Emoji Keyboard.
     *
     * En APKMirror / UpToDown la app se publica como BUNDLE, que Morphe gestiona como APKM.
     * Si se declara otro tipo, Morphe Manager no enlazara al usuario a la descarga correcta.
     */
    val COMPATIBILITY_FACEMOJI = Compatibility(
        name = "Facemoji AI Emoji Keyboard",
        packageName = "com.simejikeyboard",
        description = "Teclado con emojis, fuentes y temas de Nexileap.",
        apkFileType = ApkFileType.APKM,
        // Color de fondo del icono de la app en Morphe Manager.
        appIconColor = 0xE0245E,
        targets = listOf(
            // Las huellas (fingerprints) de estos parches se basan en las APIs publicas de
            // los SDK de terceros que la app incluye, por lo que no dependen de la version
            // concreta de Facemoji. Aun asi, ninguna version esta verificada al 100% todavia:
            // se marcan como experimentales hasta confirmarlas con una APK real.
            // Ver docs/VERIFICAR_HUELLAS.md
            AppTarget(
                version = null,
                isExperimental = true,
                description = "Cualquier version de Facemoji. Sin verificar todavia: " +
                        "abre una incidencia si el parche falla."
            ),
        ),
    )
}
