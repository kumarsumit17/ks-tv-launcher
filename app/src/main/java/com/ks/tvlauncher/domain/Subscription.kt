package com.ks.tvlauncher.domain

import kotlinx.serialization.Serializable

enum class BillingCycle { MONTHLY, YEARLY }

/**
 * One subscription, stored on the device only.
 *
 * Money is in minor units (paise for INR), never Float or Double. [renewsOn] is an ISO
 * `yyyy-MM-dd` date; it may lie in the past and is rolled forward when shown.
 */
@Serializable
data class Subscription(
    val id: String,
    val name: String,
    val provider: String,
    val costMinor: Long,
    val cycle: BillingCycle,
    val renewsOn: String,
    val active: Boolean,
    val note: String = "",
)
