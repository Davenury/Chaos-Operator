package com.github.davenury.operator

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.fabric8.kubernetes.client.utils.Serialization
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {

        Serialization.jsonMapper().registerKotlinModule()

        val operator = ScenarioOperator()
        operator.run()

        routing {
            metaRouting()
        }
    }.start(wait = true)
}