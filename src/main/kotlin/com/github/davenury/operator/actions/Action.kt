package com.github.davenury.operator.actions

import com.github.davenury.operator.ActionSpec
import io.fabric8.kubernetes.client.KubernetesClient

interface Action {
    val resourceType: String
    val verb: String
    fun applyAction(client: KubernetesClient)
    fun reverseAction(client: KubernetesClient)
    fun getName(): String
}

class Actions(
    spec: ActionSpec
) {

    private val actions: List<Action> =
        listOf(
            ScaleDeploymentAction(spec),
            DeletePodAction(spec),
            DeleteServiceAction(spec),
            NetworkIsolation(spec),
            ScalePercentageOfDeploymentsAction(spec)
        )

    fun getAction(resourceType: String, verb: String): Action? {
        return actions.find {
            it.resourceType == resourceType && it.verb == verb
        }
    }
}