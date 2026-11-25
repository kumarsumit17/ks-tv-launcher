package com.ks.tvlauncher.ui.components

import androidx.compose.foundation.focusGroup
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties

/**
 * `requestFocus()` throws if the requester is not attached to a laid-out node yet, for
 * example when a tile has not been composed. The app must never crash over that: the
 * request is simply skipped and the D-pad can still move focus normally.
 */
fun FocusRequester.requestFocusSafely() {
    try {
        requestFocus()
    } catch (e: IllegalStateException) {
        // Not attached yet; nothing to focus.
    }
}

/** Makes an overlay modal: focus may move around inside it but can never leave it. */
fun Modifier.modalFocus(): Modifier =
    this.focusProperties { onExit = { cancelFocusChange() } }.focusGroup()

/**
 * Remembers which item of a row or grid had focus last, with one [FocusRequester] per item
 * key, so focus can be sent back to exactly that item.
 *
 * Used instead of `Modifier.focusRestorer()`, which was seen on a real TV to restore the
 * wrong item inside lazy rows.
 */
class FocusMemory {
    private val requesters = HashMap<String, FocusRequester>()

    /** The key of the item that had focus last. */
    var lastKey: String? = null

    /** The requester to attach to the item with this key. */
    fun requester(key: String): FocusRequester = requesters.getOrPut(key) { FocusRequester() }

    /**
     * Where focus should go: the last focused item if it is still in [keys], else the first
     * item; [fallback] when there are no items. Also forgets items that are gone.
     */
    /** The item that had focus last, whether or not it still exists; [fallback] if none did. */
    fun last(fallback: FocusRequester): FocusRequester = lastKey?.let { requesters[it] } ?: fallback

    fun target(keys: List<String>, fallback: FocusRequester): FocusRequester {
        requesters.keys.retainAll(keys.toSet())
        val key = lastKey?.takeIf { it in keys } ?: keys.firstOrNull() ?: return fallback
        return requester(key)
    }
}
