package com.faisal.pam

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform