package com.ks.tvlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TvInputsTest {

    private fun source(
        id: String,
        kind: InputKind = InputKind.HDMI,
        passthrough: Boolean = true,
        custom: String? = null,
        label: String? = null,
        connection: InputConnection = InputConnection.CONNECTED,
    ) = TvInputSource(id, kind, passthrough, custom, label, connection)

    @Test
    fun onlyPassthroughInputsAreListed() {
        val result = TvInputs.select(
            listOf(
                source("tuner/HW0", kind = InputKind.OTHER, passthrough = false, label = "Antenna"),
                source("tis/.HdmiInputService/HW2", label = "HDMI 1"),
                source("org.videolan.vlc/.Preview", kind = InputKind.OTHER, passthrough = false),
            ),
        )
        assertEquals(listOf("HDMI 1"), result.map { it.label })
    }

    @Test
    fun noPassthroughInputsGivesAnEmptyList() {
        assertTrue(TvInputs.select(listOf(source("tuner", passthrough = false))).isEmpty())
        assertTrue(TvInputs.select(emptyList()).isEmpty())
    }

    @Test
    fun hdmiComesFirstThenPortOrderByNumber() {
        val result = TvInputs.select(
            listOf(
                source("tis/.CompositeInputService/HW5", kind = InputKind.COMPOSITE, label = "AV"),
                source("tis/.HdmiInputService/HW10", label = "HDMI 4"),
                source("tis/.HdmiInputService/HW4", label = "HDMI 3"),
                source("tis/.HdmiInputService/HW2", label = "HDMI 1"),
                source("tis/.ComponentInputService/HW6", kind = InputKind.COMPONENT, label = "YPbPr"),
            ),
        )
        assertEquals(listOf("HDMI 1", "HDMI 3", "HDMI 4", "YPbPr", "AV"), result.map { it.label })
    }

    @Test
    fun renamingAnInputKeepsItsPlace() {
        val result = TvInputs.select(
            listOf(
                source("HW3", label = "HDMI 2", custom = "Apple TV"),
                source("HW2", label = "HDMI 1"),
            ),
        )
        assertEquals(listOf("HDMI 1", "Apple TV"), result.map { it.label })
    }

    @Test
    fun duplicateIdsAreListedOnce() {
        assertEquals(1, TvInputs.select(listOf(source("HW2"), source("HW2"))).size)
    }

    @Test
    fun customLabelWinsThenOwnLabelThenId() {
        assertEquals("PS5", TvInputs.label(" PS5 ", "HDMI 1", "HW2"))
        assertEquals("HDMI 1", TvInputs.label(null, "HDMI 1", "HW2"))
        assertEquals("HDMI 1", TvInputs.label("   ", " HDMI 1", "HW2"))
        assertEquals("HW2", TvInputs.label(null, "", "HW2"))
    }

    @Test
    fun connectionStateIsKept() {
        val result = TvInputs.select(listOf(source("HW2", connection = InputConnection.STANDBY)))
        assertEquals(InputConnection.STANDBY, result.single().connection)
    }

    @Test
    fun naturalCompareOrdersNumbersByValue() {
        assertTrue(TvInputs.naturalCompare("HW2", "HW10") < 0)
        assertTrue(TvInputs.naturalCompare("HW10", "HW2") > 0)
        assertTrue(TvInputs.naturalCompare("HW02", "HW2") == 0)
        assertTrue(TvInputs.naturalCompare("HW", "HW1") < 0)
        assertTrue(TvInputs.naturalCompare("A1", "B0") < 0)
    }
}
