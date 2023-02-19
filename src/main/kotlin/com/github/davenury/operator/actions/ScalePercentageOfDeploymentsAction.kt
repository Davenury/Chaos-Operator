package com.github.davenury.operator.actions

import com.github.davenury.operator.ActionSpec
import io.fabric8.kubernetes.client.KubernetesClient

class ScalePercentageOfDeploymentsAction(
    private val spec: ActionSpec
) : Action {
    override val resourceType: String
        get() = "deployment"
    override val verb: String
        get() = "scale percentage"

    private var deploymentsParameters: List<DeploymentParameters> = mutableListOf()

    override fun applyAction(client: KubernetesClient) {
        // TODO - better assertions that spec is valid
        val deploymentsToScale = client.apps().deployments().inNamespace(spec.namespace).withLabels(
            spec.scaleDeploymentPercentageSpec!!.labels
        ).list().items
        val numberToScale = deploymentsToScale.size * spec.scaleDeploymentPercentageSpec.percentage / 100
        deploymentsParameters = deploymentsToScale.shuffled().take(numberToScale).map {
            DeploymentParameters(it.metadata.name, it.spec.replicas)
        }
        deploymentsParameters.forEach {
            client.apps().deployments().inNamespace(spec.namespace).withName(it.resourceName)
                .scale(spec.scaleDeploymentPercentageSpec.value)
        }
    }

    override fun reverseAction(client: KubernetesClient) {
        deploymentsParameters.forEach {
            client.apps().deployments().inNamespace(spec.namespace).withName(it.resourceName).scale(it.originalValue)
        }
    }

    override fun getName(): String = "Scale Percentage of Deployments"

    private data class DeploymentParameters(
        val resourceName: String,
        val originalValue: Int
    )
}