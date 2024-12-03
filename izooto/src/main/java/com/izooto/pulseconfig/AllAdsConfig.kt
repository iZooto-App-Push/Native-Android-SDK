package com.izooto.pulseconfig

// Banner Ads Configuration
class BannerAdsConfig(// Getters
    val adUnitID: String, val isStatus: Boolean, val position: Int
)

// Interstitial Ads Configuration
class InterstitialAdsConfig(// Getters
    val adUnitID: String, val isStatus: Boolean, val position: Int
)

// Reward Ads Configuration
class RewardAdsConfig(// Getters
    val adUnitID: String, val isStatus: Boolean, val position: Int, val elapseTime: String
)

// Native Ads Configuration
class NativeAdsConfig(// Getters
    val adUnitID: String, val isStatus: Boolean, val position: Int
)

// outbrain Ads Configuration
data class OutbrainAdsConfig(
    var status: Boolean, var url: String, var position: Int, var impression: Boolean
)