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
            // Version objetivo del desarrollo (APKMirror la publica como BUNDLE, ".apkm").
            AppTarget(
                version = "3.7.0",
                // Todavia no verificada en dispositivo: las huellas se basan en las APIs
                // publicas de los SDK, no en una APK comprobada. Al confirmarla, quitar
                // isExperimental. Ver docs/VERIFICAR_HUELLAS.md
                isExperimental = true,
                description = "Version objetivo de desarrollo. Sin verificar en dispositivo.",
            ),
            // Las huellas no dependen de la version concreta de Facemoji, asi que se deja
            // abierta cualquier otra version de forma experimental.
            AppTarget(
                version = null,
                isExperimental = true,
                description = "Cualquier otra version de Facemoji. Experimental: " +
                        "abre una incidencia si el parche falla.",
            ),
        ),
    )
}
