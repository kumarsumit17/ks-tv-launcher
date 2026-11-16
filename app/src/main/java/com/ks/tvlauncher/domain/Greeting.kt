package com.ks.tvlauncher.domain

import java.time.LocalTime

/** Which greeting to show. Morning 5:00–11:59, afternoon 12:00–16:59, evening 17:00–4:59. */
enum class GreetingPeriod {
    MORNING,
    AFTERNOON,
    EVENING,
    ;

    companion object {
        fun at(time: LocalTime): GreetingPeriod = when (time.hour) {
            in 5..11 -> MORNING
            in 12..16 -> AFTERNOON
            else -> EVENING
        }
    }
}

object Greeting {
    /** The name to greet, or null when none is saved, in which case only the greeting shows. */
    fun displayName(activeName: String?): String? = activeName?.trim()?.takeIf { it.isNotEmpty() }
}
