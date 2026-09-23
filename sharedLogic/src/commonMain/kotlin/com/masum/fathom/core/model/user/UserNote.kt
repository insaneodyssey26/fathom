package com.masum.fathom.core.model.user

data class UserNote(
    val id: String,
    val passageId: String,
    val content: String,
    val createdAtEpochMs: Long,
    val updatedAtEpochMs: Long
)
