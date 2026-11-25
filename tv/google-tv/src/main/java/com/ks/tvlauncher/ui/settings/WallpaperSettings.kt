package com.ks.tvlauncher.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.ks.tvlauncher.R
import com.ks.tvlauncher.data.settings.LauncherSettings
import com.ks.tvlauncher.domain.WallpaperDim
import com.ks.tvlauncher.domain.WallpaperImage
import com.ks.tvlauncher.domain.WallpaperMode
import com.ks.tvlauncher.domain.Wallpapers
import com.ks.tvlauncher.ui.components.BuiltInWallpaper
import com.ks.tvlauncher.ui.components.DpadFocusable
import com.ks.tvlauncher.ui.components.GlassButton
import com.ks.tvlauncher.ui.components.GlassChip
import com.ks.tvlauncher.ui.components.builtInWallpaperName
import com.ks.tvlauncher.ui.icons.KsIcons
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsShapes
import com.ks.tvlauncher.ui.theme.KsType

private val SwatchWidth = 72.dp
private val SwatchHeight = 41.dp

/**
 * Settings › Wallpaper (specs/custom-wallpaper.md): mode, the six built-in wallpapers, the
 * saved picture, "Choose a picture" (only where the TV has a document picker), "Remove image"
 * and the dim level (only in Custom). Every choice is written at once.
 */
@Composable
internal fun WallpaperSettings(
    settings: LauncherSettings,
    files: WallpaperFiles,
    viewModel: SettingsViewModel,
    /** Focus moves here when the focused control is about to disappear. */
    onRefocus: (FocusRequester) -> Unit,
) {
    val modeFocus = remember { FocusRequester() }
    val chooseFocus = remember { FocusRequester() }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        // No takePersistableUriPermission: the image is copied once and the original forgotten.
        if (uri != null) viewModel.importPicture(uri)
    }
    LaunchedEffect(Unit) { viewModel.refreshWallpaperFiles() }

    val custom = settings.wallpaperMode == WallpaperMode.CUSTOM
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ChoiceRow(stringResource(R.string.settings_wallpaper_mode)) {
            WallpaperMode.entries.forEach { mode ->
                GlassChip(
                    text = stringResource(
                        when (mode) {
                            WallpaperMode.DYNAMIC -> R.string.settings_wallpaper_dynamic
                            WallpaperMode.CUSTOM -> R.string.settings_wallpaper_custom
                            WallpaperMode.PLAIN -> R.string.settings_wallpaper_plain
                        },
                    ),
                    selected = settings.wallpaperMode == mode,
                    onClick = { viewModel.setWallpaperMode(mode) },
                    modifier = if (mode == WallpaperMode.DYNAMIC) Modifier.focusRequester(modeFocus) else Modifier,
                )
            }
        }
        if (settings.wallpaperPictureFailed) {
            Text(stringResource(R.string.settings_wallpaper_failed), style = KsType.Body, color = KsColors.Warning)
        }
        Text(stringResource(R.string.settings_wallpaper_pick_hint), style = KsType.Caption, color = KsColors.TextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(Wallpapers.BUILT_IN_COUNT) { id ->
                val name = stringResource(builtInWallpaperName(id))
                Swatch(
                    label = name,
                    selected = custom && settings.wallpaperImage == WallpaperImage.BuiltIn(id),
                    onClick = { viewModel.chooseBuiltInWallpaper(id) },
                ) { BuiltInWallpaper(id, Modifier.fillMaxSize()) }
            }
            if (files.pictureSaved) {
                val label = stringResource(R.string.settings_wallpaper_your_picture)
                Swatch(
                    label = label,
                    selected = custom && settings.wallpaperImage is WallpaperImage.Picture,
                    onClick = viewModel::chooseSavedPicture,
                ) {
                    Box(Modifier.fillMaxSize().background(KsColors.PanelFill), contentAlignment = Alignment.Center) {
                        Text(label, style = KsType.Caption, color = KsColors.TextPrimary, textAlign = TextAlign.Center, modifier = Modifier.padding(4.dp))
                    }
                }
            }
        }
        if (files.canPickFiles) {
            ValueRow(
                label = stringResource(R.string.settings_wallpaper_choose),
                value = stringResource(
                    when {
                        files.importing -> R.string.settings_wallpaper_copying
                        files.importFailed -> R.string.settings_wallpaper_import_failed
                        else -> R.string.settings_wallpaper_choose_hint
                    },
                ),
                onClick = { if (!files.importing) picker.launch(arrayOf("image/*")) },
                modifier = Modifier.focusRequester(chooseFocus),
            )
        }
        if (files.pictureSaved) {
            GlassButton(
                text = stringResource(R.string.settings_wallpaper_remove),
                onClick = {
                    // The button disappears once the copy is gone: hand focus on first.
                    onRefocus(if (files.canPickFiles) chooseFocus else modeFocus)
                    viewModel.removePicture()
                },
            )
        }
        if (custom) {
            ChoiceRow(stringResource(R.string.settings_wallpaper_dim)) {
                WallpaperDim.entries.forEach { dim ->
                    GlassChip(
                        text = stringResource(
                            when (dim) {
                                WallpaperDim.LOW -> R.string.settings_wallpaper_dim_low
                                WallpaperDim.MEDIUM -> R.string.settings_wallpaper_dim_medium
                                WallpaperDim.HIGH -> R.string.settings_wallpaper_dim_high
                            },
                        ),
                        selected = settings.wallpaperDim == dim,
                        onClick = { viewModel.setWallpaperDim(dim) },
                    )
                }
            }
        }
    }
}

/** A small focusable preview; the selected one carries a check mark. */
@Composable
private fun Swatch(label: String, selected: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    DpadFocusable(
        modifier = Modifier.size(SwatchWidth, SwatchHeight).semantics { contentDescription = label },
        onClick = onClick,
        shape = KsShapes.Tile,
    ) {
        // Own box: DpadFocusable hands its full size to each child as a minimum, which would
        // stretch the badge across the swatch.
        Box(Modifier.fillMaxSize()) {
            content()
            if (selected) {
                Box(
                    Modifier.align(Alignment.TopEnd).padding(4.dp).size(16.dp).background(KsColors.PrimaryButton, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(KsIcons.Check, contentDescription = null, modifier = Modifier.size(10.dp), tint = KsColors.OnPrimaryButton)
                }
            }
        }
    }
}
