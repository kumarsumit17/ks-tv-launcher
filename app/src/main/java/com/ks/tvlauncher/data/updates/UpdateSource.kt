package com.ks.tvlauncher.data.updates

import com.ks.tvlauncher.domain.AppUpdates
import com.ks.tvlauncher.domain.ReleaseInfo
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
 * GitHub Releases of this repository: public, no token. Sends only a generic `User-Agent`.
 * Plain `HttpURLConnection` on [Dispatchers.IO], like the weather providers.
 */
class GitHubUpdateSource(private val url: String = LATEST_URL) : UpdateSource {

    override suspend fun latest(): ReleaseInfo = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.setRequestProperty("User-Agent", USER_AGENT)
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
        val assets: List<Asset> = emptyList(),
    )

    @Serializable
    private data class Asset(
        val name: String,
        @SerialName("browser_download_url") val url: String,
        val size: Long,
    )

    companion object {
        const val LATEST_URL = "https://api.github.com/repos/kumarsumit17/ks-tv-launcher/releases/latest"
        const val APK_NAME = "app-release.apk"
        const val USER_AGENT = "KS-TV-Launcher"
        private const val TIMEOUT_MS = 15_000

        private val json = Json { ignoreUnknownKeys = true }

        /**
         * Parses `releases/latest`. Throws [IOException] for a malformed tag, a missing
         * `app-release.apk`, a non-HTTPS download link or an empty asset.
         */
        @Throws(IOException::class)
        fun parse(body: String): ReleaseInfo {
            val release = try {
                json.decodeFromString(Release.serializer(), body)
            } catch (e: SerializationException) {
                throw IOException("Unexpected release response", e)
            } catch (e: IllegalArgumentException) {
                throw IOException("Unexpected release response", e)
            }
            val code = AppUpdates.versionCode(release.tag) ?: throw IOException("Malformed tag ${release.tag}")
            val name = AppUpdates.versionName(release.tag) ?: throw IOException("Malformed tag ${release.tag}")
            val apk = release.assets.firstOrNull { it.name == APK_NAME } ?: throw IOException("No $APK_NAME")
            if (!apk.url.startsWith("https://")) throw IOException("Download is not HTTPS")
            if (apk.size <= 0) throw IOException("Empty $APK_NAME")
            return ReleaseInfo(
                versionName = name,
                versionCode = code,
                notes = release.body.orEmpty().trim().take(AppUpdates.MAX_NOTES),
                apkUrl = apk.url,
                apkSize = apk.size,
            )
        }
    }
}
