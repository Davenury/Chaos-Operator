package com.github.davenury.operator

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Namespaced
import io.fabric8.kubernetes.client.CustomResource
import io.fabric8.kubernetes.model.annotation.Group
import io.fabric8.kubernetes.model.annotation.Version
import io.javaoperatorsdk.operator.api.ObservedGenerationAwareStatus
import javax.swing.Action

@Group("lsc.davenury.github.com")
@Version("v1")
class Scenario: CustomResource<ScenarioSpec, ScenarioStatus>(), Namespaced {
    override fun toString(): String =
        "Scenario{spec=$spec, status=$status}"
}

@JsonDeserialize
data class ScenarioSpec(
    val phases: List<Phase>
): KubernetesResource

@JsonDeserialize
data class Phase(
    val actions: List<ActionSpec>,
    val durationInMillis: Long
)

@JsonDeserialize
data class ActionSpec(
    val namespace: String,
    val resourceType: String,
    val resourceName: String,
    val action: String,
    val value: Int? = null
)

@JsonDeserialize
class ScenarioStatus: ObservedGenerationAwareStatus(), KubernetesResource {

    var errorMessage: String = ""

    override fun toString(): String =
        "ScenarioStatus{errorMessage=$errorMessage}"

}