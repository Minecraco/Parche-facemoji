package app.parchefacemoji.patches.facemoji.settings

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.patch
import app.morphe.patcher.patch.resourceXml
import app.parchefacemoji.patches.facemoji.shared.Constants.COMPATIBILITY_FACEMOJI
import java.util.logging.Logger

private val logger = Logger.getLogger("ParcheFacemoji")

/** Clase de la extension que anyade la fila a la pantalla de ajustes. */
private const val ENTRY_CLASS = "Lapp/parchefacemoji/extension/facemoji/settings/SettingsEntry;"

/** Clase de la pantalla de ajustes del parche. */
private const val SETTINGS_ACTIVITY = "app.parchefacemoji.extension.facemoji.settings.SettingsActivity"

/**
 * Pantalla de ajustes avanzados de Facemoji (`SettingsAdvancedFragment`), que hereda de
 * `android.preference.PreferenceFragment`. Se engancha `onResume()` porque en ese momento la
 * lista de preferencias ya esta creada.
 */
internal object SettingsAdvancedOnResumeFingerprint : Fingerprint(
    definingClass = "Lcom/baidu/simeji/settings/SettingsAdvancedFragment;",
    name = "onResume",
    returnType = "V",
)

/**
 * Anade una entrada "Ajustes de Minecraco" en los ajustes de la app.
 *
 * Los ajustes de Facemoji son un `android.preference.PreferenceFragment` clasico (se comprobo
 * en la 3.7.0: `SettingsAdvancedFragment` llama a `addPreferencesFromResource`). Por eso basta
 * con:
 *
 * 1. Declarar la actividad propia en el manifiesto, para poder abrirla.
 * 2. Inyectar una llamada al principio de `onResume()` que anada una `Preference` mas a la
 *    lista; al pulsarla se abre la actividad.
 *
 * La fila se anade desde la extension (no con smali suelto) para que sea tolerante a fallos:
 * si la app cambia esa pantalla, los ajustes se abren igual y solo se pierde la entrada.
 */
@Suppress("unused")
val settingsScreenPatch = patch(
    name = "Ajustes de Minecraco",
    description = "Anade una entrada \"Ajustes de Minecraco\" dentro de los ajustes de Facemoji " +
            "para activar y desactivar los parches de anuncios, VIP y rastreo sin volver a parchear.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_FACEMOJI)

    category("Ajustes")

    extendWith("extensions/facemoji.mpe")

    execute {
        // 1. Declarar la actividad en el manifiesto (idempotente: si ya esta, no se duplica).
        resourceXml("AndroidManifest.xml").use { manifiesto ->
            val yaEsta = manifiesto.elementByAttributeOrNull("android:name", SETTINGS_ACTIVITY)

            if (yaEsta == null) {
                val aplicacion = manifiesto.elements("application").first()

                aplicacion.appendChild("activity") {
                    setAttribute("android:name", SETTINGS_ACTIVITY)
                    setAttribute("android:exported", "false")
                    setAttribute("android:label", "Ajustes de Minecraco")
                    setAttribute(
                        "android:theme",
                        "@android:style/Theme.DeviceDefault.Light.NoActionBar",
                    )
                }
            }
        }

        // 2. Anadir la fila a la pantalla de ajustes avanzados.
        val coincidencias = SettingsAdvancedOnResumeFingerprint.matchAllOrNull().orEmpty()

        if (coincidencias.isEmpty()) {
            throw PatchException(
                "No se ha encontrado la pantalla de ajustes avanzados de Facemoji en esta " +
                        "version. Desactiva este parche o abre una incidencia en " +
                        "https://github.com/Minecraco/Parche-facemoji/issues indicando la version.",
            )
        }

        coincidencias.forEach { match ->
            match.method.addInstructions(
                0,
                """
                    invoke-static {p0}, $ENTRY_CLASS->agregarEntrada(Ljava/lang/Object;)V
                """,
            )
        }

        logger.info(
            "Entrada de ajustes anyadida en ${coincidencias.size} pantalla(s); " +
                    "actividad declarada: $SETTINGS_ACTIVITY",
        )
    }
}
