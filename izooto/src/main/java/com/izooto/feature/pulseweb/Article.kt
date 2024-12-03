package com.izooto.feature.pulseweb

/**
 * The [Article] class.
 *
 * Defines the attributes for a restaurant menu item.
 */
data class Article(
    val title: String,
    val link: String,
    val bannerImage: String,
    val time: String,
    val publisherName: String,
    val publisherIcon: String,
    val isSponsored: Boolean = false
)

