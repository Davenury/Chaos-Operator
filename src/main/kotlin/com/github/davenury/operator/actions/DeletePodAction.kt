package com.github.davenury.operator.actions

import com.github.davenury.operator.ActionSpec
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.client.KubernetesClient

class DeletePodAction(
    private val spec: ActionSpec
): Action {

    override val resourceType: String
        get() = "pod"
    override val verb: String
        get() = "delete"

    private lateinit var podSpec: Pod

    override fun applyAction(client: KubernetesClient) {
        client.pods().inNamespace(spec.namespace).withName(spec.resourceName).apply {
            podSpec = this.get()
            this.delete()
        }
    }

    override fun getName() = "Delete pod"

    override fun reverseAction(client: KubernetesClient) {
        client.pods().inNamespace(spec.namespace).createOrReplace(podSpec)
    }
}