package com.github.davenury.lsc_operator.actions

import com.github.davenury.lsc_operator.ActionSpec
import io.fabric8.kubernetes.client.KubernetesClient

class DeletePodAction(
    private val spec: ActionSpec
): Action {

    override val resourceType: String
        get() = "pod"
    override val verb: String
        get() = "delete"

    override fun applyAction(client: KubernetesClient) {
        TODO("Not yet implemented")
    }

    override fun reverseAction(client: KubernetesClient) {
        TODO("Not yet implemented")
    }
}