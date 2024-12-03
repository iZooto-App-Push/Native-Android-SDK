package com.izooto.feature.pulseweb

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.izooto.AppConstant
import com.izooto.PulseFetchData
import com.izooto.R
import com.izooto.Util
import com.izooto.pulseconfig.PulseAdConfiguration
import com.izooto.pulseconfig.PulseManagerData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Locale


class PulseViewAdapter(
    private val context: Context,
    private val articleList: MutableList<Any?>,
    private val adsConfig: PulseAdConfiguration,
    private val isMobLibrary: Boolean,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val ITEM_VIEW = 1
    private val adHandler = AdHandler(context)  // Instantiate AdHandler
    private val AD_VIEW = 2
    private val adCache = mutableMapOf<Int, WeakReference<AdView>>()

    inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val newsTitle: TextView = view.findViewById(R.id.nt_title)
        private val bannerImage: ImageView = view.findViewById(R.id.nt_banner_image)
        private val circleIcon: ImageView = view.findViewById(R.id.circle_icon)
        private val dot: ImageView = view.findViewById(R.id.dot_)
        private val publisher: TextView = view.findViewById(R.id.publisher_)
        private val newsHubTime: TextView = view.findViewById(R.id.news_hub_time)
        private val sponsoredText: TextView = itemView.findViewById(R.id.overlay_text)

        @SuppressLint("SetTextI18n")
        @RequiresApi(Build.VERSION_CODES.O)
        fun bindArticles(payload: Article?) {
            try {
                if (payload!!.isSponsored) {
                    sponsoredText.visibility = View.VISIBLE
                    sponsoredText.text = "Sponsored"
                    newsHubTime.visibility = View.GONE
                    dot.visibility = View.GONE
                } else {
                    sponsoredText.visibility = View.GONE
                    newsHubTime.visibility = View.VISIBLE
                    dot.visibility = View.VISIBLE
                }

                if (payload.title.isNotEmpty()) {
                    newsTitle.text = payload.title ?: ""
                    publisher.text = payload.publisherName ?: ""
                    newsHubTime.text = (timeAgo(payload.time)) ?: ""
                    try {
                        val bannerImageUrl = payload.bannerImage.takeIf { it.isNotEmpty() }
                        val publisherIconUrl = payload.publisherIcon.takeIf { it.isNotEmpty() }
                        Glide.with(context)
                            .load(bannerImageUrl)
                            .placeholder(R.drawable.loading_android)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .apply(RequestOptions.bitmapTransform(RoundedCorners(45)))
                            .error(R.drawable.error) // Replace with your fallback image resource
                            .into(bannerImage)

                        Glide.with(context)
                            .load(publisherIconUrl)
                            .placeholder(R.drawable.loading_android)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .circleCrop()
                            .error(payload.bannerImage)
                            .into(circleIcon)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    itemView.setOnClickListener {
                        val clickUrl = updateUtms(
                            payload.link,
                            AppConstant.OLD_MEDIUM,
                            AppConstant.IZ_PULSE
                        )
                        if (clickUrl.isNotEmpty()) {
                            newsHubCheckIaKey(clickUrl, position)
                        }

                    }
                }

            } catch (e: Exception) {
                Log.e("onBindViewHolder", "exception$e")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW -> {
                val view =
                    LayoutInflater.from(context).inflate(R.layout.news_hub_items, parent, false)
                ItemViewHolder(view)
            }

            AD_VIEW -> {
                val adView = LayoutInflater.from(context)
                    .inflate(R.layout.item_adaptive_banner_ad, parent, false)
                AdViewHolder(adView)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        try {
            when (holder) {
                is ItemViewHolder -> holder.bindArticles(articleList[position] as? Article)
                is AdViewHolder -> bindAds(holder, position)
            }

        } catch (e: Exception) {
            Log.e("onBindViewHolder", "exception$e")
        }
    }


    private fun bindAds(holder: AdViewHolder, position: Int) {
        try {
            if (isMobLibrary && adsConfig.bannerAds.isStatus) {
                holder.adContainer.removeAllViews()
                // Reuse cached AdView or create a new one
                val cachedAdView = adCache[position]?.get()
                val adView = cachedAdView ?: AdView(holder.adContainer.context).apply {
                    setAdSize(getAdSizes())
                    adUnitId = adsConfig.bannerAds.adUnitID
                    adCache[position] =
                        WeakReference(this) // Use WeakReference to prevent memory leaks
                }

                holder.adContainer.addView(adView)

                // Only load if AdView is not already loaded
                if (cachedAdView == null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val adRequest = AdRequest.Builder().build()
                        withContext(Dispatchers.Main) {
                            adView.loadAd(adRequest)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("onBindViewHolder", "exception$e")
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        if (holder is AdViewHolder) {
            holder.adContainer.removeAllViews()
        }
    }

    /**
     * Determines the view type for the given position.
     */

    override fun getItemViewType(position: Int): Int {
        return when (articleList[position]) {
            is Article -> ITEM_VIEW
            AppConstant.IZ_BANNER -> AD_VIEW
            else -> throw IllegalArgumentException("Unknown item type at position $position")
        }
    }

    override fun getItemCount(): Int = articleList.size


    fun triggerSponsoredToPosition(newItems: List<Any?>, sponsoredItem: Article, interval: Int) { // List<article>
        // Add only unique items to the list
        try {
            val existingSize = articleList.size // 10
            val filteredNewItems = newItems.filter { it !in articleList }
            articleList.addAll(filteredNewItems) //11
            notifyItemRangeInserted(existingSize, filteredNewItems.size) //11
            // Insert sponsored items dynamically, avoiding duplicates
            insertSponsoredItemsDynamically(sponsoredItem, interval) //11

        } catch (e: Exception) {
            Log.e("onBindViewHolder", "exception$e")
        }
    }

    private fun insertSponsoredItemsDynamically(newItem: Article, interval: Int) {
        try {
            if (interval <= 0 || articleList.isEmpty()) return
            // Remove previously inserted sponsored items to prevent duplicates
            articleList.removeAll { it is Article && it.isSponsored }
            // Calculate positions for sponsored items
            val sponsoredPositions = mutableListOf<Int>()
            var index = interval - 1 // 0-based index for the first interval
            while (index < articleList.size) {
                sponsoredPositions.add(index)
                index += interval
            }

            // Insert sponsored items at the calculated positions
            for (position in sponsoredPositions.reversed()) {
                articleList.add(position, newItem.copy(isSponsored = true))
            }

            // Notify RecyclerView about the inserted items
            for (position in sponsoredPositions) {
                notifyItemInserted(position)
            }
        } catch (e: Exception) {
            Log.e("onBindViewHolder", "exception$e")
        }
    }


    private fun getAdSizes(): AdSize {
        val displayMetrics = context.resources.displayMetrics
        val adWidth = (displayMetrics.widthPixels / displayMetrics.density).toInt()
        return AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(context, adWidth)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun timeAgo(apiTimestamp: String): String {
        val formatter = DateTimeFormatterBuilder()
            .appendPattern("EEE, dd ") // Weekday and day
            .appendText(
                ChronoField.MONTH_OF_YEAR, mapOf(
                    1L to "Jan", 2L to "Feb", 3L to "Mar", 4L to "Apr", 5L to "May",
                    6L to "Jun", 7L to "Jul", 8L to "Aug", 9L to "Sept", 10L to "Oct",
                    11L to "Nov", 12L to "Dec"
                )
            )
            .appendPattern(" yyyy HH:mm:ss ") // Year, time
            .appendOffset("+HHMM", "") // Numeric timezone offset like +0530
            .toFormatter(Locale.ENGLISH)

        // Parse the API timestamp
        return try {
            val eventTime = ZonedDateTime.parse(apiTimestamp, formatter)
            val currentTime = ZonedDateTime.now(ZoneId.of("UTC"))
            val duration = Duration.between(eventTime, currentTime)

            val seconds = duration.seconds
            when {
                seconds < 60 -> "$seconds seconds ago"
                seconds < 3600 -> "${seconds / 60} minutes ago"
                seconds < 86400 -> "${seconds / 3600} hours ago"
                seconds < 604800 -> "${seconds / 86400} days ago"
                seconds < 2419200 -> "${seconds / 604800} weeks ago"
                seconds < 29030400 -> "${seconds / 2419200} months ago"
                else -> "${seconds / 29030400} years ago"
            }
        } catch (e: Exception) {
            "Invalid date format"
        }
    }

    /** The [AdViewHolder] class.  */
    // class AdViewHolder internal constructor(view: View?) : RecyclerView.ViewHolder(view!!)
    class AdViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val adContainer: FrameLayout = view.findViewById(R.id.ad_container)
    }

    /** The [AdViewHolder] class.  */
    //  class AdViewHolder internal constructor(view: View?) : RecyclerView.ViewHolder(view!!)

    private fun updateUtms(
        query: String,
        oldMValue: String,
        newMValue: String
    ): String {
        // Split the query string into key-value pairs
        val params = query.split("&").associate {
            val (key, value) = it.split("=")
            key to value
        }.toMutableMap()

        // Only update utm_medium if its value is oldMediumValue (in this case "newswidget")
        if (params[AppConstant.UTM_MEDIUM] == oldMValue) {
            params[AppConstant.UTM_MEDIUM] = newMValue
        }

        // Append the utm_term parameter with the newUTMTerm value
        params[AppConstant.UTM_TERM] = ""

        // Reassemble the query string
        return params.entries.joinToString("&") { "${it.key}=${it.value}" }
    }

    private fun newsHubCheckIaKey(clickUrl: String?, position: Int) {
        if (clickUrl != null) {
            try {
                val pulseFetchData = PulseManagerData.getInstance().pulseData

                Util.pulseClickAPI(context, pulseFetchData.pulse.cid, pulseFetchData.pulse.rid)

                if (pulseFetchData.pulse.adConf.interstitialAds.isStatus) {
                    if (pulseFetchData.pulse.adConf.interstitialAds.position == position) {
                        adHandler.loadAndShowInterstitialAd(
                            pulseFetchData.pulse.adConf.interstitialAds.adUnitID,
                            clickUrl
                        )
                    } else {
                        // Open the URL using CustomTabsIntent after ad is dismissed
                        val builder = CustomTabsIntent.Builder()
                        val customTabsIntent = builder.build()
                        customTabsIntent.launchUrl(context, Uri.parse(clickUrl))
                    }
                }
                if (pulseFetchData.pulse.adConf.rewardAds.isStatus) {
                    if (pulseFetchData.pulse.adConf.rewardAds.position == position) {
                        AdDescriptionDialog.showAdDescriptionDialog(
                            context,
                            clickUrl,
                            pulseFetchData.pulse.adConf.rewardAds
                        )
                    }

                } else {
                    // Open the URL using CustomTabsIntent after ad is dismissed
                    val builder = CustomTabsIntent.Builder()
                    val customTabsIntent = builder.build()
                    customTabsIntent.launchUrl(context, Uri.parse(clickUrl))
                }
            } catch (e: Exception) {
                Util.handleExceptionOnce(context, e.toString(), "NewsHubAlert", "newsHubCheckIaKey")
            }
        }
    }
}

