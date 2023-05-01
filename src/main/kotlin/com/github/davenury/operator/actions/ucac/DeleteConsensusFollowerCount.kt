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
    verb = "delete count"
)
class DeleteConsensusFollowerCount(
    private val spec: ActionSpec
): Action {

    private val scaleDeploymentActions: MutableList<ScaleDeploymentAction> = mutableListOf()

    override fun applyAction(client: KubernetesClient) {
        val peersetInformation =
            PeerFinder.create().findConsensusLeader(spec.deleteConsensusFollowerCountSpec!!.peerUrl)
                ?: kotlin.run {
                    logger.error("Could not find consensus leader")
                    return
                }

        val peersInPeerset = peersetInformation.peersInPeerset

        val deployments = peersInPeerset.mapNotNull { getDeployment(client, it).getOrNull(0) }
            .filter { it.metadata?.labels?.get("peerId") != peersetInformation.currentConsensusLeader?.value }

        deployments
            .shuffled()
            .take(spec.deleteConsensusFollowerCountSpec.count)
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

    private fun getDeployment(client: KubernetesClient, peerId: PeerId) =
        client.apps().deployments().inNamespace(spec.namespace)
            .withLabel("peerId", peerId.value)
            .list().items

    override fun getName(): String = "DeleteConsensusFollowerPercentage"

    companion object {
        private val logger = LoggerFactory.getLogger("DeleteConsensusFollowerPercentage")
    }
}