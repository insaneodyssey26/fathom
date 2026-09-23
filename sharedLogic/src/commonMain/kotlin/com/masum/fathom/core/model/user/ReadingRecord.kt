package com.masum.fathom.core.model.user

data class ReadingRecord(
    val passageId: String,
    val bookId: String,
    val firstSeenAtEpochMs: Long,
    val lastSeenAtEpochMs: Long,
    val readCount: Int = 1,
    val isCompleted: Boolean = false
)
