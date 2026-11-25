package com.ks.tvlauncher.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.ks.tvlauncher.R
import com.ks.tvlauncher.data.profile.Profile
import com.ks.tvlauncher.data.system.DeviceInfo
import com.ks.tvlauncher.domain.ProfileNames
import com.ks.tvlauncher.ui.components.GlassButton
import com.ks.tvlauncher.ui.components.GlassCard
import com.ks.tvlauncher.ui.components.GlassChip
import com.ks.tvlauncher.ui.components.GlassDialog
import com.ks.tvlauncher.ui.components.GlassPill
import com.ks.tvlauncher.ui.components.GlassSearchField
import com.ks.tvlauncher.ui.components.GlassToggle
import com.ks.tvlauncher.ui.components.requestFocusSafely
import com.ks.tvlauncher.ui.icons.KsIcons
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsType

/**
 * "Who's watching?": saved names (OK selects; hold OK, or the "Rename or remove" button, to
 * rename or remove), a field to add a name, the TV's own details marked AUTO, and "Use my own
 * TV name". Nothing is stored until Save. Names never leave the device.
 */
@Composable
fun ProfileDialog(
    profile: Profile,
    device: DeviceInfo?,
    onSave: (names: List<String>, active: String?, useOwnTvName: Boolean, ownTvName: String) -> Unit,
    onDismiss: () -> Unit,
    returnFocusTo: FocusRequester?,
) {
    var names by rememberSaveable { mutableStateOf(profile.names) }
    var active by rememberSaveable { mutableStateOf(profile.activeName ?: profile.names.firstOrNull()) }
    var field by rememberSaveable { mutableStateOf("") }
    var useOwn by rememberSaveable { mutableStateOf(profile.useOwnTvName) }
    var ownName by rememberSaveable { mutableStateOf(profile.ownTvName) }
    // The name being renamed or removed; null on the main view.
    var managing by rememberSaveable { mutableStateOf<String?>(null) }
    var renameText by rememberSaveable { mutableStateOf("") }

    val firstFocus = remember { FocusRequester() }
    val manageFocus = remember { FocusRequester() }
    val renameFocus = remember { FocusRequester() }
    // One requester per name chip, so focus can land on a chip that was just added.
    val chipFocus = remember { HashMap<String, FocusRequester>() }
    fun chipRequester(name: String) = chipFocus.getOrPut(name) { FocusRequester() }
    // A name just added: the Add button disables itself once the field clears, so focus is
    // moved to the new chip instead of being left wherever the system puts it.
    var focusChip by remember { mutableStateOf<String?>(null) }
    var openedFocusDone by remember { mutableStateOf(false) }

    fun save() {
        var finalNames = names
        var finalActive = active
        if (ProfileNames.canAdd(finalNames, field)) {
            val added = ProfileNames.sanitize(field)
            finalNames = ProfileNames.add(finalNames, field)
            finalActive = added
        }
        if (finalActive !in finalNames) finalActive = finalNames.firstOrNull()
        onSave(finalNames, finalActive, useOwn, ownName)
    }

    GlassDialog(
        onDismiss = { if (managing != null) managing = null else onDismiss() },
        title = stringResource(if (managing == null) R.string.profile_title else R.string.profile_manage_title),
        maxWidth = 560.dp,
        returnFocusTo = returnFocusTo,
        // Tighter than the default so the full dialog fits inside the overscan-safe area.
        spacing = 8.dp,
    ) {
        val target = managing
        if (target != null) {
            LaunchedEffect(target) {
                withFrameNanos { }
                manageFocus.requestFocusSafely()
            }
            GlassSearchField(
                query = renameText,
                onQueryChange = { renameText = it },
                placeholder = target,
                icon = null,
                imeAction = ImeAction.Done,
                maxLength = ProfileNames.MAX_LENGTH,
                // Down goes to Rename, not to the nearest button under the field's centre (Back).
                modifier = Modifier.fillMaxWidth().focusRequester(manageFocus).focusProperties { down = renameFocus },
            )
            val renamed = ProfileNames.rename(names, target, renameText)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassButton(
                    text = stringResource(R.string.profile_rename),
                    primary = true,
                    // Stays enabled (a disabled button cannot take focus); an invalid or
                    // unchanged name simply does nothing.
                    onClick = {
                        if (renamed != names) {
                            val newName = ProfileNames.sanitize(renameText)
                            names = renamed
                            if (active == target) active = newName
                            managing = null
                            focusChip = newName
                        }
                    },
                    modifier = Modifier.focusRequester(renameFocus),
                )
                GlassButton(
                    text = stringResource(R.string.profile_remove),
                    onClick = {
                        names = ProfileNames.remove(names, target)
                        if (active == target) active = names.firstOrNull()
                        managing = null
                        // No chip left: the field, so focus never escapes the dialog.
                        focusChip = names.firstOrNull() ?: FOCUS_FIELD
                    },
                )
                GlassButton(
                    text = stringResource(R.string.profile_back),
                    onClick = {
                        managing = null
                        focusChip = target
                    },
                )
            }
        } else {
            // Opening the dialog focuses the name field; coming back from Rename/Remove, or
            // after Add name, focuses a chip instead (one effect, so they never race).
            LaunchedEffect(focusChip) {
                withFrameNanos { }
                withFrameNanos { }
                val name = focusChip
                if (name == FOCUS_FIELD) {
                    firstFocus.requestFocusSafely()
                    focusChip = null
                } else if (name != null) {
                    chipRequester(name).requestFocusSafely()
                    focusChip = null
                } else if (!openedFocusDone) {
                    firstFocus.requestFocusSafely()
                    openedFocusDone = true
                }
            }
            GlassSearchField(
                query = field,
                onQueryChange = { field = it },
                placeholder = stringResource(R.string.profile_name_hint),
                icon = null,
                imeAction = ImeAction.Done,
                maxLength = ProfileNames.MAX_LENGTH,
                modifier = Modifier.fillMaxWidth().focusRequester(firstFocus),
            )
            Text(stringResource(R.string.profile_voice_hint), style = KsType.Caption, color = KsColors.TextSecondary)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items(names, key = { it }) { name ->
                    GlassChip(
                        text = name,
                        selected = name == active,
                        icon = if (name == active) KsIcons.Check else null,
                        onClick = { active = name },
                        modifier = Modifier.focusRequester(chipRequester(name)),
                        onLongClick = {
                            renameText = name
                            managing = name
                        },
                    )
                }
                item(key = "+add") {
                    GlassButton(
                        text = stringResource(R.string.profile_add),
                        icon = KsIcons.Plus,
                        enabled = ProfileNames.canAdd(names, field),
                        onClick = {
                            val added = ProfileNames.sanitize(field)
                            names = ProfileNames.add(names, field)
                            active = added
                            field = ""
                            focusChip = added
                        },
                    )
                }
            }
            val current = active
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (current != null) {
                    GlassButton(
                        text = stringResource(R.string.profile_manage, current),
                        onClick = {
                            renameText = current
                            managing = current
                        },
                    )
                    Spacer(Modifier.weight(1f))
                }
                if (names.size >= ProfileNames.MAX_NAMES) {
                    Text(pluralStringResource(R.plurals.profile_max, ProfileNames.MAX_NAMES, ProfileNames.MAX_NAMES), style = KsType.Caption, color = KsColors.TextSecondary)
                }
            }

            Text(stringResource(R.string.profile_this_tv).uppercase(), style = KsType.SectionLabel, color = KsColors.TextTertiary)
            GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)) {
                AutoRow(stringResource(R.string.profile_tv_name), device?.let { it.systemName ?: it.model }.orEmpty())
                AutoRow(stringResource(R.string.profile_model), device?.model.orEmpty())
                AutoRow(
                    stringResource(R.string.profile_android),
                    device?.let { stringResource(R.string.profile_android_value, it.androidRelease, it.sdkInt) }.orEmpty(),
                )
            }
            GlassToggle(label = stringResource(R.string.profile_use_own), checked = useOwn, onCheckedChange = { useOwn = it })
            if (useOwn) {
                GlassSearchField(
                    query = ownName,
                    onQueryChange = { ownName = it },
                    placeholder = stringResource(R.string.profile_own_hint),
                    icon = null,
                    imeAction = ImeAction.Done,
                    maxLength = ProfileNames.MAX_LENGTH,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)) {
                GlassButton(text = stringResource(R.string.profile_cancel), onClick = onDismiss)
                GlassButton(text = stringResource(R.string.profile_save), primary = true, onClick = ::save)
            }
        }
    }
}

/** [focusChip] value meaning "focus the name field". Not a valid name (names are trimmed). */
private const val FOCUS_FIELD = " field"

@Composable
private fun AutoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = KsType.Label, color = KsColors.TextPrimary, modifier = Modifier.weight(1f))
        Text(value, style = KsType.Body, color = KsColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.padding(start = 8.dp))
        GlassPill(text = stringResource(R.string.profile_auto))
    }
}
