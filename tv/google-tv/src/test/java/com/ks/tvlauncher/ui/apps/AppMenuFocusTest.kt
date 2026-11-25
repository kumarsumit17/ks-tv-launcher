package com.ks.tvlauncher.ui.apps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppMenuFocusTest {

    @Test
    fun moveInTheMiddleKeepsFocusOnThePressedButton() {
        assertNull(AppMenuFocus.afterMove(-1, canMoveLeft = true, canMoveRight = true))
        assertNull(AppMenuFocus.afterMove(1, canMoveLeft = true, canMoveRight = true))
    }

    @Test
    fun moveRightToTheEndFocusesMoveLeft() {
        assertEquals(AppMenuButton.MOVE_LEFT, AppMenuFocus.afterMove(1, canMoveLeft = true, canMoveRight = false))
    }

    @Test
    fun moveLeftToTheStartFocusesMoveRight() {
        assertEquals(AppMenuButton.MOVE_RIGHT, AppMenuFocus.afterMove(-1, canMoveLeft = false, canMoveRight = true))
    }

    @Test
    fun noMoveButtonLeftFocusesUnpin() {
        assertEquals(AppMenuButton.UNPIN, AppMenuFocus.afterMove(1, canMoveLeft = false, canMoveRight = false))
        assertEquals(AppMenuButton.UNPIN, AppMenuFocus.afterMove(-1, canMoveLeft = false, canMoveRight = false))
    }

    @Test
    fun noMoveYetChangesNothing() {
        assertNull(AppMenuFocus.afterMove(0, canMoveLeft = false, canMoveRight = true))
    }
}
