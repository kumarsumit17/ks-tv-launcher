package com.ks.tvlauncher.data.wallpaper

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.core.graphics.scale
import coil3.ImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.ks.tvlauncher.domain.Wallpapers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * The user's wallpaper picture (docs/spec/custom-wallpaper.md). A picked image is decoded once,
 * downscaled to fit 1920×1080 and copied to `filesDir/wallpaper.jpg`; KS keeps no permission on
 * the original and never reads it again. The copy lives in app storage, so it survives updates
 * and is removed on uninstall.
 */
class WallpaperPictureStore(context: Context, private val imageLoader: ImageLoader) {

    private val appContext = context.applicationContext
    private val file: File get() = File(appContext.filesDir, FILE_NAME)

    fun exists(): Boolean = file.isFile && file.length() > 0

    /**
     * True if the TV has a document picker for images. Android TV's placeholder package
     * (`frameworkpackagestubs`) answers the intent but only says there is no app, so it does
     * not count.
     */
    fun canPickFiles(): Boolean = try {
        appContext.packageManager
            .queryIntentActivities(pickIntent(), PackageManager.MATCH_DEFAULT_ONLY)
            .any { it.activityInfo?.packageName?.let { pkg -> pkg != STUB_PACKAGE } == true }
    } catch (e: RuntimeException) {
        false
    }

    /**
     * Copies [uri] into app storage. False if the image cannot be read; the previous copy
     * stays until a new one has been written in full.
     */
    suspend fun import(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        val bitmap = try {
            decode(uri)
        } catch (e: IOException) {
            null
        } catch (e: SecurityException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        } catch (e: OutOfMemoryError) {
            null
        } ?: return@withContext false
        val temp = File(appContext.filesDir, "$FILE_NAME.tmp")
        try {
            temp.outputStream().use { out ->
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)) return@withContext false
            }
            temp.renameTo(file)
        } catch (e: IOException) {
            false
        } finally {
            temp.delete()
            bitmap.recycle()
        }
    }

    /** "Remove image": deletes the copy. */
    suspend fun remove() {
        withContext(Dispatchers.IO) { file.delete() }
    }

    /**
     * Loads the stored picture once, through Coil, at screen size (at most 1920×1080). Null if
     * the file is missing or unreadable. Not kept in Coil's memory cache: Home holds the one
     * copy while it is visible.
     */
    suspend fun load(widthPx: Int, heightPx: Int): Bitmap? {
        if (!exists()) return null
        val (w, h) = Wallpapers.fitWithin(widthPx.coerceAtLeast(1), heightPx.coerceAtLeast(1))
        val request = ImageRequest.Builder(appContext)
            .data(file)
            .size(w, h)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .allowHardware(true)
            .build()
        val result = imageLoader.execute(request) as? SuccessResult ?: return null
        return result.image.toBitmap()
    }

    /** Decodes [uri] already scaled to fit 1920×1080, applying the image's own rotation. */
    private fun decode(uri: Uri): Bitmap? {
        val resolver = appContext.contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(resolver, uri)
            return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                val (w, h) = Wallpapers.fitWithin(info.size.width, info.size.height)
                decoder.setTargetSize(w, h)
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        }
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) } ?: return null
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val (w, h) = Wallpapers.fitWithin(bounds.outWidth, bounds.outHeight)
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= w && bounds.outHeight / (sample * 2) >= h) sample *= 2
        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null
        if (decoded.width == w && decoded.height == h) return decoded
        return decoded.scale(w, h).also { if (it !== decoded) decoded.recycle() }
    }

    companion object {
        private const val FILE_NAME = "wallpaper.jpg"
        private const val JPEG_QUALITY = 90
        private const val STUB_PACKAGE = "com.android.tv.frameworkpackagestubs"

        /** The system document picker, images only. KS takes no lasting permission on the result. */
        fun pickIntent(): Intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("image/*")
    }
}
