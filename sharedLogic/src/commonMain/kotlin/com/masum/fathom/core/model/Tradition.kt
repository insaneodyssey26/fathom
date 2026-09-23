package com.masum.fathom.core.model

data class Tradition(
    val id: TraditionId,
    val description: String,
    val defaultBookIds: List<String> = emptyList()
)
