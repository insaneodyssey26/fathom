package com.masum.fathom.core.content

import com.masum.fathom.core.model.TraditionId

data class ContentPackMetadata(
    val packId: String,
    val traditionId: TraditionId,
    val bookId: String,
    val title: String,
    val version: Int,
    val sizeBytes: Long,
    val sha256Checksum: String,
    val downloadUrl: String
)

data class ContentCatalogManifest(
    val schemaVersion: Int,
    val catalogVersion: Int,
    val availablePacks: List<ContentPackMetadata>
)
