package com.ks.tvlauncher.domain

/** The drawer's category filter. Apps whose category maps to none of these appear under All only. */
enum class AppCategory { VIDEO, MUSIC, GAMES, TOOLS }

/** One launchable app, as discovered on the device. */
data class AppInfo(
    val packageName: String,
    /** The activity that launches it (the leanback one when the app has both). */
    val activityName: String,
    val label: String,
    /** Part of the artwork cache key, so an update refreshes the banner. */
    val versionCode: Long,
    val category: AppCategory?,
    /** True when the app ships a TV banner; otherwise its icon is drawn on a frosted tile. */
    val hasBanner: Boolean,
)
