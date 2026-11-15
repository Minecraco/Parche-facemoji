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

/**
 * Hace que todos los metodos que coincidan con esta huella devuelvan siempre `value`.
 *
 * Se usa para las comprobaciones booleanas de la app (`isVip()`, `getEnable()`, ...), que son
 * la forma habitual de apagar una funcion o de saltarse una comprobacion de pago.
 *
 * El codigo insertado es:
 *
 * ```smali
 * const-string v0, "<etiqueta>"
 * invoke-static {v0}, L.../PatchHooks;->logBlocked(Ljava/lang/String;)V
 * const/4 v0, 0x0    ; o 0x1
 * return v0
 * ```
 *
 * Se escribe al principio del metodo y termina en `return`, asi que el cuerpo original nunca
 * llega a ejecutarse y `v0` no puede pisar ningun valor en uso.
 *
 * @param value Valor que devolvera el metodo.
 * @param surface Etiqueta corta que se escribe en logcat (solo ASCII).
 * @return `true` si se ha modificado al menos un metodo.
 */
internal fun Fingerprint.forceBoolean(
    value: Boolean,
    surface: String,
    extensionMethod: String = "logBlocked",
): Boolean {
    var patched = false

    matchAllOrNull().orEmpty().forEach { match ->
        val original = match.originalMethod

        // Solo sirve para metodos que devuelven boolean.
        if (original.returnType != "Z") return@forEach

        // Un metodo abstracto o nativo no tiene cuerpo donde insertar nada.
        val implementation = original.implementation ?: return@forEach

        // `const/4 v0` necesita que el metodo tenga al menos un registro libre.
        if (implementation.registerCount < 1) return@forEach

        val literal = if (value) "0x1" else "0x0"

        match.method.addInstructions(
            0,
            """
                const-string v0, "$surface"
                invoke-static {v0}, $EXTENSION_CLASS->$extensionMethod(Ljava/lang/String;)V
                const/4 v0, $literal
                return v0
            """,
        )

        patched = true
    }

    return patched
}

/**
 * Hace que todos los metodos que coincidan con esta huella devuelvan el entero `value`.
 *
 * Variante de [forceBoolean] para las banderas que la app guarda como `Int` (por ejemplo
 * `isVip()I` de las configuraciones del teclado Rizz).
 *
 * @param value Valor que devolvera el metodo (entre -8 y 7, que es lo que cabe en `const/4`).
 * @param surface Etiqueta corta que se escribe en logcat (solo ASCII).
 * @return `true` si se ha modificado al menos un metodo.
 */
internal fun Fingerprint.forceInt(
    value: Int,
    surface: String,
    extensionMethod: String = "logBlocked",
): Boolean {
    require(value in -8..7) { "forceInt solo admite valores que quepan en const/4" }

    var patched = false

    matchAllOrNull().orEmpty().forEach { match ->
        val original = match.originalMethod

        if (original.returnType != "I") return@forEach

        val implementation = original.implementation ?: return@forEach

        if (implementation.registerCount < 1) return@forEach

        match.method.addInstructions(
            0,
            """
                const-string v0, "$surface"
                invoke-static {v0}, $EXTENSION_CLASS->$extensionMethod(Ljava/lang/String;)V
                const/4 v0, $value
                return v0
            """,
        )

        patched = true
    }

    return patched
}

/**
 * Hace que todos los metodos que coincidan con esta huella devuelvan `java.lang.Boolean`.
 *
 * Se usa en los envoltorios que devuelven el objeto en vez del primitivo
 * (por ejemplo `getVipIsUnLock()Ljava/lang/Boolean;`).
 *
 * @param value Valor que devolvera el metodo.
 * @param surface Etiqueta corta que se escribe en logcat (solo ASCII).
 * @return `true` si se ha modificado al menos un metodo.
 */
internal fun Fingerprint.forceBooleanObject(
    value: Boolean,
    surface: String,
    extensionMethod: String = "logBlocked",
): Boolean {
    var patched = false
    val campo = if (value) "TRUE" else "FALSE"

    matchAllOrNull().orEmpty().forEach { match ->
        val original = match.originalMethod

        if (original.returnType != "Ljava/lang/Boolean;") return@forEach

        val implementation = original.implementation ?: return@forEach

        if (implementation.registerCount < 1) return@forEach

        match.method.addInstructions(
            0,
            """
                const-string v0, "$surface"
                invoke-static {v0}, $EXTENSION_CLASS->$extensionMethod(Ljava/lang/String;)V
                sget-object v0, Ljava/lang/Boolean;->$campo:Ljava/lang/Boolean;
                return-object v0
            """,
        )

        patched = true
    }

    return patched
}
