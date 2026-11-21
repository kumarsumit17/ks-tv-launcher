package com.ks.tvlauncher.domain

import kotlinx.serialization.Serializable

/** A published release, as KS needs it. Stored while an update is pending. */
@Serializable
data class ReleaseInfo(
    /** "1.2.3", without the leading v. */
    val versionName: String,
    val versionCode: Int,
    val notes: String = "",
    val apkUrl: String,
    val apkSize: Long,
)

/** Why a downloaded file is (not) safe to install. */
enum class UpdateVerdict { OK, WRONG_SIZE, NOT_AN_APK, WRONG_PACKAGE, WRONG_SIGNER, NOT_NEWER }

/** Pure rules for in-app updates (specs/app-updates.md). */
object AppUpdates {
    private val TAG = Regex("""^v?(\d{1,5})\.(\d{1,2})\.(\d{1,2})(-[0-9A-Za-z.-]+)?$""")

    /**
     * `vMAJOR.MINOR.PATCH` → `MAJOR*10000 + MINOR*100 + PATCH`, the rule in
     * `.github/workflows/release.yml`. Null for a malformed tag, or a MINOR or PATCH above 99,
     * which would collide with the next part.
     */
    fun versionCode(tag: String): Int? {
        val match = TAG.matchEntire(tag.trim()) ?: return null
        val (major, minor, patch) = match.destructured
        return major.toInt() * 10000 + minor.toInt() * 100 + patch.toInt()
    }

    /** "v1.2.3-rc1" → "1.2.3-rc1"; null when the tag is malformed. */
    fun versionName(tag: String): String? = tag.trim().takeIf { versionCode(it) != null }?.removePrefix("v")

    /** An update exists only for a strictly higher versionCode: never a downgrade or reinstall. */
    fun isNewer(candidate: Int, installed: Int): Boolean = candidate > installed

    /** The checks before any install prompt, in the order the spec gives. */
    fun verify(
        expectedSize: Long,
        actualSize: Long,
        archivePackage: String?,
        ownPackage: String,
        archiveSigners: Set<String>,
        ownSigners: Set<String>,
        archiveVersionCode: Long,
        installedVersionCode: Long,
    ): UpdateVerdict = when {
        expectedSize <= 0 || actualSize != expectedSize -> UpdateVerdict.WRONG_SIZE
        archivePackage == null -> UpdateVerdict.NOT_AN_APK
        archivePackage != ownPackage -> UpdateVerdict.WRONG_PACKAGE
        archiveSigners.isEmpty() || archiveSigners != ownSigners -> UpdateVerdict.WRONG_SIGNER
        archiveVersionCode <= installedVersionCode -> UpdateVerdict.NOT_NEWER
        else -> UpdateVerdict.OK
    }

    /** Release notes shown in About are cut to this many characters. */
    const val MAX_NOTES = 1200

    const val NOTIFICATION_ID = 0x4B5355 // "KSU"; reminder ids are hashes of subscription ids
}
