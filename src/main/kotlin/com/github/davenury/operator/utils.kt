package com.github.davenury.operator

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*

val httpClient = HttpClient(OkHttp) {
    expectSuccess = true
}