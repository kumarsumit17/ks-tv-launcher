package com.ks.tvlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class AnimationSpeedTest {

    @Test
    fun defaultIsHalfSpeedDuration() {
        assertEquals(50, AnimationSpeed.DEFAULT)
        assertEquals(50, AnimationSpeed.normalise(null))
    }

    @Test
    fun oddValuesSnapToTheNearestStep() {
        assertEquals(25, AnimationSpeed.normalise(0))
        assertEquals(25, AnimationSpeed.normalise(-40))
        assertEquals(75, AnimationSpeed.normalise(80))
        assertEquals(150, AnimationSpeed.normalise(900))
        assertEquals(100, AnimationSpeed.normalise(100))
    }

    @Test
    fun stepMovesOneAndStopsAtTheEnds() {
        assertEquals(75, AnimationSpeed.step(50, +1))
        assertEquals(25, AnimationSpeed.step(50, -1))
        assertEquals(25, AnimationSpeed.step(25, -1))
        assertEquals(150, AnimationSpeed.step(150, +1))
        // A large direction still moves one step.
        assertEquals(75, AnimationSpeed.step(50, +5))
    }

    @Test
    fun durationsScale() {
        assertEquals(100, AnimationSpeed.scale(200, 50))
        assertEquals(140, AnimationSpeed.scale(280, 50))
        assertEquals(400, AnimationSpeed.scale(400, 100))
        assertEquals(600, AnimationSpeed.scale(400, 150))
        assertEquals(50, AnimationSpeed.scale(200, 25))
        assertEquals(1, AnimationSpeed.scale(1, 25))
    }

    @Test
    fun labels() {
        assertEquals("0.25×", AnimationSpeed.label(25))
        assertEquals("0.5×", AnimationSpeed.label(50))
        assertEquals("0.75×", AnimationSpeed.label(75))
        assertEquals("1×", AnimationSpeed.label(100))
        assertEquals("1.25×", AnimationSpeed.label(125))
        assertEquals("1.5×", AnimationSpeed.label(150))
    }
}
