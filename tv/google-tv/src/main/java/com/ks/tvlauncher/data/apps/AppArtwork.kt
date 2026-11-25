package com.ks.tvlauncher.data.apps

import android.content.ComponentName
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.key.Keyer
import coil3.request.Options
import coil3.size.Dimension
import com.ks.tvlauncher.domain.AppInfo

/** Coil model for an app's own artwork: its TV banner, or its icon when it has none. */
data class AppArtwork(
    val packageName: String,
    val activityName: String,
    val versionCode: Long,
    val banner: Boolean,
)

fun AppInfo.artwork(): AppArtwork = AppArtwork(packageName, activityName, versionCode, hasBanner)

/** Memory-cache key: package + versionCode, so an app update refreshes its artwork. */
class AppArtworkKeyer : Keyer<AppArtwork> {
    override fun key(data: AppArtwork, options: Options): String =
        "app:${data.packageName}:${data.versionCode}:${if (data.banner) "banner" else "icon"}"
}

/**
 * Loads the artwork from `PackageManager` and draws it into a bitmap of the requested size.
 * Coil runs fetchers off the main thread, and nothing is written to disk: it is local data.
 */
class AppArtworkFetcher(
    private val data: AppArtwork,
    private val options: Options,
) : Fetcher {

    override suspend fun fetch(): FetchResult {
        val pm = options.context.packageManager
        val drawable = load(pm)
        val (width, height) = targetSize(drawable)
        val bitmap = drawable.toBitmap(width, height, Bitmap.Config.ARGB_8888)
        return ImageFetchResult(image = bitmap.asImage(), isSampled = true, dataSource = DataSource.DISK)
    }

    private fun load(pm: PackageManager): Drawable {
        val activity = try {
            pm.getActivityInfo(ComponentName(data.packageName, data.activityName), 0)
        } catch (e: PackageManager.NameNotFoundException) {
            throw IllegalStateException("${data.packageName} is no longer installed", e)
        }
        if (data.banner) {
            activity.loadBanner(pm)?.let { return it }
            activity.applicationInfo.loadBanner(pm)?.let { return it }
        }
        return activity.loadIcon(pm)
    }

    /** The requested size, never larger than [MAX_WIDTH]x[MAX_HEIGHT]; icons are square. */
    private fun targetSize(drawable: Drawable): Pair<Int, Int> {
        val requestedWidth = (options.size.width as? Dimension.Pixels)?.px
        val requestedHeight = (options.size.height as? Dimension.Pixels)?.px
        return if (data.banner) {
            val width = (requestedWidth ?: drawable.intrinsicWidth.takeIf { it > 0 } ?: DEFAULT_BANNER_WIDTH)
                .coerceIn(1, MAX_WIDTH)
            val height = (requestedHeight ?: drawable.intrinsicHeight.takeIf { it > 0 } ?: DEFAULT_BANNER_HEIGHT)
                .coerceIn(1, MAX_HEIGHT)
            width to height
        } else {
            val side = listOfNotNull(requestedWidth, requestedHeight).minOrNull()
                ?: drawable.intrinsicWidth.takeIf { it > 0 }
                ?: DEFAULT_ICON_SIZE
            val clamped = side.coerceIn(1, MAX_ICON_SIZE)
            clamped to clamped
        }
    }

    class Factory : Fetcher.Factory<AppArtwork> {
        override fun create(data: AppArtwork, options: Options, imageLoader: ImageLoader): Fetcher =
            AppArtworkFetcher(data, options)
    }

    private companion object {
        const val DEFAULT_BANNER_WIDTH = 320
        const val DEFAULT_BANNER_HEIGHT = 180
        const val DEFAULT_ICON_SIZE = 192
        const val MAX_WIDTH = 640
        const val MAX_HEIGHT = 360
        const val MAX_ICON_SIZE = 256
    }
}
