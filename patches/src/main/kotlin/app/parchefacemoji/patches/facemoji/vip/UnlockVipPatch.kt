package app.parchefacemoji.patches.facemoji.vip

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.patch
import app.parchefacemoji.patches.facemoji.shared.Constants.COMPATIBILITY_FACEMOJI
import app.parchefacemoji.patches.facemoji.shared.forceBoolean
import app.parchefacemoji.patches.facemoji.shared.forceBooleanObject
import java.util.logging.Logger

private val logger = Logger.getLogger("ParcheFacemoji")

/** Contenido que se marca como no VIP para que la app lo deje usar. */
private val VIP_FLAGS: Map<String, Fingerprint> = linkedMapOf(
    "Fuentes de color: es VIP" to CoolFontIsVipFingerprint,
    "Fuentes de color: candado" to CoolFontIsFontLockFingerprint,
    "Fuentes de color: candado o VIP" to CoolFontIsFontStateLockOrVipFingerprint,
    "Fuentes de color: tipo bloqueado" to CoolFontIsLockTypeFingerprint,
    "Stickers: serie VIP" to SeriesStickerIsVipFingerprint,
    "Stickers: serieVIP local" to StickerItemIsSeriesVipFingerprint,
    "TextBomb: es VIP" to TextBombIsVipFingerprint,
    "Rizz: frases VIP" to RizzConfigIsVipFingerprint,
    "Rizz: detalles VIP" to RizzConfigDetailIsVipFingerprint,
    "Rizz: rompehielos VIP" to RizzIceBreakerIsVipFingerprint,
)

/** Contenido que se marca como ya desbloqueado. */
private val VIP_UNLOCKS: Map<String, Fingerprint> = linkedMapOf(
    "Fuentes de color: desbloqueada por video o suscripcion" to CoolFontIsVipUnlockedFingerprint,
    "Fuentes de color: desbloqueo guardado" to CoolFontVipUnlockedObjectFingerprint,
)

/**
 * Desbloquea el contenido que Facemoji reserva a la suscripcion VIP.
 *
 * La app no comprueba la suscripcion en un unico sitio: cada catalogo lleva su propia bandera
 * (`CoolFontBean.isVip`, `SeriesStickerItem.isSeriesVip`, `TextBombBean.isVip`, ...) y la IU
 * la consulta antes de dejar aplicar el contenido. El parche hace que todas esas banderas
 * respondan "esto es gratis" y que los desbloqueos por video se den por hechos.
 *
 * Consecuencias que conviene conocer:
 * - Los iconos de corona y los avisos de "VIP" desaparecen del catalogo.
 * - La app deja de ofrecer el video recompensado para desbloquear una fuente, porque ya la
 *   considera desbloqueada.
 * - El servidor de Facemoji sigue siendo el que sirve el contenido: si algun dia comprueba la
 *   suscripcion al descargar una fuente o un sticker, la descarga puede fallar aunque el
 *   candado ya no se vea. Eso no se puede comprobar sin un dispositivo real.
 */
@Suppress("unused")
val unlockVipPatch = patch(
    name = "Desbloquear VIP",
    description = "Marca como gratuitos las fuentes de color, las series de stickers, los TextBomb " +
            "y las frases del teclado Rizz que la app reserva a la suscripcion, y da por hechos " +
            "los desbloqueos por video.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_FACEMOJI)

    category("Suscripcion")

    extendWith("extensions/facemoji.mpe")

    execute {
        val unlocked = VIP_FLAGS.filter { (surface, fingerprint) ->
            fingerprint.forceBoolean(value = false, surface = "vip: $surface")
        }.keys

        val marked = VIP_UNLOCKS.filter { (surface, fingerprint) ->
            if (fingerprint.returnType == "Ljava/lang/Boolean;") {
                fingerprint.forceBooleanObject(value = true, surface = "vip: $surface")
            } else {
                fingerprint.forceBoolean(value = true, surface = "vip: $surface")
            }
        }.keys

        if (unlocked.isEmpty() && marked.isEmpty()) {
            throw PatchException(
                "No se ha encontrado ninguna comprobacion VIP en esta version de Facemoji. " +
                        "Desactiva este parche o abre una incidencia en " +
                        "https://github.com/Minecraco/Parche-facemoji/issues indicando la version " +
                        "de la app.",
            )
        }

        logger.info("VIP: ${unlocked.size} banderas marcadas como gratuitas y " +
                "${marked.size} desbloqueos dados por hechos")
        logger.info("  gratuitas: ${unlocked.joinToString()}")
        logger.info("  desbloqueadas: ${marked.joinToString()}")
    }
}
