package com.ks.tvlauncher.ui.apps

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.ImageLoader
import coil3.compose.AsyncImage
import com.ks.tvlauncher.data.apps.artwork
import com.ks.tvlauncher.domain.AppInfo

/** The app-wide artwork loader, provided once by `MainActivity`. */
val LocalAppImageLoader = staticCompositionLocalOf<ImageLoader> {
    error("LocalAppImageLoader is not provided")
}

/**
 * An app's own artwork inside a tile: its banner filling the tile, or, for an app with no TV
 * banner, its icon centred on the glass. Loaded off the main thread and cached in memory at
 * the size of the tile, so scrolling back to a tile costs nothing.
 */
@Composable
fun AppArtworkImage(app: AppInfo) {
    val imageLoader = LocalAppImageLoader.current
    val model = remember(app.packageName, app.activityName, app.versionCode, app.hasBanner) { app.artwork() }
    if (app.hasBanner) {
        AsyncImage(
            model = model,
            contentDescription = null,
            imageLoader = imageLoader,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    } else {
        AsyncImage(
            model = model,
            contentDescription = null,
            imageLoader = imageLoader,
            modifier = Modifier.fillMaxHeight(0.5f).aspectRatio(1f),
            contentScale = ContentScale.Fit,
        )
    }
}
