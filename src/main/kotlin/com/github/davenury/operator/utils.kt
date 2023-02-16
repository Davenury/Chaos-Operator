import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)