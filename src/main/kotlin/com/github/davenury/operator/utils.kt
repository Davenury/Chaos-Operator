package com.github.davenury.operator

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*

val httpClient = HttpClient(OkHttp) {
    expectSuccess = true
    install(ContentNegotiation) {
        jackson {
            this.registerKotlinModule()
        }
    }
}