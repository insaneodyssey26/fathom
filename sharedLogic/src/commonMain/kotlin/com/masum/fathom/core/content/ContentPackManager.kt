package com.masum.fathom.core.content

import com.masum.fathom.core.common.result.ContentError
import com.masum.fathom.core.common.result.FathomResult

expect class Sha256Verifier() {
    fun calculateSha256(bytes: ByteArray): String
}

class ContentPackManager(
    private val sha256Verifier: Sha256Verifier
) {
    fun verifyAndValidatePack(
        metadata: ContentPackMetadata,
        downloadedBytes: ByteArray
    ): FathomResult<Unit, ContentError.Pack> {
        if (downloadedBytes.size.toLong() != metadata.sizeBytes) {
            return FathomResult.Failure(ContentError.Pack.INTEGRITY_CHECK_FAILED)
        }

        val calculatedHash = sha256Verifier.calculateSha256(downloadedBytes)
        if (!calculatedHash.equals(metadata.sha256Checksum, ignoreCase = true)) {
            return FathomResult.Failure(ContentError.Pack.INTEGRITY_CHECK_FAILED)
        }

        return FathomResult.Success(Unit)
    }
}
