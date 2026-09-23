package com.masum.fathom.core.model

data class Book(
    val id: String,
    val traditionId: TraditionId,
    val title: String,
    val author: String?,
    val language: String,
    val totalPassages: Int,
    val isBundled: Boolean = false
)
