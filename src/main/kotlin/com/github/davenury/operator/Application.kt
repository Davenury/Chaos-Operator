package com.github.davenury.operator

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {

        install(MicrometerMetrics) {
            registry = Metrics.meterRegistry
            meterBinders = listOf(
                JvmMemoryMetrics(listOf(Tag.of("application", "chaos_operator"))),
                JvmGcMetrics(listOf(Tag.of("application", "chaos_operator"))),
                ProcessorMetrics(listOf(Tag.of("application", "chaos_operator")))
            )
            timers { _, _ ->
                tag("application", "chaos_operator")
            }
        }

        val operator = ScenarioOperator()
        operator.run()

        routing {
            metaRouting()
        }
    }.start(wait = true)
}