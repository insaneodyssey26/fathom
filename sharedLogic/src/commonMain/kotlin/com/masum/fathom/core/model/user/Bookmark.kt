package com.masum.fathom.core.model.user

data class Bookmark(
    val id: String,
    val passageId: String,
    val createdAtEpochMs: Long,
    val tags: List<String> = emptyList()
)
