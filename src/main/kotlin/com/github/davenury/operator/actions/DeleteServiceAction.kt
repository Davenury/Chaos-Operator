package com.github.davenury.operator.actions

import com.github.davenury.operator.ActionSpec
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.client.KubernetesClient

@RegisterAction(
    resourceType = "service",
    verb = "delete"
)
class DeleteServiceAction(
    private val spec: ActionSpec
): Action {

    private lateinit var serviceSpec: Service

    override fun applyAction(client: KubernetesClient) {
        client.services().inNamespace(spec.namespace).withName(spec.resourceName).apply {
            serviceSpec = this.get()
            this.delete()
        }
    }

    override fun getName() = "Delete service"

    override fun reverseAction(client: KubernetesClient) {
        client.services().inNamespace(spec.namespace).createOrReplace(serviceSpec)
    }
}