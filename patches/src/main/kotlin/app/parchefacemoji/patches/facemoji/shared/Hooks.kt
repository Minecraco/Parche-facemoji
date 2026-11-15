package app.parchefacemoji.patches.facemoji.shared

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions

/**
 * Descriptor de la clase de la extension que se fusiona en Facemoji.
 * Ver extensions/extension/src/main/java/app/parchefacemoji/extension/facemoji/PatchHooks.java
 */
internal const val EXTENSION_CLASS = "Lapp/parchefacemoji/extension/facemoji/PatchHooks;"

/**
 * Neutraliza todos los metodos que coincidan con esta huella y que devuelvan `void`.
 *
 * Se inserta al principio del metodo:
 *
 * ```smali
 * const-string v0, "<etiqueta>"
 * invoke-static {v0}, L.../PatchHooks;-><metodoExtension>(Ljava/lang/String;)V
 * return-void
 * ```
 *
 * `v0` es seguro aqui por dos motivos:
 * - Siempre existe: cualquier metodo con parametros tiene al menos un registro.
 * - Solo se escribe justo antes de `return-void`, asi que no puede corromper
 *   ningun valor que el metodo fuese a usar despues.
 *
 * Las huellas se resuelven de forma opcional: si una red publicitaria o un SDK de
 * analitica no esta presente en la version de Facemoji que se esta parcheando, se ignora
 * en lugar de hacer fallar el parche entero. Asi el mismo parche sirve para varias versiones.
 *
 * @param surface Etiqueta corta que se escribe en logcat (solo ASCII).
 * @param extensionMethod Metodo de [EXTENSION_CLASS] que recibe la etiqueta.
 * @return `true` si se ha neutralizado al menos un metodo.
 */
internal fun Fingerprint.blockVoidMethods(
    surface: String,
    extensionMethod: String = "logBlocked",
): Boolean {
    var patched = false

    matchAllOrNull().orEmpty().forEach { match ->
        val original = match.originalMethod

        // El codigo insertado termina en `return-void`, por lo que solo vale para metodos void.
        if (original.returnType != "V") return@forEach

        // Un metodo abstracto o nativo no tiene cuerpo donde insertar nada.
        if (original.implementation == null) return@forEach

        match.method.addInstructions(
            0,
            """
                const-string v0, "$surface"
                invoke-static {v0}, $EXTENSION_CLASS->$extensionMethod(Ljava/lang/String;)V
                return-void
            """,
        )

        patched = true
    }

    return patched
}
