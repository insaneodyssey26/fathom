package com.masum.fathom

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform