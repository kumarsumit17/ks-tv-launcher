package com.ks.tvlauncher.data.updates

import com.ks.utility.appupdate.GitHubRepo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

class GitHubReleaseParsingTest {

    private fun release(
        tag: String = "v0.7.0",
        assets: String = APK,
        notes: String = "\"Faster Home.\\r\\nIn-app updates.\"",
        draft: Boolean = false,
        prerelease: Boolean = false,
    ) = """
        {"url":"x","tag_name":"$tag","name":"KS $tag","draft":$draft,"prerelease":$prerelease,
         "body":$notes,"assets":[${assets.replace("/v0.7.0/", "/$tag/")}],"author":{"login":"someone"}}
    """.trimIndent()

    /** A `releases?per_page=30` response: a list, newest first. */
    private fun list(vararg releases: String) = releases.joinToString(",", "[", "]")

    private fun body(tag: String = "v0.7.0", assets: String = APK, notes: String = "\"Faster Home.\\r\\nIn-app updates.\"") =
        list(release(tag, assets, notes))

    @Test
    fun parsesTagNotesAndApkAsset() {
        val release = GitHubUpdateSource.parse(body(assets = "$AAB,$APK"), REPO)
        assertEquals("0.7.0", release.versionName)
        assertEquals(700, release.versionCode)
        assertEquals("Faster Home.\r\nIn-app updates.", release.notes)
        assertEquals("https://github.com/o/r/releases/download/v0.7.0/app-release.apk", release.apkUrl)
        assertEquals(6_543_210L, release.apkSize)
        assertEquals("ab".repeat(32), release.apkSha256)
    }

    @Test
    fun missingNotesAreEmptyAndLongNotesAreCut() {
        assertEquals("", GitHubUpdateSource.parse(body(notes = "null"), REPO).notes)
        val long = "a".repeat(5000)
        assertEquals(1200, GitHubUpdateSource.parse(body(notes = "\"$long\""), REPO).notes.length)
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
            body(assets = APK.replace("sha256:", "md5:")),
            body(assets = APK.replace("github.com/o/r/", "github.com/o/other/")),
            body(assets = APK.replace("https://github.com/o/r/", "https://example.com/o/r/")),
            "not json",
            "{}",
            "[]",
            release(),
        ).forEach { assertThrows(IOException::class.java) { GitHubUpdateSource.parse(it, REPO) } }
    }

    @Test
    fun takesTheNewestGoogleTvOrPlainRelease() {
        val releases = list(
            release(tag = "android-v0.9.5"),
            release(tag = "google-tv-v0.9.1", notes = "\"TV notes\""),
            release(tag = "v0.9.0", notes = "\"Bridge\""),
            release(tag = "v0.8.1"),
        )
        val r = GitHubUpdateSource.parse(releases, REPO)
        assertEquals("0.9.1", r.versionName)
        assertEquals(901, r.versionCode)
        assertEquals("TV notes", r.notes)
        assertEquals("https://github.com/o/r/releases/download/google-tv-v0.9.1/app-release.apk", r.apkUrl)
    }

    @Test
    fun aPlainReleaseNewerThanThePlatformOneWins() {
        val r = GitHubUpdateSource.parse(list(release(tag = "google-tv-v0.9.1"), release(tag = "v0.10.0", notes = "\"All\"")), REPO)
        assertEquals("0.10.0", r.versionName)
        assertEquals("All", r.notes)
    }

    @Test
    fun skipsDraftsPreReleasesAndOtherPlatforms() {
        val r = GitHubUpdateSource.parse(
            list(
                release(tag = "google-tv-v0.9.3", draft = true),
                release(tag = "google-tv-v0.9.2", prerelease = true),
                release(tag = "mac-v2.0.0"),
                release(tag = "v0.8.1"),
            ),
            REPO,
        )
        assertEquals("0.8.1", r.versionName)
        assertThrows(IOException::class.java) { GitHubUpdateSource.parse(list(release(tag = "android-v1.0.0")), REPO) }
    }

    @Test
    fun prefersTheGoogleTvFileName() {
        val tvApk = APK.replace("app-release.apk", "app-release-google-tv.apk").replace("ABAB", "CDCD")
        val r = GitHubUpdateSource.parse(list(release(tag = "google-tv-v0.9.1", assets = "$APK,$tvApk")), REPO)
        assertEquals("https://github.com/o/r/releases/download/google-tv-v0.9.1/app-release-google-tv.apk", r.apkUrl)
        val only = GitHubUpdateSource.parse(list(release(tag = "google-tv-v0.9.1", assets = tvApk)), REPO)
        assertEquals("https://github.com/o/r/releases/download/google-tv-v0.9.1/app-release-google-tv.apk", only.apkUrl)
    }

    private companion object {
        val REPO = GitHubRepo.parse("o/r")!!
        const val APK = """{"name":"app-release.apk","size":6543210,"digest":"sha256:ABABABABABABABABABABABABABABABABABABABABABABABABABABABABABABABAB","content_type":"application/vnd.android.package-archive","browser_download_url":"https://github.com/o/r/releases/download/v0.7.0/app-release.apk"}"""
        const val AAB = """{"name":"app-release.aab","size":5000000,"browser_download_url":"https://github.com/o/r/releases/download/v0.7.0/app-release.aab"}"""
    }
}
