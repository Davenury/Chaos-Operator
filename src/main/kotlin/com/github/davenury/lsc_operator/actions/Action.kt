package com.github.davenury.lsc_operator.actions

import com.github.davenury.lsc_operator.ActionSpec
import io.fabric8.kubernetes.client.KubernetesClient

interface Action {
    val resourceType: String
    val verb: String

    fun applyAction(client: KubernetesClient)
    fun reverseAction(client: KubernetesClient)
}

class Actions(
    private val spec: ActionSpec
) {

    private val actions = listOf(ScaleDeploymentAction(spec), DeletePodAction(spec))

    fun getAction(resourceType: String, verb: String): Action? {
        return actions.find {
            it.resourceType == resourceType && it.verb == verb
        }
    }
}