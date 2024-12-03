package com.izooto.pulseconfig;

// Model for Ad Configuration
public class PulseAdConfiguration {
    private BannerAdsConfig bannerAds;
    private InterstitialAdsConfig interstitialAds;
    private RewardAdsConfig rewardAds;
    private NativeAdsConfig nativeAds;
    // Constructor
    public PulseAdConfiguration(BannerAdsConfig bannerAds, InterstitialAdsConfig interstitialAds, RewardAdsConfig rewardAds, NativeAdsConfig nativeAds) {
        this.bannerAds = bannerAds;
        this.interstitialAds = interstitialAds;
        this.rewardAds = rewardAds;
        this.nativeAds = nativeAds;
    }
    public BannerAdsConfig getBannerAds() {
        return bannerAds;
    }

    public void setBannerAds(BannerAdsConfig bannerAds) {
        this.bannerAds = bannerAds;
    }

    public InterstitialAdsConfig getInterstitialAds() {
        return interstitialAds;
    }

    public void setInterstitialAds(InterstitialAdsConfig interstitialAds) {
        this.interstitialAds = interstitialAds;
    }

    public RewardAdsConfig getRewardAds() {
        return rewardAds;
    }

    public void setRewardAds(RewardAdsConfig rewardAds) {
        this.rewardAds = rewardAds;
    }

    public NativeAdsConfig getNativeAds() {
        return nativeAds;
    }

    public void setNativeAds(NativeAdsConfig nativeAds) {
        this.nativeAds = nativeAds;
    }
}