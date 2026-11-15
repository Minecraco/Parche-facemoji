package app.parchefacemoji.patches.facemoji.ads

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.patch
import app.parchefacemoji.patches.facemoji.shared.COMPATIBILITY_FACEMOJI
import app.parchefacemoji.patches.facemoji.shared.blockVoidMethods
import java.util.logging.Logger

private val logger = Logger.getLogger("ParcheFacemoji")

/**
 * Superficie publicitaria -> huella que la neutraliza.
 *
 * El orden es solo informativo (se usa para el mensaje de log).
 */
private val AD_SURFACES: Map<String, Fingerprint> = linkedMapOf(
    "AdMob: banner" to AdMobBannerFingerprint,
    "AdMob: nativo" to AdMobNativeFingerprint,
    "AdMob: intersticial" to AdMobInterstitialFingerprint,
    "AdMob: app open" to AdMobAppOpenFingerprint,
    "AdMob: recompensado" to AdMobRewardedFingerprint,
    "AdMob: recompensado intersticial" to AdMobRewardedInterstitialFingerprint,
    "Facebook: banner" to FacebookBannerFingerprint,
    "Facebook: intersticial" to FacebookInterstitialFingerprint,
    "Unity Ads" to UnityAdsFingerprint,
    "AppLovin: banner" to AppLovinBannerFingerprint,
)

/**
 * Bloquea los anuncios interceptando la API publica de cada red publicitaria.
 *
 * Cada anuncio se neutraliza en el momento en el que la app pide cargarlo o mostrarlo:
 * el metodo del SDK termina inmediatamente con `return-void`, por lo que el SDK nunca
 * llega a descargar ni a pintar el anuncio. La app no recibe el callback de error ni de
 * exito, asi que no muestra ningun dialogo: simplemente se queda sin anuncio.
 */
@Suppress("unused")
val removeAdsPatch = patch(
    name = "Eliminar anuncios",
    description = "Bloquea la carga y la visualizacion de los anuncios de AdMob y de las demas " +
            "redes publicitarias que Facemoji pueda incluir.",
    default = true,
) {
    compatibleWith(COMPATIBILITY_FACEMOJI)

    category("Anuncios")

    // Anade las clases de diagnostico (PatchHooks) al DEX de la app.
    extendWith("extensions/facemoji.mpe")

    execute {
        val blocked = AD_SURFACES.filter { (surface, fingerprint) ->
            fingerprint.blockVoidMethods(surface = surface)
        }.keys

        if (blocked.isEmpty()) {
            throw PatchException(
                "No se ha encontrado ninguna red publicitaria conocida en esta version de Facemoji. " +
                        "Es probable que la app haya cambiado de SDK o que esta version aun no este soportada. " +
                        "Desactiva este parche o abre una incidencia en " +
                        "https://github.com/Minecraco/Parche-facemoji/issues indicando la version de la app.",
            )
        }

        logger.info("Anuncios bloqueados (${blocked.size}): ${blocked.joinToString()}")
    }
}
