package com.github.davenury.operator.actions

import com.github.davenury.operator.ActionSpec
import com.github.davenury.operator.NetworkIsolationSpec
import io.fabric8.kubernetes.api.model.LabelSelectorRequirement
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicyBuilder
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicyEgressRuleBuilder
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicyIngressRuleBuilder
import io.fabric8.kubernetes.api.model.networking.v1.NetworkPolicyPeerBuilder
import io.fabric8.kubernetes.client.KubernetesClient
import java.util.UUID

@RegisterAction(
    resourceType = "network",
    verb = "isolate"
)
class NetworkIsolation(
    private val spec: ActionSpec
): Action {

    private val networkPolicyName = "network-policy-${UUID.randomUUID()}"
    override fun applyAction(client: KubernetesClient) {
        val networkPolicy = NetworkPolicyBuilder()
            .withNewMetadata()
            .withName(networkPolicyName)
            .withNamespace(spec.namespace)
            .endMetadata()
            .withNewSpec()
            .withNewPodSelector()
            .addToMatchExpressions(
                spec.networkIsolationSpec!!.toLabelSelectorRequirement()
            )
            .endPodSelector()
            .addToIngress(
                NetworkPolicyIngressRuleBuilder()
                    .addToFrom(
                        NetworkPolicyPeerBuilder()
                            .withNewPodSelector()
                            .addToMatchExpressions(spec.networkIsolationSpec.toLabelSelectorRequirement())
                            .endPodSelector().build()
                    )
                    .addToFrom(
                        NetworkPolicyPeerBuilder()
                            .withNewNamespaceSelector()
                            .addToMatchLabels("kubernetes.io/metadata.name", "kube-system")
                            .endNamespaceSelector()
                            .build()
                    )
                    .build()
            )
            .addToEgress(
                NetworkPolicyEgressRuleBuilder()
                    .addToTo(
                        NetworkPolicyPeerBuilder()
                            .withNewPodSelector()
                            .addToMatchExpressions(spec.networkIsolationSpec.toLabelSelectorRequirement())
                            .endPodSelector().build()
                    )
                    .addToTo(
                        NetworkPolicyPeerBuilder()
                            .withNewNamespaceSelector()
                            .addToMatchLabels("kubernetes.io/metadata.name", "kube-system")
                            .endNamespaceSelector()
                            .build()
                    ).build()
            )
            .endSpec()
            .build()

        client.network().networkPolicies().create(networkPolicy)
    }

    private fun NetworkIsolationSpec.toLabelSelectorRequirement(): LabelSelectorRequirement =
        LabelSelectorRequirement(this.labelKey, this.operator, this.values)

    override fun reverseAction(client: KubernetesClient) {
        client.network().networkPolicies().inNamespace(spec.namespace).withName(networkPolicyName).delete()
    }

    override fun getName(): String = "Network Isolation"
}