package com.github.davenury.lsc_operator

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Namespaced
import io.fabric8.kubernetes.client.CustomResource
import io.fabric8.kubernetes.model.annotation.Group
import io.fabric8.kubernetes.model.annotation.Version
import io.javaoperatorsdk.operator.api.ObservedGenerationAwareStatus
import java.time.Duration

@Group("lsc.davenury.github.com")
@Version("v1")
class Scenario: CustomResource<ScenarioSpec, ScenarioStatus>(), Namespaced {
    override fun toString(): String =
        "Scenario{spec=$spec, status=$status}"
}

@JsonDeserialize
data class ScenarioSpec(
    val actions: List<ActionSpec>
): KubernetesResource

@JsonDeserialize
data class ActionSpec(
    val durationInMillis: Long,
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