package com.ks.tvlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdatesTest {

    @Test
    fun tagMapsLikeTheReleaseWorkflow() {
        assertEquals(601, AppUpdates.versionCode("v0.6.1"))
        assertEquals(10000, AppUpdates.versionCode("v1.0.0"))
        assertEquals(12345, AppUpdates.versionCode("v1.23.45"))
        assertEquals(609, AppUpdates.versionCode("0.6.9"))
        // Leading zeros are decimal, as `10#` makes them in the workflow.
        assertEquals(808, AppUpdates.versionCode("v0.08.08"))
        // A pre-release suffix is ignored for the code, like `${tag%%-*}`.
        assertEquals(700, AppUpdates.versionCode("v0.7.0-rc1"))
        assertEquals("0.7.0-rc1", AppUpdates.versionName("v0.7.0-rc1"))
        assertEquals("0.6.1", AppUpdates.versionName("v0.6.1"))
    }

    @Test
    fun malformedTagsHaveNoCode() {
        listOf("", "v", "latest", "v1", "v1.2", "v1.2.3.4", "v1.2.x", "V1.2.3", "v1.2.3 beta", "v-1.2.3", "v1.100.0", "v1.2.100", "vv1.2.3")
            .forEach { assertNull(it, AppUpdates.versionCode(it)) }
        assertNull(AppUpdates.versionName("nightly"))
    }

    @Test
    fun onlyAHigherCodeIsAnUpdate() {
        assertTrue(AppUpdates.isNewer(602, 601))
        assertFalse(AppUpdates.isNewer(601, 601))
        assertFalse(AppUpdates.isNewer(600, 601))
    }

    private fun verify(
        size: Long = 100,
        pkg: String? = "com.ks.tvlauncher",
        signers: Set<String> = setOf("2aa25f08"),
        code: Long = 602,
        sha: String = "A".repeat(64),
    ) = AppUpdates.verify(
        expectedSize = 100,
        actualSize = size,
        expectedSha256 = "a".repeat(64),
        actualSha256 = sha,
        archivePackage = pkg,
        ownPackage = "com.ks.tvlauncher",
        archiveSigners = signers,
        ownSigners = setOf("2aa25f08"),
        archiveVersionCode = code,
        installedVersionCode = 601,
    )

    @Test
    fun verificationDecisions() {
        assertEquals(UpdateVerdict.OK, verify())
        assertEquals(UpdateVerdict.WRONG_SIZE, verify(size = 99))
        assertEquals(UpdateVerdict.WRONG_CHECKSUM, verify(sha = "b".repeat(64)))
        assertEquals(UpdateVerdict.WRONG_CHECKSUM, verify(sha = ""))
        assertEquals(UpdateVerdict.NOT_AN_APK, verify(pkg = null))
        assertEquals(UpdateVerdict.WRONG_PACKAGE, verify(pkg = "com.ks.tvlauncher.dev"))
        assertEquals(UpdateVerdict.WRONG_SIGNER, verify(signers = setOf("deadbeef")))
        assertEquals(UpdateVerdict.WRONG_SIGNER, verify(signers = emptySet()))
        assertEquals(UpdateVerdict.WRONG_SIGNER, verify(signers = setOf("2aa25f08", "deadbeef")))
        assertEquals(UpdateVerdict.NOT_NEWER, verify(code = 601))
        assertEquals(UpdateVerdict.NOT_NEWER, verify(code = 500))
    }

    @Test
    fun checksRunInSpecOrder() {
        // Size first, even when everything else is also wrong.
        assertEquals(UpdateVerdict.WRONG_SIZE, verify(size = 1, pkg = "x", signers = emptySet(), code = 1))
        assertEquals(UpdateVerdict.WRONG_CHECKSUM, verify(sha = "", pkg = "x", signers = emptySet(), code = 1))
        assertEquals(UpdateVerdict.WRONG_PACKAGE, verify(pkg = "x", signers = emptySet(), code = 1))
        assertEquals(UpdateVerdict.WRONG_SIGNER, verify(signers = emptySet(), code = 1))
    }

    @Test
    fun zeroExpectedSizeNeverVerifies() {
        val verdict = AppUpdates.verify(0, 0, "", "", "com.ks.tvlauncher", "com.ks.tvlauncher", setOf("a"), setOf("a"), 2, 1)
        assertEquals(UpdateVerdict.WRONG_SIZE, verdict)
    }
}
