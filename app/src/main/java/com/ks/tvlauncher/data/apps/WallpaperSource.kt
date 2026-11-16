package com.ks.tvlauncher.data.apps

import android.content.Context
import android.graphics.Bitmap
import androidx.palette.graphics.Palette
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import com.ks.tvlauncher.domain.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** A tiny copy of an app's artwork plus its accent colour, for the dynamic wallpaper. */
class Wallpaper(
    val packageName: String,
    /** Deliberately tiny: drawn full screen it is already a soft blur, on every API level. */
    val bitmap: Bitmap,
    val tint: Int,
)

/**
 * Builds the dynamic wallpaper from the app's own artwork, through the same Coil pipeline and
 * memory cache as the tiles. Never uses web URLs or third-party artwork.
 */
class WallpaperSource(context: Context, private val imageLoader: ImageLoader) {

    private val appContext = context.applicationContext

    suspend fun load(app: AppInfo): Wallpaper? {
        val request = ImageRequest.Builder(appContext)
            .data(app.artwork())
            .size(WIDTH, HEIGHT)
            .build()
        val result = imageLoader.execute(request) as? SuccessResult ?: return null
        return withContext(Dispatchers.Default) {
            val bitmap = result.image.toBitmap(WIDTH, HEIGHT)
            val palette = Palette.from(bitmap).maximumColorCount(12).generate()
            val tint = palette.getVibrantColor(palette.getDominantColor(FALLBACK_TINT))
            Wallpaper(app.packageName, bitmap, tint)
        }
    }

    private companion object {
        const val WIDTH = 32
        const val HEIGHT = 18
        const val FALLBACK_TINT = 0xFF16264A.toInt()
    }
}
