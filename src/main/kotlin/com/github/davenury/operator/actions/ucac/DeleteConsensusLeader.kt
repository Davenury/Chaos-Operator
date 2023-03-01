package com.github.davenury.operator.actions.ucac

import com.github.davenury.operator.ActionSpec
import com.github.davenury.operator.ScaleDeploymentSpec
import com.github.davenury.operator.actions.Action
import com.github.davenury.operator.actions.RegisterAction
import com.github.davenury.operator.actions.ScaleDeploymentAction
import io.fabric8.kubernetes.client.KubernetesClient
import org.slf4j.LoggerFactory

@RegisterAction(
    resourceType = "consensus leader",
    verb = "delete"
)
class DeleteConsensusLeader(
    private val spec: ActionSpec
): Action {

    private var scaleDeploymentAction: ScaleDeploymentAction? = null

    override fun applyAction(client: KubernetesClient) {
        val peersetId = PeersetId(spec.deleteConsensusLeaderSpec!!.peersetId)
        val peerFinder = PeerFinder.create()
        val leaderId = peerFinder.findConsensusLeader(peersetId, spec.namespace) ?: kotlin.run {
            logger.error("Could not find consensus leader")
            return
        }
        val deployments = getDeployment(client, leaderId, peersetId, spec.namespace)
        if (deployments.isEmpty()) {
            logger.error("There's no deployment with consensus leader - peersetId: ${peersetId.value}, leader peerId: ${leaderId.value}")
            return
        }
        scaleDeploymentAction = ScaleDeploymentAction(
            ActionSpec(
                namespace = spec.namespace,
                resourceType = "deployment",
                resourceName = deployments[0].metadata.name,
                action = "scale",
                scaleDeploymentSpec = ScaleDeploymentSpec(0)
            )
        ).also {
            it.applyAction(client)
        }
    }

    override fun reverseAction(client: KubernetesClient) {
        scaleDeploymentAction?.reverseAction(client)
    }

    override fun getName(): String = "Delete consensus leader"

    private fun getDeployment(client: KubernetesClient, peerId: PeerId, peersetId: PeersetId, namespace: String) =
        client.apps().deployments().inNamespace(spec.namespace)
            .withLabel("peerId", peerId.value.toString())
            .withLabel("peersetId", peersetId.value.toString())
            .list().items

    companion object {
        private val logger = LoggerFactory.getLogger("DeleteConsensusLeader")
    }
}