package com.izooto.feature.pulseweb

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.LoadAdError

class AdHandler(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null

    // Load and show interstitial ad with a URL redirect after ad is dismissed
    fun loadAndShowInterstitialAd(adUnitId: String, clickUrl: String) {
        val adRequest = AdRequest.Builder().build()

        // Load the interstitial ad
        InterstitialAd.load(context, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad

                // Set the ad listener to handle dismiss event
                interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {

                        // Open the URL using CustomTabsIntent after ad is dismissed
                        val builder = CustomTabsIntent.Builder()
                        val customTabsIntent = builder.build()
                        customTabsIntent.launchUrl(context, Uri.parse(clickUrl))
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        Log.e("AdHandler", "Interstitial ad failed to show: ${adError.message}")
                        interstitialAd = null
                    }

                    override fun onAdShowedFullScreenContent() {
                        Log.e("AdHandler", "Interstitial ad showed")
                        interstitialAd = null
                    }
                }

                // Show the interstitial ad
                interstitialAd?.show(context as Activity)
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                val builder = CustomTabsIntent.Builder()
                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(context, Uri.parse(clickUrl))

                Log.e("AdHandler", "Failed to load interstitial ad: ${loadAdError.message}")
                interstitialAd = null
            }
        })
    }
}
