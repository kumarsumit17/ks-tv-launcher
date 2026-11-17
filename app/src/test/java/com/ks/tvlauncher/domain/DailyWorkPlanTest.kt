package com.ks.tvlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class DailyWorkPlanTest {

    @Test
    fun bothSwitchesOnInAReleaseBuildRegistersBoth() {
        assertEquals(
            setOf(DailyWork.RENEWAL_REMINDERS, DailyWork.UPDATE_CHECK),
            DailyWorkPlan.toRegister(remindersEnabled = true, autoUpdateCheck = true, inAppUpdates = true),
        )
    }

    @Test
    fun remindersOffRegistersOnlyTheUpdateCheck() {
        assertEquals(
            setOf(DailyWork.UPDATE_CHECK),
            DailyWorkPlan.toRegister(remindersEnabled = false, autoUpdateCheck = true, inAppUpdates = true),
        )
    }

    @Test
    fun automaticChecksOffRegistersOnlyReminders() {
        assertEquals(
            setOf(DailyWork.RENEWAL_REMINDERS),
            DailyWorkPlan.toRegister(remindersEnabled = true, autoUpdateCheck = false, inAppUpdates = true),
        )
    }

    @Test
    fun devBuildsNeverRegisterTheUpdateCheck() {
        assertEquals(
            setOf(DailyWork.RENEWAL_REMINDERS),
            DailyWorkPlan.toRegister(remindersEnabled = true, autoUpdateCheck = true, inAppUpdates = false),
        )
    }

    @Test
    fun bothOffRegistersNothing() {
        assertEquals(
            emptySet<DailyWork>(),
            DailyWorkPlan.toRegister(remindersEnabled = false, autoUpdateCheck = false, inAppUpdates = true),
        )
    }
}
