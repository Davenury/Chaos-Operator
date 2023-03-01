package com.github.davenury.operator.actions.ucac

import com.github.davenury.operator.ActionSpec
import com.github.davenury.operator.ScaleDeploymentSpec
import com.github.davenury.operator.actions.Action
import com.github.davenury.operator.actions.RegisterAction
import com.github.davenury.operator.actions.ScaleDeploymentAction
import io.fabric8.kubernetes.client.KubernetesClient
import org.slf4j.LoggerFactory

@RegisterAction(
    resourceType = "consensus follower",
    verb = "delete percentage"
)
class DeleteConsensusFollowerPercentage(
    private val spec: ActionSpec
) : Action {

    private val scaleDeploymentActions: MutableList<ScaleDeploymentAction> = mutableListOf()

    override fun applyAction(client: KubernetesClient) {
        val peersetId = PeersetId(spec.deleteConsensusFollowerPercentageSpec!!.peersetId)
        val leaderId = PeerFinder.create().findConsensusLeader(peersetId, spec.namespace) ?: kotlin.run {
            logger.error("Could not find consensus leader")
            return
        }

        val deployments =
            getDeployments(client, peersetId).filter { it.metadata.labels["peerId"] != leaderId.value.toString() }
        deployments
            .shuffled()
            .take((deployments.size * spec.deleteConsensusFollowerPercentageSpec.percentage) / 100)
            .forEach {
                val action = ScaleDeploymentAction(
                    ActionSpec(
                        namespace = spec.namespace,
                        resourceType = "deployment",
                        resourceName = it.metadata.name,
                        action = "scale",
                        scaleDeploymentSpec = ScaleDeploymentSpec(0)
                    )
                )
                scaleDeploymentActions.add(action)
                action.applyAction(client)
            }
    }

    override fun reverseAction(client: KubernetesClient) {
        scaleDeploymentActions.forEach { it.reverseAction(client) }
    }

    private fun getDeployments(client: KubernetesClient, peersetId: PeersetId) =
        client.apps().deployments().inNamespace(spec.namespace)
            .withLabel("peersetId", peersetId.value.toString())
            .list().items

    override fun getName(): String = "DeleteConsensusFollowerPercentage"

    companion object {
        private val logger = LoggerFactory.getLogger("DeleteConsensusFollowerPercentage")
    }
}