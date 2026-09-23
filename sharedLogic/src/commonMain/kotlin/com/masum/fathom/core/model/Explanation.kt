package com.masum.fathom.core.model

data class Explanation(
    val id: String,
    val passageId: String,
    val title: String?,
    val body: String,
    val author: String?,
    val source: String?
)
