package com.github.davenury.operator

import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.fabric8.kubernetes.client.utils.Serialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import meterRegistry

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {

        install(MicrometerMetrics) {
            registry = meterRegistry
            meterBinders = listOf(
                JvmMemoryMetrics(),
                JvmGcMetrics(),
                ProcessorMetrics()
            )
        }

        Serialization.jsonMapper().registerKotlinModule()

        val operator = ScenarioOperator()
        operator.run()

        routing {
            metaRouting()
        }
    }.start(wait = true)
}