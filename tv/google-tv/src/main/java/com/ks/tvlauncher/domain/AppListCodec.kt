package com.ks.tvlauncher.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * The last app list, kept between launches so Home can show it at once on a cold start
 * (specs/plan-per-platform-2.md, instant Home). Plain JSON with a format number; anything that
 * cannot be read counts as "no cache", never as an error.
 */
object AppListCodec {
    private const val FORMAT = 1
    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class Stored(val format: Int = FORMAT, val apps: List<StoredApp> = emptyList())

    @Serializable
    private data class StoredApp(
        val packageName: String,
        val activityName: String,
        val label: String,
        val versionCode: Long = 0L,
        val category: String? = null,
        val hasBanner: Boolean = false,
    )

    fun encode(apps: List<AppInfo>): String = json.encodeToString(
        Stored.serializer(),
        Stored(
            apps = apps.map {
                StoredApp(it.packageName, it.activityName, it.label, it.versionCode, it.category?.name, it.hasBanner)
            },
        ),
    )

    /** The stored list, or null when there is none or it cannot be read. */
    fun decode(text: String?): List<AppInfo>? {
        if (text.isNullOrBlank()) return null
        val stored = try {
            json.decodeFromString(Stored.serializer(), text)
        } catch (e: SerializationException) {
            return null
        } catch (e: IllegalArgumentException) {
            return null
        }
        if (stored.format != FORMAT) return null
        return stored.apps
            .filter { it.packageName.isNotBlank() && it.activityName.isNotBlank() }
            .map {
                AppInfo(
                    packageName = it.packageName,
                    activityName = it.activityName,
                    label = it.label.ifBlank { it.packageName },
                    versionCode = it.versionCode,
                    category = it.category?.let { name -> AppCategory.entries.firstOrNull { c -> c.name == name } },
                    hasBanner = it.hasBanner,
                )
            }
    }
}
