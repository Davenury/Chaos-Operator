package com.github.davenury.operator

import com.github.davenury.operator.auditlog.AuditLog
import com.github.davenury.operator.state.InMemoryScenarioStateHolder
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import io.javaoperatorsdk.operator.Operator

class ScenarioOperator(
    private val auditLog: AuditLog,
) {

    fun run() {
        try {
            val client = KubernetesClientBuilder().build()
            val operator = Operator(client)
            operator.register(ScenarioReconciler(client, InMemoryScenarioStateHolder(), auditLog))

            operator.installShutdownHook()
            operator.start()
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error: $e")
        }
    }

}
