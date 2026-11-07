package com.masum.fathom.core.security

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import com.masum.fathom.BuildConfig
import java.io.File
import java.security.MessageDigest

object KeystoreIntegrityChecker {

    private fun decodeMasked(bytes: ByteArray, salt: Byte = 0x55.toByte()): String {
        val out = ByteArray(bytes.size)
        for (i in bytes.indices) {
            out[i] = (bytes[i].toInt() xor salt.toInt()).toByte()
        }
        return String(out, Charsets.UTF_8)
    }

    private val PLAY_STORE_SIGNING_FINGERPRINT = decodeMasked(
        byteArrayOf(
            54, 108, 103, 100, 109, 48, 97, 100, 99, 96, 54, 52, 98, 51, 54, 108,
            51, 48, 109, 54, 98, 100, 52, 100, 51, 96, 96, 108, 109, 54, 100, 99,
            54, 49, 52, 51, 98, 109, 51, 97, 48, 48, 55, 97, 49, 102, 49, 101,
            109, 109, 102, 100, 49, 49, 55, 101, 100, 54, 48, 55, 99, 96, 99, 48
        )
    )

    private val LOCAL_UPLOAD_KEY_FINGERPRINT = decodeMasked(
        byteArrayOf(
            100, 97, 49, 109, 103, 97, 96, 49, 108, 54, 101, 54, 98, 55, 52, 48,
            97, 48, 96, 100, 102, 98, 101, 102, 98, 102, 102, 101, 96, 51, 97, 49,
            102, 108, 102, 51, 100, 54, 108, 109, 51, 55, 54, 108, 108, 52, 54, 96,
            99, 100, 108, 48, 98, 101, 102, 101, 98, 98, 96, 55, 102, 103, 96, 99
        )
    )

    private val DEBUG_KEY_FINGERPRINT = decodeMasked(
        byteArrayOf(
            51, 96, 98, 100, 52, 55, 102, 101, 97, 52, 98, 109, 101, 97, 98, 48,
            101, 100, 52, 97, 48, 109, 48, 102, 101, 100, 98, 49, 97, 52, 97, 108,
            48, 100, 96, 99, 102, 99, 102, 49, 96, 102, 100, 102, 54, 96, 98, 103,
            97, 108, 99, 100, 102, 100, 54, 55, 98, 101, 52, 101, 109, 52, 101, 52
        )
    )

    private val SUSPICIOUS_FRAMEWORK_CLASSES = listOf(
        byteArrayOf(49, 48, 123, 39, 58, 55, 35, 123, 52, 59, 49, 39, 58, 60, 49, 123, 45, 37, 58, 38, 48, 49, 123, 13, 37, 58, 38, 48, 49, 23, 39, 60, 49, 50, 48),
        byteArrayOf(49, 48, 123, 39, 58, 55, 35, 123, 52, 59, 49, 39, 58, 60, 49, 123, 45, 37, 58, 38, 48, 49, 123, 13, 37, 58, 38, 48, 49, 29, 48, 57, 37, 48, 39, 38),
        byteArrayOf(58, 39, 50, 123, 57, 38, 37, 58, 38, 48, 49, 123, 57, 38, 37, 49, 123, 59, 52, 33, 60, 35, 48, 55, 39, 60, 49, 50, 48),
        byteArrayOf(54, 58, 56, 123, 48, 57, 49, 48, 39, 49, 39, 60, 35, 48, 39, 38, 123, 39, 60, 39, 32, 123, 7, 60, 39, 32),
        byteArrayOf(60, 58, 123, 50, 60, 33, 61, 32, 55, 123, 57, 38, 37, 58, 38, 48, 49, 123, 54, 58, 39, 48, 123, 24, 52, 60, 59)
    )

    private val SUSPICIOUS_MAP_ENTRIES = listOf(
        byteArrayOf(51, 39, 60, 49, 52, 120, 52, 50, 48, 59, 33),
        byteArrayOf(51, 39, 60, 49, 52, 120, 50, 52, 49, 50, 48, 33),
        byteArrayOf(50, 52, 49, 50, 48, 33, 123, 38, 58),
        byteArrayOf(57, 60, 55, 51, 39, 60, 49, 52),
        byteArrayOf(45, 37, 58, 38, 48, 49, 123, 37, 39, 58, 37),
        byteArrayOf(13, 37, 58, 38, 48, 49, 23, 39, 60, 49, 50, 48, 123, 63, 52, 39)
    )

    private var cachedIntegrityResult: Boolean? = null

    fun isBinaryTampered(context: Context): Boolean {
        if (BuildConfig.DEBUG) return false
        cachedIntegrityResult?.let { return !it }
        val isValid = verifyCertificateSignature(context) && !isHookingDetected()
        cachedIntegrityResult = isValid
        return !isValid
    }

    @SuppressLint("PackageManagerGetSignatures")
    private fun verifyCertificateSignature(context: Context): Boolean {
        if (BuildConfig.DEBUG) return true

        return try {
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                val signingInfo = packageInfo.signingInfo ?: return false
                if (signingInfo.hasMultipleSigners()) {
                    signingInfo.apkContentsSigners
                } else {
                    signingInfo.signingCertificateHistory
                }
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            if (signatures.isNullOrEmpty()) return false

            val md = MessageDigest.getInstance("SHA-256")
            signatures.any { sig ->
                val digest = md.digest(sig.toByteArray()).joinToString("") { "%02x".format(it) }
                digest.equals(PLAY_STORE_SIGNING_FINGERPRINT, ignoreCase = true) ||
                    digest.equals(LOCAL_UPLOAD_KEY_FINGERPRINT, ignoreCase = true) ||
                    (BuildConfig.DEBUG && digest.equals(DEBUG_KEY_FINGERPRINT, ignoreCase = true))
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun isHookingDetected(): Boolean {
        if (BuildConfig.DEBUG) return false

        return try {
            if (Debug.isDebuggerConnected()) return true
            if (hasHookClasses()) return true
            if (hasSuspiciousMemoryMappings()) return true
            false
        } catch (_: Exception) {
            false
        }
    }

    private fun hasHookClasses(): Boolean {
        return SUSPICIOUS_FRAMEWORK_CLASSES.any { masked ->
            val className = decodeMasked(masked)
            try {
                Class.forName(className)
                true
            } catch (_: ClassNotFoundException) {
                false
            }
        }
    }

    private fun hasSuspiciousMemoryMappings(): Boolean {
        return try {
            val mapsFile = File("/proc/self/maps")
            if (!mapsFile.exists() || !mapsFile.canRead()) return false
            val patterns = SUSPICIOUS_MAP_ENTRIES.map { decodeMasked(it).lowercase() }
            mapsFile.useLines { lines ->
                lines.any { line ->
                    val lower = line.lowercase()
                    patterns.any { lower.contains(it) }
                }
            }
        } catch (_: Exception) {
            false
        }
    }
}
