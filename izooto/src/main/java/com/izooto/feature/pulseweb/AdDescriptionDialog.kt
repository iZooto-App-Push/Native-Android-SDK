package com.izooto.feature.pulseweb

import RewardedAdManager
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import com.izooto.PulseFetchData
import com.izooto.pulseconfig.RewardAdsConfig

object AdDescriptionDialog {

    fun showAdDescriptionDialog(context: Context,clickUrl : String,rewardAds: RewardAdsConfig) {
        // Create an AlertDialog.Builder
        val builder = AlertDialog.Builder(context)
        
        // Set the dialog title
        builder.setTitle("Watch Ads for Rewards")
        
        // Set the dialog message (description)
        builder.setMessage("Watch an ad to earn rewards. You will get rewarded after watching the entire ad. The reward will be credited to your account immediately after completion.")
        
        // Set the "OK" button
        builder.setPositiveButton("OK") { dialog, _ ->
            try {
                val rewardedAdManager = RewardedAdManager()
                rewardedAdManager.showRewardedAd(
                    context = context as Activity, // Your Activity
                    adUnitId = rewardAds.adUnitID, // Your Ad Unit ID
                    clickUrl = clickUrl,
                    onAdFailedToLoad = { adError ->
                        val builder = CustomTabsIntent.Builder()
                        val customTabsIntent = builder.build()
                        customTabsIntent.launchUrl(context, Uri.parse(clickUrl))
                    },
                    onAdLoaded = {
                        Log.d("MainActivity", "Ad loaded successfully.")
                    },
                    onRewardEarned = { rewardItem ->
                        Log.d("MainActivity", "Reward earned: ${rewardItem.amount} ${rewardItem.type}")
                    },
                    onAdDismissed = {
                        Log.d("MainActivity", "Ad was dismissed.")
                    }
                )

            }
            catch (ex:Exception){
                Log.e("Exception Ex",ex.toString())

            }
            dialog.dismiss()
        }

        // Set the "Cancel" button
        builder.setNegativeButton("Cancel") { dialog, _ ->
            // Handle "Cancel" button click (dismiss the dialog)
            dialog.dismiss()
        }
        
        // Create and show the dialog
        builder.create().show()
    }
}
