package com.ks.tvlauncher.data.updates

import com.ks.tvlauncher.domain.AppUpdates
import com.ks.tvlauncher.domain.ReleaseInfo
import com.ks.utility.appupdate.AppUpdate
import com.ks.utility.appupdate.GitHubRepo
import com.ks.utility.appupdate.UpdatePlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Where KS learns about its newest release. */
interface UpdateSource {
    /**
     * The newest published release for Google TV (a `google-tv-v…` tag or a plain `v…` one);
     * throws [RateLimitedException] on HTTP 403/429, else [IOException].
     */
    @Throws(IOException::class)
    suspend fun latest(): ReleaseInfo
}

/** GitHub asked KS to slow down (HTTP 403 or 429): try again tomorrow. */
class RateLimitedException(code: Int) : IOException("HTTP $code")

/**
 * GitHub Releases of this repository: public, no token. Sends only the shared request headers
 * (ks-utility `GitHubRepo.requestHeaders`: `Accept`, the API version and a generic `User-Agent`).
 * Plain `HttpURLConnection` on [Dispatchers.IO], like the weather providers.
 */
class GitHubUpdateSource(private val url: String = RELEASES_URL) : UpdateSource {

    override suspend fun latest(): ReleaseInfo = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            REPO.requestHeaders.forEach { (name, value) -> connection.setRequestProperty(name, value) }
            when (val code = connection.responseCode) {
                in 200..299 -> parse(connection.inputStream.bufferedReader().use { it.readText() })
                403, 429 -> throw RateLimitedException(code)
                else -> throw IOException("HTTP $code")
            }
        } finally {
            connection.disconnect()
        }
    }

    @Serializable
    private data class Release(
        @SerialName("tag_name") val tag: String,
        val body: String? = null,
    )

    companion object {
        /** This app's releases (the shared app-update rules, ks-utility `specs/app-update.md`). */
        val REPO: GitHubRepo = requireNotNull(GitHubRepo.parse("kumasumit/ks-tv-launcher"))
        /**
         * The release list, not `releases/latest`: a `google-tv-v…` release is not marked latest
         * (the suite's platform tags; ks-utility `specs/app-update.md`).
         */
        val RELEASES_URL: String = REPO.releasesApiUrl
        /**
         * The release file. A `google-tv-v…` release is built through the shared workflow's
         * `tv-module`, which names it [TV_APK_NAME]; plain releases up to the bridge release carry
         * [APK_NAME], the name copies before v0.9.0 look for. The TV name is preferred.
         */
        const val TV_APK_NAME = "app-release-google-tv.apk"
        const val APK_NAME = "app-release.apk"
        private const val TIMEOUT_MS = 15_000

        private val json = Json { ignoreUnknownKeys = true }

        /**
         * Parses the release list: the newest release whose tag is `google-tv-vX.Y.Z` or a plain
         * `vX.Y.Z` (never another platform's tag, a draft or a pre-release; ks-utility
         * `AppUpdate.parseReleases`). Throws [IOException] when there is none, for a version KS
         * cannot number, or with no usable `app-release.apk`: the shared rules keep only a file of
         * this repository's releases (`https://github.com/kumasumit/ks-tv-launcher/releases/download/…`),
         * with a size and GitHub's `sha256:` digest.
         */
        @Throws(IOException::class)
        fun parse(body: String, repo: GitHubRepo = REPO): ReleaseInfo {
            val release = AppUpdate.parseReleases(body, repo, UpdatePlatform.GOOGLE_TV)
                ?: throw IOException("No Google TV release in the response")
            val code = AppUpdates.versionCode(release.version) ?: throw IOException("Malformed tag ${release.tag}")
            val name = AppUpdates.versionName(release.version) ?: throw IOException("Malformed tag ${release.tag}")
            val apk = release.find { it == TV_APK_NAME } ?: release.find { it == APK_NAME }
                ?: throw IOException("No usable $TV_APK_NAME or $APK_NAME (from this repository, with a size and a SHA-256)")
            return ReleaseInfo(
                versionName = name,
                versionCode = code,
                notes = notesOf(body, release.tag).trim().take(AppUpdates.MAX_NOTES),
                apkUrl = apk.url,
                apkSize = apk.size,
                apkSha256 = apk.sha256,
            )
        }

        /** The release notes of [tag] in the list; empty when missing. */
        private fun notesOf(body: String, tag: String): String = try {
            json.decodeFromString(ListSerializer(Release.serializer()), body)
                .firstOrNull { it.tag.trim() == tag }?.body.orEmpty()
        } catch (e: SerializationException) {
            ""
        } catch (e: IllegalArgumentException) {
            ""
        }
    }
}
