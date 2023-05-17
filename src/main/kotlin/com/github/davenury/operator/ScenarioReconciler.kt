package com.github.davenury.operator

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.github.davenury.operator.actions.Action
import com.github.davenury.operator.auditlog.AuditLog
import com.github.davenury.operator.state.ScenarioStateHolder
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.utils.Serialization
import io.javaoperatorsdk.operator.api.reconciler.*
import org.slf4j.LoggerFactory

@ControllerConfiguration
class ScenarioReconciler(
    private val client: KubernetesClient,
    private val stateHolder: ScenarioStateHolder,
    private val auditLog: AuditLog,
) : Reconciler<Scenario>, ErrorStatusHandler<Scenario> {

    init {
        Serialization.jsonMapper().registerKotlinModule()
            .registerModules(JavaTimeModule(), Jdk8Module(), ParameterNamesModule())

    }

    private var actions: MutableMap<ScenarioName, List<Action>> = mutableMapOf()

    override fun reconcile(scenario: Scenario?, context: Context<Scenario>?): UpdateControl<Scenario> {
        scenario?.let { scenario ->
            val scenarioName = ScenarioName(scenario.metadata.name)
            try {
                val scenarioState = stateHolder.getScenarioState(scenarioName)
                scenario.status = ScenarioStatus().apply { this.status = scenarioState.status }

                applyReverseActions(scenarioName)

                if (scenarioState.phase == scenario.spec.phases.size) {
                    stateHolder.setCompleted(scenarioName)
                    return UpdateControl.updateStatus(scenario.apply { scenario.status.status = ScenarioStatus.ScenarioStatus.COMPLETED })
                }

                if (!scenarioState.isCompleted()) {
                    actions[scenarioName] = scenario.applyActions(scenarioState.phase, client).onEach { auditLog.logAction(it, scenarioName.name) }

                    val scheduleIn = scenario.getDurationOfPhase(scenarioState.phase)

                    logger.info("Scheduling action in $scheduleIn")
                    return UpdateControl.updateStatus(scenario).rescheduleAfter(scheduleIn)
                }

                Metrics.bumpPhaseChange(scenarioName)
                return UpdateControl.updateStatus(scenario.apply { scenario.status.status = scenarioState.status })
            } catch (e: Exception) {
                return onError(e, scenarioName, scenario)
            }
        }
        return UpdateControl.updateStatus(scenario)
    }

    private fun onError(e: Exception, scenarioName: ScenarioName, scenario: Scenario): UpdateControl<Scenario> {
        logger.error("Error while executing scenario phase", e)
        stateHolder.setError(scenarioName)
        Metrics.bumpProcessingError(scenarioName)
        return UpdateControl.updateStatus(scenario.apply {
            scenario.status?.status = ScenarioStatus.ScenarioStatus.ERROR
            scenario.status?.errorMessage = "Error in operator. Contact devs. Error message: ${e.message}"
        })
    }

    override fun updateErrorStatus(
        scenario: Scenario?,
        context: Context<Scenario>?,
        e: Exception?
    ): ErrorStatusUpdateControl<Scenario> {
        scenario?.status?.errorMessage = "Error: ${e?.message}"
        scenario?.status?.status = ScenarioStatus.ScenarioStatus.ERROR
        return ErrorStatusUpdateControl.updateStatus(scenario)
    }

    private fun applyReverseActions(scenarioName: ScenarioName) {
        actions[scenarioName]?.forEach {
            logger.info("Executing reverse action for ${it.getName()}")
            it.reverseAction(client)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger("ScenarioReconciler")
    }
}
