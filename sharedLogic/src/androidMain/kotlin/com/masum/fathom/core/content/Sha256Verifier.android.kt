package com.masum.fathom.core.content

import java.security.MessageDigest

actual class Sha256Verifier actual constructor() {
    actual fun calculateSha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(bytes)
        return hash.joinToString("") { "%02x".format(it) }
    }
}
