package dev.shizzi

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import io.github.muntashirakon.adb.AbsAdbConnectionManager
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate
import java.util.Date
import javax.security.auth.x500.X500Principal

class LocalAdbConnectionManager private constructor() : AbsAdbConnectionManager() {
    private val keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }

    init {
        setApi(Build.VERSION.SDK_INT)
        ensureKey()
    }

    override fun getPrivateKey(): PrivateKey =
        keyStore.getKey(KEY_ALIAS, null) as PrivateKey

    override fun getCertificate(): Certificate =
        requireNotNull(keyStore.getCertificate(KEY_ALIAS))

    override fun getDeviceName(): String = "Shizzi"

    private fun ensureKey() {
        if (keyStore.containsAlias(KEY_ALIAS)) return

        val now = System.currentTimeMillis()
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY,
        )
            .setKeySize(2048)
            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setCertificateSubject(X500Principal("CN=Shizzi Local ADB"))
            .setCertificateSerialNumber(BigInteger.valueOf(now))
            .setCertificateNotBefore(Date(now - DAY_MS))
            .setCertificateNotAfter(Date(now + CERT_VALID_MS))
            .build()

        KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, KEYSTORE)
            .apply { initialize(spec) }
            .generateKeyPair()
    }

    companion object {
        @Volatile private var instance: LocalAdbConnectionManager? = null

        fun get(context: Context): LocalAdbConnectionManager {
            context.applicationContext // keep API explicit; no Activity is retained.
            return instance ?: synchronized(this) {
                instance ?: LocalAdbConnectionManager().also { instance = it }
            }
        }

        private const val KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "shizzi_local_adb"
        private const val DAY_MS = 86_400_000L
        private const val CERT_VALID_MS = 10L * 365L * DAY_MS
    }
}
