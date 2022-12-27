package com.github.davenury.operator.actions

import com.github.davenury.operator.ActionSpec
import io.fabric8.kubernetes.client.KubernetesClient


class ScaleDeploymentAction(
    private val spec: ActionSpec
    ): Action {

    override val resourceType: String
        get() = "deployment"
    override val verb: String
        get() = "scale"

    private var currentValue: Int = 0

    override fun applyAction(client: KubernetesClient) {
        currentValue = client.apps().deployments().inNamespace(spec.namespace).withName(spec.resourceName).get().spec.replicas
        client.apps().deployments().inNamespace(spec.namespace).withName(spec.resourceName).scale(spec.value!!)
    }

    override fun getName() = "Scale Deployment"

    override fun reverseAction(client: KubernetesClient) {
        client.apps().deployments().inNamespace(spec.namespace).withName(spec.resourceName).scale(currentValue)
    }
}