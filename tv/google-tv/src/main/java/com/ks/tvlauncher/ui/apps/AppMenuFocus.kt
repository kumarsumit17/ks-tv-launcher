package com.ks.tvlauncher.ui.apps

/** A button of [AppMenu] that focus can be sent to. */
enum class AppMenuButton { UNPIN, MOVE_LEFT, MOVE_RIGHT }

/**
 * Where focus goes after a Move in the app menu. Move keeps the menu open; when the app reaches
 * the start or the end of the row, the pressed button disappears, and focus must land on the
 * nearest remaining button instead of leaving the menu.
 */
object AppMenuFocus {
    /**
     * [delta] is the move just made (-1 left, +1 right). Returns null while the pressed button
     * still exists (focus stays on it); otherwise the other Move button when shown, else Unpin,
     * which sits directly above the Move buttons.
     */
    fun afterMove(delta: Int, canMoveLeft: Boolean, canMoveRight: Boolean): AppMenuButton? = when {
        delta < 0 && !canMoveLeft -> if (canMoveRight) AppMenuButton.MOVE_RIGHT else AppMenuButton.UNPIN
        delta > 0 && !canMoveRight -> if (canMoveLeft) AppMenuButton.MOVE_LEFT else AppMenuButton.UNPIN
        else -> null
    }
}
