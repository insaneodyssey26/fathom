package com.masum.fathom.core.content

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH

actual class Sha256Verifier actual constructor() {
    @OptIn(ExperimentalForeignApi::class)
    actual fun calculateSha256(bytes: ByteArray): String {
        val digest = UByteArray(CC_SHA256_DIGEST_LENGTH)
        bytes.usePinned { pinnedBytes ->
            digest.usePinned { pinnedDigest ->
                CC_SHA256(
                    pinnedBytes.addressOf(0),
                    bytes.size.toUInt(),
                    pinnedDigest.addressOf(0)
                )
            }
        }
        return digest.joinToString("") { it.toString(16).padStart(2, '0') }
    }
}
