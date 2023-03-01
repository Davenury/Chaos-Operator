package com.github.davenury.operator

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.github.davenury.operator.actions.Action
import com.github.davenury.operator.actions.Actions
import io.fabric8.kubernetes.api.model.KubernetesResource
import io.fabric8.kubernetes.api.model.Namespaced
import io.fabric8.kubernetes.client.CustomResource
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.model.annotation.Group
import io.fabric8.kubernetes.model.annotation.Version
import io.javaoperatorsdk.operator.api.ObservedGenerationAwareStatus
import org.slf4j.LoggerFactory
import java.time.Duration

@Group("lsc.davenury.github.com")
@Version("v1")
class Scenario: CustomResource<ScenarioSpec, ScenarioStatus>(), Namespaced {
    override fun toString(): String =
        "Scenario{spec=$spec, status=$status}"

    fun applyActions(phase: Int, client: KubernetesClient): List<Action> {
        val actions = this.spec.phases[phase].actions.map { spec ->
            Actions.getAction(spec)
                .also {
                    it?.applyAction(client) ?: kotlin.run {
                        logger.warn("Action ${spec.action} ${spec.resourceType} not found, skipping")
                    }
                }
            }
        return actions.filterNotNull()
    }

    fun getDurationOfPhase(phase: Int) = this.spec.phases[phase].duration

    companion object {
        private val logger = LoggerFactory.getLogger("Scenario")
    }
}

@JsonDeserialize
data class ScenarioSpec(
    val phases: List<Phase>
)

@JsonDeserialize
data class Phase(
    val actions: List<ActionSpec>,
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    val duration: Duration
)

@JsonDeserialize
data class ActionSpec(
    val namespace: String,
    val resourceType: String,
    val action: String,
    val resourceName: String? = null,
    val scaleDeploymentSpec: ScaleDeploymentSpec? = null,
    val scaleDeploymentPercentageSpec: ScaleDeploymentPercentageSpec? = null,
    val networkIsolationSpec: NetworkIsolationSpec? = null,
    val deleteConsensusLeaderSpec: DeleteConsensusLeaderSpec? = null,
)

@JsonDeserialize
data class DeleteConsensusLeaderSpec(
    val peersetId: Int
)

@JsonDeserialize
data class ScaleDeploymentSpec(
    val value: Int
)

@JsonDeserialize
data class ScaleDeploymentPercentageSpec(
    val value: Int,
    val percentage: Int,
    val labels: List<Label> = listOf()
)
@JsonDeserialize
data class Label(
    val key: String,
    val value: String
)

@JsonDeserialize
data class NetworkIsolationSpec(
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
        NEW {
            override fun next(): ScenarioStatus = IN_PROGRESS
        }, IN_PROGRESS {
            override fun next(): ScenarioStatus = COMPLETED
        }, COMPLETED {
            override fun next(): ScenarioStatus = COMPLETED
        }, ERROR {
            override fun next(): ScenarioStatus = ERROR
        };

        abstract fun next(): ScenarioStatus
    }
}

data class ScenarioName(val name: String)