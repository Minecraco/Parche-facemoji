package verificar

import app.morphe.patcher.Patcher
import app.morphe.patcher.PatcherConfig
import app.morphe.patcher.patch.loadPatchesFromJar
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import java.io.File

/**
 * Aplica el bundle de parches a una APK real y comprueba que el resultado se genera.
 *
 * Esta prueba es la unica forma de detectar, sin un dispositivo, los fallos que solo aparecen
 * al parchear: smali mal formado, saltos a etiquetas inexistentes, manifiesto invalido, etc.
 *
 * Solo se ejecuta si existe la variable de entorno `APK_A_VERIFICAR`, para que
 * `./gradlew build` siga funcionando en cualquier maquina sin necesidad de la APK:
 *
 * ```bash
 * APK_A_VERIFICAR=/ruta/facemoji.apkm ./gradlew test --tests verificar.AplicarParchesTest
 * ```
 */
class AplicarParchesTest {

    @Test
    @EnabledIfEnvironmentVariable(named = "APK_A_VERIFICAR", matches = ".+")
    fun `el bundle se aplica a la APK y genera un APK parcheado`() {
        val rutaApk = System.getenv("APK_A_VERIFICAR")!!
        val apk = File(rutaApk)
        assertTrue(apk.isFile, "No existe la APK: $rutaApk")

        val bundle = File("build/libs").listFiles { fichero ->
            fichero.name.endsWith(".mpp") &&
                    !fichero.name.contains("sources") &&
                    !fichero.name.contains("javadoc")
        }?.firstOrNull() ?: error("No se ha encontrado el .mpp en build/libs")

        val salida = File("build/verificacion/facemoji-parcheado.apk")
        salida.parentFile.mkdirs()

        Patcher(PatcherConfig(apk)).use { patcher ->
            patcher += loadPatchesFromJar(setOf(bundle))

            val resultados = runBlocking { patcher().toList() }
            println("Parches ejecutados: ${resultados.size}")

            patcher.writeApk(salida)
        }

        assertTrue(
            salida.isFile && salida.length() > 0,
            "El parcheo no ha generado ningun APK",
        )
        println("APK parcheado: ${salida.absolutePath} (${salida.length()} bytes)")
    }
}
