package com.izooto.feature.pulseweb


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.izooto.AppConstant
import com.izooto.PulseFetchData
import com.izooto.Util
import com.izooto.pulseconfig.PulseAdConfiguration
import com.izooto.pulseconfig.PulseManagerData
import com.izooto.pulseconfig.PulseMargin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


internal class PulseHandler : PWInterface {

    private var context: Context? = null
    private var isLoading = false
    private lateinit var pulseViewAdapter: PulseViewAdapter
    private var dataList: MutableList<Any?> = ArrayList()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob()) // CoroutineScope
    private val handler = Handler(Looper.getMainLooper())
    private var scrollRunnable: Runnable? = null
    private var adConfDuplicate: PulseAdConfiguration? = null


    override fun addConfiguration(context: Context?, pwUrl: String?) {
        requireNotNull(context) { "Context cannot be null" }
        requireNotNull(pwUrl) { "URL cannot be null" }
        this.context = context
    }

    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun createScrollView(
        scrollView: ScrollView,
        layout: LinearLayout?,
        shouldShowProgressBar: Boolean,

        ) {
        try {
            Thread.sleep(2000)
            val pulseData = PulseManagerData.getInstance().pulseData
            val adsConfig = pulseData.pulse.adConf
            // Safely create and add the label
            val label = createAndReturnLabel(
                context,
                pulseData.pulse.label.text,
                pulseData.pulse.label.isStatus,
                pulseData.pulse.label.color,
                pulseData.pulse.label.margin,
                pulseData.pulse.label.size,
                pulseData.pulse.label.alignment
            )
            // Ensure context and layout are not null before adding the label
            if (label != null) {
                context?.let { ctx ->
                    layout?.addView(label) ?: run {
                        Log.e("PulseHandler", "Layout is null, cannot add the label")
                    }
                } ?: run {
                    Log.e("PulseHandler", "Context is null, cannot create label")
                }
            } else {
                Log.e("PulseHandler", "Label creation returned null")
            }
            // add progress bar
            // Check for null context and layout before adding the progress bar
            val progressBar = if (shouldShowProgressBar) {
                // Ensure context is not null
                context?.let { ctx ->
                    // Create progress bar and check if layout is not null
                    createAndReturnProgressbar(ctx, shouldShowProgressBar)?.also { pb ->
                        layout?.addView(pb) // Only add to layout if layout is not null
                    }
                } ?: run {
                    // Handle the case where context is null (optional)
                    Log.e("PulseHandler", "Context is null, cannot create ProgressBar")
                    null // Return null if context is null
                }
            } else {
                null // Return null if shouldShowProgressBar is false
            }
            val bottomProgressbar = createAndReturnProgressbar(context, true)
            bottomProgressbar?.visibility = View.GONE

            // Initialize the RecyclerView safely
            val recyclerView = context?.let { ctx ->
                RecyclerView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT // Start with wrap_content
                    )
                    layoutManager = LinearLayoutManager(ctx)
                    setHasFixedSize(true)
                }
            } ?: run {
                // Handle the case where context is null (optional)
                Log.e("PulseHandler", "Context is null, cannot initialize RecyclerView")
                return // Or handle accordingly, like throwing an exception
            }
            // Load initial data asynchronously
            scope.launch(Dispatchers.IO) {
                loadInitialData(context!!, progressBar, pulseData.pulse.url)
            }

            // Safe initialization of pulseViewAdapter
            context?.let { ctx ->
                pulseViewAdapter = PulseViewAdapter(
                    ctx,
                    dataList,
                    adsConfig,
                    hasAdMobLibrary()
                )
            } ?: run {
                Log.e("PulseHandler", "Context is null, cannot initialize PulseViewAdapter")
                return // or throw IllegalStateException("Context cannot be null")
            }

            recyclerView.adapter = pulseViewAdapter
            layout?.addView(recyclerView)

            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    // Check if we've reached the bottom of the RecyclerView
                    if (!recyclerView.canScrollVertically(1)) {
                        // Load more data

                        scope.launch(Dispatchers.IO) {
                            if (bottomProgressbar != null) {
                                loadMoreData(
                                    context,
                                    pulseData.pulse.url,
                                    bottomProgressbar,
                                )
                            }
                        }

                    }
                }
            })

        } catch (e: IllegalStateException) {
            System.err.println("PulseData  has not been initialized.")
        }

    }

    override fun createCoordinateView(
        context: Context?,
        layout: NestedScrollView?,
        mainLayout: LinearLayout?,
        shouldShowProgressBar: Boolean
    ) {
        if (context == null || layout == null || mainLayout == null) {
            return
        }
        try {
            this.context = context
            val pulseData = PulseManagerData.getInstance().pulseData
            // Log.e("urls", pulseData.pulse.url)

            if (pulseData.pulse.url == null || pulseData.pulse.url.equals("") || pulseData.pulse.url.isEmpty()) {
                return
            }

            if (pulseData.pulse.isStatus) {
                val label: TextView? = createAndReturnLabel(
                    context,
                    pulseData.pulse.label.text,
                    pulseData.pulse.label.isStatus,
                    pulseData.pulse.label.color,
                    pulseData.pulse.label.margin,
                    pulseData.pulse.label.size,
                    pulseData.pulse.label.alignment
                )  // Safely create and add the label
                label?.let { mainLayout.addView(it) }

                val progressBar: ProgressBar? = createAndReturnProgressbar(
                    context,
                    shouldShowProgressBar
                )  // Safely create and add the progress bar
                progressBar?.let { mainLayout.addView(it) }

                scope.launch(Dispatchers.IO) {
                    try {
                        loadInitialData(
                            context,
                            progressBar,
                            pulseData.pulse.url
                        )  // To load initial data
                    } catch (e: Exception) {
                        Log.d(AppConstant.APP_NAME_TAG, "Failed to load initial data: $e")
                    }
                }

                val recyclerView: RecyclerView? = createAndReturnRecyclerView(
                    context,
                    dataList,
                    pulseData.pulse.adConf
                )  // Safely create and add the RecyclerView)
                recyclerView?.let {
                    mainLayout.addView(recyclerView)
                }

                val bottomProgressBar = createAndReturnProgressbar(context, true)
                bottomProgressBar?.let {
                    it.visibility = View.GONE
                    mainLayout.addView(bottomProgressBar)
                }
                layout.setOnScrollChangeListener { v: NestedScrollView, _, scrollY, _, _ ->
                    try {
                        val view = v.getChildAt(v.childCount - 1)
                        val diff =
                            view.bottom - (v.height + scrollY) // Check if NestedScrollView has reached the bottom
                        val threshold = view.height * 0.05

                        if (diff <= threshold && !isLoading && PulseFetchData.fetchPageIndex() <= 5) {
                            try {
                                bottomProgressBar?.visibility = View.VISIBLE
                                scrollRunnable?.let { handler.removeCallbacks(it) }

                                // Schedule a new task to load more data after a brief delay
                                scrollRunnable = Runnable {
                                    scope.launch(Dispatchers.IO) {
                                        try {
                                            loadMoreData(
                                                context,
                                                pulseData.pulse.url,
                                                bottomProgressBar
                                            )
                                        } catch (e: Exception) {
                                            Log.d(
                                                AppConstant.APP_NAME_TAG,
                                                "Error loading more data: ${e.message}"
                                            )
                                        }
                                    }
                                }
                                handler.postDelayed(
                                    scrollRunnable!!,
                                    200
                                )  // Debounce delay of 200ms
                            } catch (ex: Exception) {
                                Log.d(AppConstant.APP_NAME_TAG, "Error handling scroll: $ex")
                            }
                        }
                    } catch (ex: Exception) {
                        Log.d(AppConstant.APP_NAME_TAG, "Error handling scroll: $ex")
                    }
                }
            } else {
                Log.d(
                    AppConstant.APP_NAME_TAG,
                    "Pulse feature is disabled in the SDK. Please contact the support team for assistance."
                );
            }
        } catch (e: Exception) {
            Util.handleExceptionOnce(context, e.toString(), "PulseHandler", "createCoordinateView")
        }
    }

    // To load initial data
    @SuppressLint("NotifyDataSetChanged")
    private suspend fun loadInitialData(
        context: Context?,
        progressBar: ProgressBar?,
        pulseUrl: String?
    ) {
        if (context == null || pulseUrl.isNullOrEmpty()) {
            Log.d(AppConstant.APP_NAME_TAG, "Context or pulseUrl is null, unable to load data.")
            return
        }

        try {
            // Fetch data on a background thread
            val newItems = withContext(Dispatchers.IO) {
                PulseFetchData.returnPulseResponse(context, pulseUrl, false)
            }

            // Update the UI on the main thread
            withContext(Dispatchers.Main) {
                if (!newItems.isNullOrEmpty()) {
                    // Add ads and calculate start position
                    val mixed = occupyAdsViewList(newItems, adConfDuplicate!!.bannerAds.position)
                    dataList.addAll(mixed)
                    // Notify adapter for newly inserted items
                    pulseViewAdapter.notifyDataSetChanged()
                    // Handle sponsored content
                    PulseFetchData.documentData?.let { document ->
                        addSponsoredContent(document)
                    } ?: Log.v(AppConstant.APP_NAME_TAG, "")
                } else {
                    Log.d(AppConstant.APP_NAME_TAG, "No new items to display.")
                }
            }
        } catch (e: Exception) {
            Log.e(AppConstant.APP_NAME_TAG, "Failed to load initial data: ${e.message}")
        } finally {
            // Hide the progress bar
            withContext(Dispatchers.Main) {
                progressBar?.visibility = View.GONE
            }
        }
    }

    // Function to dynamically adjust the height of the RecyclerView
    @SuppressLint("NotifyDataSetChanged")
    private suspend fun loadMoreData(
        context: Context?,
        pulseURL: String?,
        bottomProgressBar: ProgressBar?
    ) = withContext(Dispatchers.Main) {
        if (context == null || pulseURL.isNullOrEmpty()) {
            Log.d(AppConstant.APP_NAME_TAG, "Invalid context or pulseURL.")
            return@withContext
        }

        try {
            bottomProgressBar?.visibility = View.VISIBLE
            isLoading = true

            val newData = withContext(Dispatchers.IO) {
                PulseFetchData.returnPulseResponse(context, pulseURL, true)
            }

            if (newData.isNullOrEmpty()) {
                Log.d(AppConstant.APP_NAME_TAG, "No more data to load.")
                bottomProgressBar?.visibility = View.GONE
                isLoading = false
                return@withContext
            }

            // Add ads and determine the start position
            val mixed = occupyAdsViewList(newData, adConfDuplicate!!.bannerAds.position)
            dataList.addAll(mixed)

            // Update the adapter
            pulseViewAdapter.notifyDataSetChanged()
            // Handle Sponsored Content
            PulseFetchData.documentData?.let { document ->
                addSponsoredContent(document)
            } ?: Log.i(AppConstant.APP_NAME_TAG, "No ad items to display!")

        } catch (e: Exception) {
            Log.e(AppConstant.APP_NAME_TAG, "Error loading more data: ${e.message}")
            Util.handleExceptionOnce(
                context,
                "Error loading more data: ${e.message}",
                "PulseHandler",
                "loadMoreData"
            )
        } finally {
            bottomProgressBar?.visibility = View.GONE
            isLoading = false
        }
    }

    /**
     * Helper method to add sponsored content at the appropriate position.
     */
    private fun addSponsoredContent(document: Documents) {
        pulseViewAdapter.triggerSponsoredToPosition(
            dataList,
            Article(
                title = document.content,
                link = document.landingUrl,
                bannerImage = document.thumbnailUrl,
                time = "",
                publisherName = document.publisherName,
                publisherIcon = document.thumbnailUrl,
                isSponsored = true
            ),
            PulseFetchData.outbrainAdsConfig.position
        )
    }

    private fun createCardView(context: Context?, titleMargin: Int): CardView? {
        // Check if context is null
        if (context == null) {
            Log.e("PulseHandler", "Context is null, cannot create CardView")
            return null // Return null if context is null
        }

        // Create and return CardView if context is not null
        return CardView(context).apply {
            radius = 10f
            setCardBackgroundColor(Color.WHITE)
            cardElevation = 8f
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                setMargins(titleMargin, titleMargin, titleMargin, titleMargin)
            }
        }
    }

    // Add banner ads
    private suspend fun loadBannerAds(index: Int) = withContext(Dispatchers.Main) {
        loadBannerAd(index)
    }

    private fun loadBannerAd(index: Int) {
        if (index >= dataList.size) return
        val item = dataList[index]
        if (item is AdView) {
            item.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    loadBannerAd(index + 3)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(
                        "PulseHandler",
                        "Banner ad failed at index $index: ${loadAdError.message}"
                    )

                    // loadBannerAd(index + 3)
                }
            }
            item.loadAd(AdRequest.Builder().build())
        } else {
            // loadBannerAd(index + 3)
        }
    }



    private fun getAdWidth(): Int {
        // Ensure context is not null and is an Activity
        val activity = context as? Activity
            ?: return 320 // Return a default width if context is not an Activity

        val displayMetrics = activity.resources.displayMetrics
        val adWidthPixels = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Get the width using currentWindowMetrics for Android R and above
            activity.windowManager.currentWindowMetrics.bounds.width()
        } else {
            // Fallback to displayMetrics for lower API levels
            displayMetrics.widthPixels
        }

        // Ensure adWidthPixels is not zero before conversion
        return if (adWidthPixels > 0) {
            (adWidthPixels / displayMetrics.density).toInt()
        } else {
            320 // Return a default width if the calculation yields zero
        }
    }

    // To dynamically create the label
    private fun createAndReturnLabel(
        context: Context?,
        pulseTitle: String,
        titleEnable: Boolean,
        titleColor: String,
        titleMargin: PulseMargin,
        titleSize: Int,
        titlePosition: String
    ): TextView? {
        if (context == null || !titleEnable) {
            return null
        }
        // To Manage the color of the title
        val color = try {
            Color.parseColor(titleColor)
        } catch (e: Exception) {
            Log.d(
                AppConstant.APP_NAME_TAG,
                "Encountered an invalid color format: $titleColor. Falling back to default color: black."
            )
            Color.BLACK
        }
        // To create the title
        return try {
            TextView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = when (titlePosition) {
                        "right" -> Gravity.END
                        "center" -> Gravity.CENTER
                        else -> Gravity.START
                    }
                    setMargins(
                        maxOf(0, titleMargin.left),
                        maxOf(0, titleMargin.top),
                        maxOf(0, titleMargin.right),
                        maxOf(0, titleMargin.bottom)
                    )
                }
                text = pulseTitle
                setTextColor(color)
                setTypeface(null, Typeface.BOLD)
                textSize = maxOf(0f, titleSize.toFloat())
            }
        } catch (ex: Exception) {
            Log.d(AppConstant.APP_NAME_TAG, "Error creating TextView: $ex")
            null
        }
    }

    // To dynamically create the progress bar
    private fun createAndReturnProgressbar(context: Context?, isProgress: Boolean): ProgressBar? {
        if (context == null || !isProgress) {
            Log.d(AppConstant.APP_NAME_TAG, "Context is null or ProgressBar is disabled.")
            return null
        }
        return try {
            ProgressBar(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    indeterminateTintList = ColorStateList.valueOf(Color.LTGRAY)
                    setBackgroundColor(Color.TRANSPARENT)
                }
                isIndeterminate = true
            }
        } catch (ex: Exception) {
            null
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun createAndReturnRecyclerView(
        context: Context?,
        dataList: MutableList<Any?>,
        adConf: PulseAdConfiguration
    ): RecyclerView? {
        if (context == null) {
            Log.d(AppConstant.APP_NAME_TAG, "Context is null. Cannot create RecyclerView.")
            return null
        }
        return try {
            adConfDuplicate = adConf
            RecyclerView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                // Use a LinearLayoutManager with prefetch optimization
                val layoutManager = LinearLayoutManager(context).apply {
                    isItemPrefetchEnabled = true
                    initialPrefetchItemCount = 10 // Prefetch 10 items for smoother scrolling
                }
                this.layoutManager = layoutManager

                // Instantiate adapter with data and configuration
                pulseViewAdapter = PulseViewAdapter(context, dataList, adConf, hasAdMobLibrary())
                adapter = pulseViewAdapter

                // RecyclerView performance optimizations
                setHasFixedSize(true) // Optimizes layout recalculations
                setItemViewCacheSize(20) // Cache 20 views for smoother scrolling
                isNestedScrollingEnabled = false // Avoid nested scroll conflicts
                itemAnimator = null // Disable animations for better performance

                // Notify the adapter if required
                pulseViewAdapter.notifyDataSetChanged()

            }
        } catch (ex: Exception) {
            Log.d(AppConstant.APP_NAME_TAG, "Error creating RecyclerView: $ex")
            null
        }
    }

    // To check if AdMob library is available
    private fun hasAdMobLibrary(): Boolean {
        return try {
            Class.forName("com.google.android.gms.ads.AdView")
            true
        } catch (e: ClassNotFoundException) {
            Log.d(AppConstant.APP_NAME_TAG, "AdMob library not found. AdView class is missing.")
            false
        }
    }

    private fun occupyAdsViewList(
        articles: MutableList<Any?>,
        adInterval: Int
    ): MutableList<Any?> {
        if (adConfDuplicate != null && adConfDuplicate!!.bannerAds.isStatus && hasAdMobLibrary()) {
            val mixedList = mutableListOf<Any?>()
            articles.forEachIndexed { index, article ->
                mixedList.add(article)
                if ((index + 1) % adInterval == 0) {
                    mixedList.add(AppConstant.IZ_BANNER) // Use a placeholder for ads
                }
            }
            return mixedList
        } else {
            return articles
        }

    }

}

