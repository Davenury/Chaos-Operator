package com.github.davenury.operator

import io.fabric8.kubernetes.client.KubernetesClientBuilder
import io.javaoperatorsdk.operator.Operator

class ScenarioOperator {

    fun run() {
        try {
            val client = KubernetesClientBuilder().build()
            val operator = Operator(client)
            operator.register(ScenarioReconciler(client))

            operator.installShutdownHook()
            operator.start()
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error: $e")
        }
    }

}