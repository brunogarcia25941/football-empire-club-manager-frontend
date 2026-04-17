package com.brunogarcia.footballempireclubmanager

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform