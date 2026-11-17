package com.ks.tvlauncher.data.updates

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

class GitHubReleaseParsingTest {

    private fun body(tag: String = "v0.7.0", assets: String = APK, notes: String = "\"Faster Home.\\r\\nIn-app updates.\"") = """
        {"url":"x","tag_name":"$tag","name":"KS $tag","draft":false,"prerelease":false,
         "body":$notes,"assets":[$assets],"author":{"login":"someone"}}
    """.trimIndent()

    @Test
    fun parsesTagNotesAndApkAsset() {
        val release = GitHubUpdateSource.parse(body(assets = "$AAB,$APK"))
        assertEquals("0.7.0", release.versionName)
        assertEquals(700, release.versionCode)
        assertEquals("Faster Home.\r\nIn-app updates.", release.notes)
        assertEquals("https://github.com/o/r/releases/download/v0.7.0/app-release.apk", release.apkUrl)
        assertEquals(6_543_210L, release.apkSize)
    }

    @Test
    fun missingNotesAreEmptyAndLongNotesAreCut() {
        assertEquals("", GitHubUpdateSource.parse(body(notes = "null")).notes)
        val long = "a".repeat(5000)
        assertEquals(1200, GitHubUpdateSource.parse(body(notes = "\"$long\"")).notes.length)
    }

    @Test
    fun rejectsBadReleases() {
        listOf(
            body(tag = "nightly"),
            body(tag = "v1.2"),
            body(assets = AAB),
            body(assets = ""),
            body(assets = APK.replace("https://", "http://")),
            body(assets = APK.replace("6543210", "0")),
            "not json",
            "{}",
        ).forEach { assertThrows(IOException::class.java) { GitHubUpdateSource.parse(it) } }
    }

    private companion object {
        const val APK = """{"name":"app-release.apk","size":6543210,"content_type":"application/vnd.android.package-archive","browser_download_url":"https://github.com/o/r/releases/download/v0.7.0/app-release.apk"}"""
        const val AAB = """{"name":"app-release.aab","size":5000000,"browser_download_url":"https://github.com/o/r/releases/download/v0.7.0/app-release.aab"}"""
    }
}
