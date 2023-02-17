package com.github.davenury.operator

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

object Metrics {
    val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    fun bumpPhaseChange(scenarioName: String) {
        Counter.builder("chaos_operator_phase_change")
            .tag("scenario", scenarioName)
            .register(meterRegistry)
            .increment()
    }

    fun bumpProcessingError(scenarioName: String) {
        Counter.builder("chaos_operator_execution_error")
            .tag("scenario", scenarioName)
            .register(meterRegistry)
            .increment()
    }
}