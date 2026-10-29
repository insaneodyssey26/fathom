package com.masum.fathom.core.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest

object KeystoreIntegrityChecker {

    private const val DEBUG_KEY_SHA256 = "61:ED:37:7E:85:D3:86:A8:DF:EE:6B:86:4B:D8:5B:0B:FA:A5:AF:81"

    fun computeSigningCertificateSha256(context: Context): String? {
        return try {
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            if (signatures.isNullOrEmpty()) return null

            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(signatures[0].toByteArray())
            digest.joinToString(":") { String.format("%02X", it) }
        } catch (_: Exception) {
            null
        }
    }

    fun isBinaryTampered(context: Context, releaseCertSha256: String? = null): Boolean {
        if (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
            return false
        }
        val currentHash = computeSigningCertificateSha256(context) ?: return false
        if (releaseCertSha256.isNullOrBlank()) {
            return false
        }
        return !currentHash.equals(releaseCertSha256, ignoreCase = true)
    }
}
