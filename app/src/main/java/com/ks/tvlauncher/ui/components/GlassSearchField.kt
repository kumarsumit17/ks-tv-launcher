package com.ks.tvlauncher.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.ks.tvlauncher.ui.icons.KsIcons
import com.ks.tvlauncher.ui.theme.KsColors
import com.ks.tvlauncher.ui.theme.KsShapes
import com.ks.tvlauncher.ui.theme.KsType

/**
 * Search (or plain text) field for the remote. At rest it is an ordinary focus stop, so moving past it with
 * the D-pad never pops the keyboard. OK starts editing and opens the keyboard (voice input
 * works through it); the keyboard's Search action, or Back, ends editing and returns focus
 * to the field.
 */
@Composable
fun GlassSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    /** Null for a plain text field without the magnifier. */
    icon: ImageVector? = KsIcons.Search,
    imeAction: ImeAction = ImeAction.Search,
    maxLength: Int = Int.MAX_VALUE,
) {
    var editing by remember { mutableStateOf(false) }
    val restFocus = remember { FocusRequester() }
    val editFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    // Runs when editing starts (focus the text field, show the keyboard) and when it ends
    // (hide the keyboard, give focus back to the resting field).
    var wasEditing by remember { mutableStateOf(false) }
    LaunchedEffect(editing) {
        withFrameNanos { }
        if (editing) {
            editFocus.requestFocusSafely()
            keyboard?.show()
        } else if (wasEditing) {
            keyboard?.hide()
            restFocus.requestFocusSafely()
        }
        wasEditing = editing
    }
    BackHandler(enabled = editing) { editing = false }

    val content: @Composable (@Composable () -> Unit) -> Unit = { text ->
        Row(
            modifier = Modifier
                .glass(shape = KsShapes.Pill)
                .fillMaxWidth()
                .defaultMinSize(minHeight = 30.dp)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(13.dp), tint = KsColors.TextSecondary)
                Spacer(Modifier.width(8.dp))
            }
            Box(Modifier.weight(1f)) { text() }
        }
    }

    if (editing) {
        Box(modifier) {
            content {
                BasicTextField(
                    value = query,
                    onValueChange = { onQueryChange(it.take(maxLength)) },
                    singleLine = true,
                    textStyle = KsType.Label.copy(color = KsColors.TextPrimary),
                    cursorBrush = SolidColor(KsColors.TextPrimary),
                    keyboardOptions = KeyboardOptions(imeAction = imeAction),
                    keyboardActions = KeyboardActions(onSearch = { editing = false }, onDone = { editing = false }),
                    modifier = Modifier.fillMaxWidth().focusRequester(editFocus),
                )
            }
        }
    } else {
        DpadFocusable(
            modifier = modifier.focusRequester(restFocus),
            onClick = { editing = true },
            shape = KsShapes.Pill,
            focusedScale = 1.02f,
        ) {
            content {
                Text(
                    text = query.ifEmpty { placeholder },
                    style = KsType.Label,
                    color = if (query.isEmpty()) KsColors.TextSecondary else KsColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
