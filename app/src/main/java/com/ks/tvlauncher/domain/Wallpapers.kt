package com.ks.tvlauncher.domain

/** What Home draws behind everything (docs/spec/custom-wallpaper.md). */
enum class WallpaperMode { DYNAMIC, CUSTOM, PLAIN }

/** Extra darkening over a custom wallpaper, on top of the usual readability overlays. */
enum class WallpaperDim { LOW, MEDIUM, HIGH }

/** The image a CUSTOM wallpaper shows. */
sealed interface WallpaperImage {
    /** One of the [Wallpapers.BUILT_IN_COUNT] wallpapers drawn in code. */
    data class BuiltIn(val id: Int) : WallpaperImage

    /** The user's picture, copied to app storage. [version] changes on every new copy. */
    data class Picture(val version: Long) : WallpaperImage
}

/** Rules for the wallpaper settings. No Android code; unit tested. */
object Wallpapers {

    const val BUILT_IN_COUNT = 6

    /** Largest stored picture; bigger images are downscaled on import. */
    const val MAX_WIDTH = 1920
    const val MAX_HEIGHT = 1080

    val DEFAULT_IMAGE: WallpaperImage = WallpaperImage.BuiltIn(0)

    /**
     * The stored mode; before Stage 6b there was only the dynamic on/off switch, so a missing
     * mode follows it ("off" becomes PLAIN). Unknown values read as the default, DYNAMIC.
     */
    fun mode(stored: String?, legacyDynamic: Boolean?): WallpaperMode =
        stored?.let { saved -> WallpaperMode.entries.firstOrNull { it.name == saved } }
            ?: if (legacyDynamic == false) WallpaperMode.PLAIN else WallpaperMode.DYNAMIC

    fun dim(stored: String?): WallpaperDim =
        stored?.let { saved -> WallpaperDim.entries.firstOrNull { it.name == saved } } ?: WallpaperDim.MEDIUM

    /** "builtin:2" or "picture:1731830000000". */
    fun encode(image: WallpaperImage): String = when (image) {
        is WallpaperImage.BuiltIn -> "builtin:${image.id}"
        is WallpaperImage.Picture -> "picture:${image.version}"
    }

    /** A missing or damaged value reads as the first built-in wallpaper. */
    fun decode(raw: String?): WallpaperImage {
        if (raw == null) return DEFAULT_IMAGE
        val value = raw.substringAfter(':', "")
        return when (raw.substringBefore(':')) {
            "builtin" -> value.toIntOrNull()?.takeIf { it in 0 until BUILT_IN_COUNT }?.let { WallpaperImage.BuiltIn(it) }
            "picture" -> value.toLongOrNull()?.let { WallpaperImage.Picture(it) }
            else -> null
        } ?: DEFAULT_IMAGE
    }

    /** Control Center's Wallpaper row: Dynamic → Custom → Plain → Dynamic. */
    fun next(mode: WallpaperMode): WallpaperMode =
        WallpaperMode.entries[(mode.ordinal + 1) % WallpaperMode.entries.size]

    /** Black overlay strength for each dim level. */
    fun dimAlpha(dim: WallpaperDim): Float = when (dim) {
        WallpaperDim.LOW -> 0.15f
        WallpaperDim.MEDIUM -> 0.35f
        WallpaperDim.HIGH -> 0.55f
    }

    /**
     * The size to store a [width]×[height] image at: its own size if it already fits within
     * [maxWidth]×[maxHeight], else scaled down keeping its shape. Never scaled up; never 0.
     */
    fun fitWithin(width: Int, height: Int, maxWidth: Int = MAX_WIDTH, maxHeight: Int = MAX_HEIGHT): Pair<Int, Int> {
        require(width > 0 && height > 0) { "empty image" }
        if (width <= maxWidth && height <= maxHeight) return width to height
        val scale = minOf(maxWidth.toDouble() / width, maxHeight.toDouble() / height)
        return maxOf(1, Math.round(width * scale).toInt()) to maxOf(1, Math.round(height * scale).toInt())
    }
}
