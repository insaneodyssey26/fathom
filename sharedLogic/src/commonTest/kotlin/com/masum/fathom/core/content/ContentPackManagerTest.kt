package com.masum.fathom.core.content

import com.masum.fathom.core.common.result.ContentError
import com.masum.fathom.core.common.result.FathomResult
import com.masum.fathom.core.model.TraditionId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContentPackManagerTest {

    private val verifier = Sha256Verifier()
    private val manager = ContentPackManager(verifier)

    @Test
    fun givenValidBytesAndMatchingSha256_thenValidationSucceeds() {
        val payload = "Fathom Ancient Wisdom Content".encodeToByteArray()
        val validHash = verifier.calculateSha256(payload)

        val metadata = ContentPackMetadata(
            packId = "pack_gita_v1",
            traditionId = TraditionId.HINDUISM,
            bookId = "bhagavad_gita",
            title = "Bhagavad Gita",
            version = 1,
            sizeBytes = payload.size.toLong(),
            sha256Checksum = validHash,
            downloadUrl = "https://cdn.fathom.app/packs/gita_v1.db"
        )

        val result = manager.verifyAndValidatePack(metadata, payload)
        assertTrue(result is FathomResult.Success)
    }

    @Test
    fun givenTamperedBytes_thenValidationFailsWithIntegrityError() {
        val originalPayload = "Fathom Ancient Wisdom Content".encodeToByteArray()
        val validHash = verifier.calculateSha256(originalPayload)

        val tamperedPayload = "Tampered Malicious Content".encodeToByteArray()

        val metadata = ContentPackMetadata(
            packId = "pack_gita_v1",
            traditionId = TraditionId.HINDUISM,
            bookId = "bhagavad_gita",
            title = "Bhagavad Gita",
            version = 1,
            sizeBytes = originalPayload.size.toLong(),
            sha256Checksum = validHash,
            downloadUrl = "https://cdn.fathom.app/packs/gita_v1.db"
        )

        val result = manager.verifyAndValidatePack(metadata, tamperedPayload)
        assertTrue(result is FathomResult.Failure)
        assertEquals(ContentError.Pack.INTEGRITY_CHECK_FAILED, result.error)
    }
}
