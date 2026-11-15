package app.parchefacemoji.patches.facemoji.ads

import app.morphe.patcher.Fingerprint

/**
 * Puntos de entrada de las redes publicitarias que Facemoji puede incluir.
 *
 * Se apunta a la API publica de cada SDK (no al codigo ofuscado de Facemoji) porque los
 * SDK de terceros se publican con reglas de ofuscacion que conservan esos nombres entre
 * versiones. Eso hace que estas huellas sobrevivan a las actualizaciones de la app.
 *
 * Segun los metadatos publicos de la app sus rastreadores y librerias incluyen
 * Google AdMob (red principal), Facebook y AppsFlyer. Aun asi se declaran tambien las
 * redes de mediacion mas habituales: si no estan, la huella simplemente no coincide.
 */

/**
 * Anuncios de banner y rectangulo (AdMob).
 *
 * `AdView` hereda `loadAd` de `BaseAdView`, por lo que el punto de llamada real es el
 * de la clase base. Cubre todas las sobrecargas (`AdRequest` y `AdManagerAdRequest`).
 */
internal object AdMobBannerFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/BaseAdView;",
    name = "loadAd",
    returnType = "V",
)

/** Anuncios nativos y personalizados (AdMob). */
internal object AdMobNativeFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/AdLoader;",
    name = "loadAd",
    returnType = "V",
)

/** Anuncios intersticiales (AdMob). */
internal object AdMobInterstitialFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/interstitial/InterstitialAd;",
    name = "show",
    returnType = "V",
)

/** Anuncios de apertura de app (AdMob), los que aparecen al salir del teclado. */
internal object AdMobAppOpenFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/appopen/AppOpenAd;",
    name = "show",
    returnType = "V",
)

/** Anuncios recompensados (AdMob). */
internal object AdMobRewardedFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/rewarded/RewardedAd;",
    name = "show",
    returnType = "V",
)

/** Anuncios recompensados intersticiales (AdMob). */
internal object AdMobRewardedInterstitialFingerprint : Fingerprint(
    definingClass = "Lcom/google/android/gms/ads/rewardedinterstitial/RewardedInterstitialAd;",
    name = "show",
    returnType = "V",
)

/** Banner de Facebook Audience Network. */
internal object FacebookBannerFingerprint : Fingerprint(
    definingClass = "Lcom/facebook/ads/AdView;",
    name = "loadAd",
    returnType = "V",
)

/** Intersticial de Facebook Audience Network. */
internal object FacebookInterstitialFingerprint : Fingerprint(
    definingClass = "Lcom/facebook/ads/InterstitialAd;",
    name = "show",
    returnType = "V",
)

/** Unity Ads. */
internal object UnityAdsFingerprint : Fingerprint(
    definingClass = "Lcom/unity3d/ads/UnityAds;",
    name = "show",
    returnType = "V",
)

/** AppLovin (banner). */
internal object AppLovinBannerFingerprint : Fingerprint(
    definingClass = "Lcom/applovin/adview/AppLovinAdView;",
    name = "loadNextAd",
    returnType = "V",
)
