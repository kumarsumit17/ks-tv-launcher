package com.ks.tvlauncher.data.updates

import com.ks.tvlauncher.domain.AppUpdates
import com.ks.tvlauncher.domain.ReleaseInfo
import com.ks.utility.appupdate.AppUpdate
import com.ks.utility.appupdate.GitHubRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Where KS learns about its newest release. */
interface UpdateSource {
    /** The newest published release; throws [RateLimitedException] on HTTP 403/429, else [IOException]. */
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
class GitHubUpdateSource(private val url: String = LATEST_URL) : UpdateSource {

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
        val REPO: GitHubRepo = requireNotNull(GitHubRepo.parse("kumarsumit17/ks-tv-launcher"))
        val LATEST_URL: String = REPO.latestReleaseApiUrl
        const val APK_NAME = "app-release.apk"
        private const val TIMEOUT_MS = 15_000

        private val json = Json { ignoreUnknownKeys = true }

        /**
         * Parses `releases/latest`. Throws [IOException] for a malformed tag, or no usable
         * `app-release.apk`: the shared rules keep only a file of this repository's releases
         * (`https://github.com/kumarsumit17/ks-tv-launcher/releases/download/…`), with a size and
         * GitHub's `sha256:` digest.
         */
        @Throws(IOException::class)
        fun parse(body: String, repo: GitHubRepo = REPO): ReleaseInfo {
            val release = try {
                json.decodeFromString(Release.serializer(), body)
            } catch (e: SerializationException) {
                throw IOException("Unexpected release response", e)
            } catch (e: IllegalArgumentException) {
                throw IOException("Unexpected release response", e)
            }
            val code = AppUpdates.versionCode(release.tag) ?: throw IOException("Malformed tag ${release.tag}")
            val name = AppUpdates.versionName(release.tag) ?: throw IOException("Malformed tag ${release.tag}")
            val apk = AppUpdate.parseRelease(body, repo)?.find { it == APK_NAME }
                ?: throw IOException("No usable $APK_NAME (from this repository, with a size and a SHA-256)")
            return ReleaseInfo(
                versionName = name,
                versionCode = code,
                notes = release.body.orEmpty().trim().take(AppUpdates.MAX_NOTES),
                apkUrl = apk.url,
                apkSize = apk.size,
                apkSha256 = apk.sha256,
            )
        }
    }
}
