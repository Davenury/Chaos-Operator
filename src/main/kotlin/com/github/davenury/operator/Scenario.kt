package com.github.davenury.operator

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
    val phases: List<Phase>
): KubernetesResource

@JsonDeserialize
data class Phase(
    val actions: List<ActionSpec>,
    val duration: Duration
)

@JsonDeserialize
data class ActionSpec(
    val namespace: String,
    val resourceType: String,
    val resourceName: String? = null,
    val action: String,
    val scaleDeploymentSpec: ScaleDeploymentSpec? = null,
    val networkIsolationSpec: NetworkIsolationSpec? = null,
)

data class ScaleDeploymentSpec(
    val value: Int
)

data class NetworkIsolationSpec(
    val selectorSpec: SelectorSpec
)

data class SelectorSpec(
    val labelKey: String,
    val operator: String,
    val values: List<String>
)

@JsonDeserialize
class ScenarioStatus: ObservedGenerationAwareStatus(), KubernetesResource {

    var errorMessage: String = "---------"
    var status: ScenarioStatus = ScenarioStatus.NEW

    override fun toString(): String =
        "ScenarioStatus{errorMessage=$errorMessage,status=${status.name}}"

    enum class ScenarioStatus {
        NEW, IN_PROGRESS, COMPLETED, ERROR
    }
}