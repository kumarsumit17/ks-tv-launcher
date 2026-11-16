package com.ks.tvlauncher.domain

/** Rules for the ordered list of pinned package names. Pure functions; nothing is stored here. */
object PinnedApps {

    /**
     * First-run defaults, in this order: Netflix, Prime Video, YouTube, JioHotstar, Spotify,
     * Plex, Kodi. Each entry lists that app's package names, TV edition first; the first one
     * installed is pinned. Every package name was checked against the Play Store.
     */
    val DEFAULT_CANDIDATES: List<List<String>> = listOf(
        listOf("com.netflix.ninja", "com.netflix.mediaclient"),
        listOf("com.amazon.amazonvideo.livingroom", "com.amazon.avod.thirdpartyclient"),
        listOf("com.google.android.youtube.tv", "com.google.android.youtube"),
        listOf("in.startv.hotstar"),
        listOf("com.spotify.tv.android", "com.spotify.music"),
        listOf("com.plexapp.android"),
        listOf("org.xbmc.kodi"),
    )

    /** The default pins for a device with these packages installed. */
    fun defaults(installed: Set<String>): List<String> =
        DEFAULT_CANDIDATES.mapNotNull { candidates -> candidates.firstOrNull { it in installed } }

    /** Drops blanks and duplicates, keeping the first occurrence and the order. */
    fun normalize(pinned: List<String>): List<String> =
        pinned.map { it.trim() }.filter { it.isNotEmpty() }.distinct()

    /** Removes packages that are no longer installed. */
    fun prune(pinned: List<String>, installed: Set<String>): List<String> =
        pinned.filter { it in installed }

    /** Adds the package at the end; a package that is already pinned stays where it is. */
    fun pin(pinned: List<String>, packageName: String): List<String> =
        if (packageName in pinned) pinned else pinned + packageName

    fun unpin(pinned: List<String>, packageName: String): List<String> =
        pinned.filterNot { it == packageName }

    /** Moves the package [delta] places (negative is left), clamped to the ends of the list. */
    fun move(pinned: List<String>, packageName: String, delta: Int): List<String> {
        val from = pinned.indexOf(packageName)
        if (from < 0) return pinned
        val to = (from + delta).coerceIn(0, pinned.lastIndex)
        if (to == from) return pinned
        return pinned.toMutableList().apply {
            removeAt(from)
            add(to, packageName)
        }
    }

    fun canMove(pinned: List<String>, packageName: String, delta: Int): Boolean {
        val from = pinned.indexOf(packageName)
        return from >= 0 && from + delta in pinned.indices
    }
}
