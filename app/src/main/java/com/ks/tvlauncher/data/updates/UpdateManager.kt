package com.ks.tvlauncher.data.updates

import com.ks.utility.appupdate.Sha256
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import com.ks.tvlauncher.BuildConfig
import com.ks.tvlauncher.domain.AppUpdates
import com.ks.tvlauncher.domain.ReleaseInfo
import com.ks.tvlauncher.domain.UpdateVerdict
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/** Where the About update flow is. */
sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data object Waiting : UpdateState
    data object CheckFailed : UpdateState
    data class Downloading(val percent: Int) : UpdateState
    data object Verifying : UpdateState
    /** Verified; waiting for "Install unknown apps" to be allowed for KS. */
    data object NeedsPermission : UpdateState
    /** Handed to Android; its confirmation is on screen. */
    data object Installing : UpdateState
    data class Failed(val reason: UpdateFailure) : UpdateState
}

enum class UpdateFailure { DOWNLOAD, NOT_VERIFIED, INSTALL, CANCELLED }

/**
 * Check → download (with progress, cancellable) → verify → install, for the About section.
 * Lives as long as the process so a download continues if Settings closes. Release builds
 * only; the file is deleted on every failure and after the update.
 */
class UpdateManager(
    context: Context,
    private val scope: CoroutineScope,
    private val checker: UpdateChecker,
    private val store: UpdateStore,
    private val notifier: UpdateNotifier,
) {
    private val context = context.applicationContext
    private val dir = File(this.context.cacheDir, "updates")
    private val apk = File(dir, "update.apk")

    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    val available: StateFlow<ReleaseInfo?> = store.available.stateIn(scope, SharingStarted.WhileSubscribed(5_000), null)

    private var job: Job? = null
    private var receiver: BroadcastReceiver? = null

    private val busy: Boolean
        get() = _state.value.let {
            it is UpdateState.Checking || it is UpdateState.Downloading || it is UpdateState.Verifying || it is UpdateState.Installing
        }

    /** "Check for updates". */
    fun check() {
        if (!BuildConfig.IN_APP_UPDATES || busy) return
        _state.value = UpdateState.Checking
        job = scope.launch {
            _state.value = when (checker.check(automatic = false)) {
                CheckResult.UpToDate -> UpdateState.UpToDate
                is CheckResult.Available -> UpdateState.Idle
                CheckResult.Waiting -> UpdateState.Waiting
                CheckResult.Failed -> UpdateState.CheckFailed
            }
        }
    }

    /** "Download and install": download, verify, then install (or ask for the permission). */
    fun start() {
        if (!BuildConfig.IN_APP_UPDATES || busy) return
        _state.value = UpdateState.Downloading(0)
        job = scope.launch {
            val release = store.available.first()
            if (release == null) {
                _state.value = UpdateState.Idle
                return@launch
            }
            try {
                download(release)
            } catch (e: CancellationException) {
                deleteFile()
                _state.value = UpdateState.Idle
                throw e
            } catch (e: IOException) {
                deleteFile()
                _state.value = UpdateState.Failed(UpdateFailure.DOWNLOAD)
                return@launch
            }
            _state.value = UpdateState.Verifying
            val verdict = withContext(Dispatchers.IO) { verify(release) }
            if (verdict != UpdateVerdict.OK) {
                deleteFile()
                _state.value = UpdateState.Failed(UpdateFailure.NOT_VERIFIED)
                return@launch
            }
            install()
        }
    }

    /** Cancel during download (or verify): the file is deleted, the old version stays. */
    fun cancel() {
        job?.cancel()
        job = null
        if (_state.value !is UpdateState.Installing) {
            deleteFile()
            _state.value = UpdateState.Idle
        }
    }

    /** Back in KS (from Android settings): continue once installing apps is allowed. */
    fun onResumed() {
        if (_state.value == UpdateState.NeedsPermission && canInstall()) install()
    }

    /** The verified file is there: install now, or first ask to allow installing apps. */
    fun install() {
        if (!apk.isFile) {
            _state.value = UpdateState.Failed(UpdateFailure.INSTALL)
            return
        }
        if (!canInstall()) {
            _state.value = UpdateState.NeedsPermission
            val ask = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, "package:${context.packageName}".toUri())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (startActivity(ask)) return
            // No such page on this TV: Android asks during the install instead.
        }
        _state.value = UpdateState.Installing
        scope.launch(Dispatchers.IO) {
            try {
                commitSession()
            } catch (e: IOException) {
                finish(UpdateState.Failed(UpdateFailure.INSTALL))
            } catch (e: SecurityException) {
                finish(UpdateState.Failed(UpdateFailure.INSTALL))
            }
        }
    }

    /** Clears the message after the user has seen it (Settings closed). */
    fun reset() {
        if (!busy && _state.value != UpdateState.NeedsPermission) _state.value = UpdateState.Idle
    }

    /**
     * Run once, well after start: forgets a release that is now installed, withdraws its
     * notification and deletes any leftover download.
     */
    suspend fun cleanUpAfterStart() {
        if (!BuildConfig.IN_APP_UPDATES) return
        if (checker.clearIfInstalled(store.available.first())) notifier.cancel()
        if (!busy && _state.value != UpdateState.NeedsPermission) withContext(Dispatchers.IO) { dir.deleteRecursively() }
    }

    private suspend fun download(release: ReleaseInfo) = withContext(Dispatchers.IO) {
        if (!release.apkUrl.startsWith("https://")) throw IOException("Not HTTPS")
        dir.mkdirs()
        apk.delete()
        val connection = URL(release.apkUrl).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.setRequestProperty("User-Agent", GitHubUpdateSource.REPO.requestHeaders.getValue("User-Agent"))
            val code = connection.responseCode
            if (code !in 200..299) throw IOException("HTTP $code")
            // Redirects (to GitHub's file host) must stay on HTTPS.
            if (connection.url.protocol != "https") throw IOException("Redirected off HTTPS")
            var done = 0L
            var shown = -1
            connection.inputStream.use { input ->
                apk.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        ensureActive()
                        val read = input.read(buffer)
                        if (read < 0) break
                        done += read
                        if (done > release.apkSize) throw IOException("Larger than announced")
                        output.write(buffer, 0, read)
                        val percent = (done * 100 / release.apkSize).toInt()
                        if (percent != shown) {
                            shown = percent
                            _state.value = UpdateState.Downloading(percent)
                        }
                    }
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun verify(release: ReleaseInfo): UpdateVerdict {
        val pm = context.packageManager
        val archive = archiveInfo(pm, apk.path)
        val own = ownInfo(pm)
        return AppUpdates.verify(
            expectedSize = release.apkSize,
            actualSize = apk.length(),
            expectedSha256 = release.apkSha256,
            actualSha256 = if (apk.length() == release.apkSize) Sha256.hex(apk) else "",
            archivePackage = archive?.packageName,
            ownPackage = context.packageName,
            archiveSigners = archive?.let(::signers).orEmpty(),
            ownSigners = own?.let(::signers).orEmpty(),
            archiveVersionCode = archive?.let(PackageInfoCompat::getLongVersionCode) ?: 0L,
            installedVersionCode = own?.let(PackageInfoCompat::getLongVersionCode) ?: Long.MAX_VALUE,
        )
    }

    private fun commitSession() {
        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            setAppPackageName(context.packageName)
            setSize(apk.length())
            // Android confirms every install; KS never asks to skip that.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_REQUIRED)
            }
        }
        val sessionId = installer.createSession(params)
        registerReceiver()
        try {
            installer.openSession(sessionId).use { session ->
                session.openWrite("ks-update", 0, apk.length()).use { out ->
                    apk.inputStream().use { it.copyTo(out) }
                    session.fsync(out)
                }
                val status = Intent(ACTION_STATUS).setPackage(context.packageName)
                val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0)
                session.commit(PendingIntent.getBroadcast(context, sessionId, status, flags).intentSender)
            }
        } catch (e: IOException) {
            runCatching { installer.abandonSession(sessionId) }
            throw e
        }
    }

    /** Registered at runtime for this install only: KS declares no receiver in its manifest. */
    private fun registerReceiver() {
        if (receiver != null) return
        val r = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) = onStatus(intent)
        }
        receiver = r
        ContextCompat.registerReceiver(context, r, IntentFilter(ACTION_STATUS), ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    private fun onStatus(intent: Intent) {
        when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirm = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_INTENT, Intent::class.java)
                if (confirm == null || !startActivity(confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))) {
                    finish(UpdateState.Failed(UpdateFailure.INSTALL))
                }
            }
            // Android now replaces and restarts KS; usually this process ends first.
            PackageInstaller.STATUS_SUCCESS -> finish(UpdateState.Idle)
            PackageInstaller.STATUS_FAILURE_ABORTED -> finish(UpdateState.Failed(UpdateFailure.CANCELLED))
            else -> finish(UpdateState.Failed(UpdateFailure.INSTALL))
        }
    }

    private fun finish(state: UpdateState) {
        receiver?.let { runCatching { context.unregisterReceiver(it) } }
        receiver = null
        deleteFile()
        _state.value = state
    }

    private fun canInstall(): Boolean = context.packageManager.canRequestPackageInstalls()

    private fun startActivity(intent: Intent): Boolean = try {
        context.startActivity(intent)
        true
    } catch (e: android.content.ActivityNotFoundException) {
        false
    } catch (e: SecurityException) {
        false
    }

    private fun deleteFile() {
        apk.delete()
    }

    private companion object {
        const val TIMEOUT_MS = 30_000
        val ACTION_STATUS = "${BuildConfig.APPLICATION_ID}.action.UPDATE_INSTALL_STATUS"

        @Suppress("DEPRECATION")
        val SIGNER_FLAGS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            PackageManager.GET_SIGNATURES
        }

        @Suppress("DEPRECATION")
        fun archiveInfo(pm: PackageManager, path: String): PackageInfo? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageArchiveInfo(path, PackageManager.PackageInfoFlags.of(SIGNER_FLAGS.toLong()))
            } else {
                pm.getPackageArchiveInfo(path, SIGNER_FLAGS)
            }

        @Suppress("DEPRECATION")
        fun ownInfo(pm: PackageManager): PackageInfo? = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(BuildConfig.APPLICATION_ID, PackageManager.PackageInfoFlags.of(SIGNER_FLAGS.toLong()))
            } else {
                pm.getPackageInfo(BuildConfig.APPLICATION_ID, SIGNER_FLAGS)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }

        /** SHA-256 (hex) of each certificate the APK contents are signed with. */
        @SuppressLint("PackageManagerGetSignatures")
        @Suppress("DEPRECATION")
        fun signers(info: PackageInfo): Set<String> {
            val certs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.signingInfo?.apkContentsSigners
            } else {
                info.signatures
            }
            val digest = MessageDigest.getInstance("SHA-256")
            return certs.orEmpty().map { sig ->
                digest.digest(sig.toByteArray()).joinToString("") { "%02x".format(it) }
            }.toSet()
        }
    }
}
