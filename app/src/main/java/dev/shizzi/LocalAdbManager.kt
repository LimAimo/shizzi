package dev.shizzi

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import io.github.muntashirakon.adb.AdbPairingRequiredException
import io.github.muntashirakon.adb.android.AdbMdns
import java.net.InetAddress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume

object LocalAdbManager {
    private const val PREFS = "local_adb"
    private const val KEY_PAIRED = "paired"
    private const val DISCOVERY_TIMEOUT_MS = 30_000L

    fun isSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    fun wasPaired(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_PAIRED, false)

    fun openWirelessDebuggingSettings(context: Context) {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    suspend fun pair(context: Context, pairingCode: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            check(isSupported()) { "Wireless debugging requires Android 11 or newer" }
            val endpoint = discover(context, AdbMdns.SERVICE_TYPE_TLS_PAIRING)
            val manager = LocalAdbConnectionManager.get(context)
            check(manager.pair(endpoint.address, endpoint.port, pairingCode.trim())) {
                "ADB pairing was rejected"
            }
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_PAIRED, true).apply()
            connectOrThrow(context)
        }
    }

    suspend fun connect(context: Context): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching { connectOrThrow(context) }
    }

    private fun connectOrThrow(context: Context) {
        check(isSupported()) { "Wireless debugging requires Android 11 or newer" }
        val manager = LocalAdbConnectionManager.get(context)
        if (manager.isConnected) return
        try {
            check(manager.autoConnect(context.applicationContext, 8_000L)) {
                "Could not connect to the local ADB daemon"
            }
        } catch (required: AdbPairingRequiredException) {
            throw IllegalStateException("Wireless debugging needs pairing", required)
        }
    }

    private suspend fun discover(context: Context, serviceType: String): Endpoint =
        withTimeout(DISCOVERY_TIMEOUT_MS) {
            suspendCancellableCoroutine { continuation ->
                var mdns: AdbMdns? = null
                mdns = AdbMdns(context.applicationContext, serviceType) { host: InetAddress?, port: Int ->
                    if (!continuation.isActive || host == null || port <= 0) return@AdbMdns
                    mdns?.stop()
                    continuation.resume(Endpoint(host.hostAddress ?: "127.0.0.1", port))
                }
                continuation.invokeOnCancellation { mdns?.stop() }
                mdns!!.start()
            }
        }

    data class Endpoint(val address: String, val port: Int)
}
