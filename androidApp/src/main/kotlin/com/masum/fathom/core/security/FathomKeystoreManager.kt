package com.masum.fathom.core.security

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import android.util.Base64
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey

object FathomKeystoreManager {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val ALIAS_ENTITLEMENT_HMAC = "fathom_entitlement_hmac_key"
    private const val HMAC_ALGORITHM = "HmacSHA256"

    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    private fun getHmacSecretKey(): SecretKey {
        if (!keyStore.containsAlias(ALIAS_ENTITLEMENT_HMAC)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_HMAC_SHA256, ANDROID_KEYSTORE)
            val builder = KeyGenParameterSpec.Builder(
                ALIAS_ENTITLEMENT_HMAC,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                try {
                    builder.setIsStrongBoxBacked(true)
                    keyGenerator.init(builder.build())
                    keyGenerator.generateKey()
                    return keyStore.getKey(ALIAS_ENTITLEMENT_HMAC, null) as SecretKey
                } catch (_: StrongBoxUnavailableException) {
                    builder.setIsStrongBoxBacked(false)
                }
            }
            keyGenerator.init(builder.build())
            keyGenerator.generateKey()
        }
        return keyStore.getKey(ALIAS_ENTITLEMENT_HMAC, null) as SecretKey
    }

    fun generateEntitlementHmac(payload: String): String {
        return try {
            val mac = Mac.getInstance(HMAC_ALGORITHM)
            mac.init(getHmacSecretKey())
            val bytes = mac.doFinal(payload.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (_: Exception) {
            ""
        }
    }

    fun verifyEntitlementHmac(payload: String, expectedHmac: String): Boolean {
        if (expectedHmac.isBlank()) return false
        val computed = generateEntitlementHmac(payload)
        if (computed.isBlank()) return false
        return MessageDigest.isEqual(
            computed.toByteArray(Charsets.UTF_8),
            expectedHmac.toByteArray(Charsets.UTF_8)
        )
    }
}
