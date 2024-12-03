import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener


class RewardedAdManager {

    private var rewardedAd: RewardedAd? = null

    // Load the rewarded ad
    fun loadRewardedAd(
        context: Activity,
        adUnitId: String,
        onAdLoaded: () -> Unit,
        onAdFailedToLoad: (AdError) -> Unit
    ) {
        val adRequest = AdRequest.Builder().build()

        // Load the ad
        RewardedAd.load(context, "ca-app-pub-3940256099942544/5224354917", adRequest, object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewardedAd = ad
                onAdLoaded() // Callback when ad is loaded successfully
                Log.d("RewardedAd", "Ad loaded successfully.")
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                onAdFailedToLoad(adError) // Callback if ad fails to load
                Log.d("RewardedAd", "Ad failed to load: ${adError.message}")
            }
        })
    }

    // Show the rewarded ad
    fun showRewardedAd(
        context: Activity,
        adUnitId: String,
        clickUrl: String,
        onAdFailedToLoad: (AdError) -> Unit,
        onAdLoaded: () -> Unit,
        onRewardEarned: (RewardItem) -> Unit,
        onAdDismissed: () -> Unit
    ) {
        // Load the ad if not already loaded
        if (rewardedAd == null) {
            loadRewardedAd(
                context,
                adUnitId,
                onAdLoaded,
                onAdFailedToLoad
            )
        }

        // Show the rewarded ad if it is loaded
        rewardedAd?.show(context, object : OnUserEarnedRewardListener {
            override fun onUserEarnedReward(reward: RewardItem) {
                // Handle the earned reward here
                onRewardEarned(reward)
                Log.d("RewardedAd", "Reward earned: ${reward.amount} ${reward.type}")
            }
        }) ?: run {
            // If the ad isn't loaded, log failure and fallback to custom URL
           // onAdFailedToLoad(AdError(0, "Ad is not ready to be shown", ""))
            Log.d("RewardedAd", "Ad is not ready to be shown.")
            // Optionally, open the fallback URL if ad fails to show
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(context, Uri.parse(clickUrl))
        }

        // Set up callback for when the ad is dismissed
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                // Called when the ad is dismissed
               // onAdDismissed()
                Log.d("RewardedAd", "Ad was dismissed.")
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                // Called when the ad fails to show
              //  onAdFailedToLoad(adError)
                Log.d("RewardedAd", "Ad failed to show: ${adError.message}")
            }
        }
    }
}

