package com.github.davenury.operator

import com.github.davenury.operator.actions.Action
import com.github.davenury.operator.actions.Actions
import io.fabric8.kubernetes.client.KubernetesClient
import io.javaoperatorsdk.operator.api.reconciler.*
import org.slf4j.LoggerFactory

@ControllerConfiguration
class ScenarioReconciler(
    private val client: KubernetesClient
) : Reconciler<Scenario>, ErrorStatusHandler<Scenario> {

    private var scenarioStates: MutableMap<String, ScenarioState> = mutableMapOf()
    private var actions: MutableList<Action> = mutableListOf()

    override fun reconcile(scenario: Scenario?, context: Context<Scenario>?): UpdateControl<Scenario> {
        scenario?.let {
            it.status = ScenarioStatus()
            logger.info("$scenario")
            try {
                val scenarioName = scenario.crdName

                val counter = if (scenarioStates.containsKey(scenarioName)) {
                    scenario.status?.status = ScenarioStatus.ScenarioStatus.IN_PROGRESS
                    scenarioStates[scenarioName]!!.counter + 1
                } else {
                    scenarioStates[scenarioName] = ScenarioState(0, false)
                    scenario.status?.status = ScenarioStatus.ScenarioStatus.NEW
                    0
                }
                logger.info("Beginning phase $counter")

                if (scenarioStates[scenarioName]!!.counter < scenario.spec.phases.size - 1) {
                    scenarioStates[scenarioName] = ScenarioState(counter, false)
                } else {
                    logger.info("Scenario $scenarioName is completed")
                    scenario.status?.status = ScenarioStatus.ScenarioStatus.COMPLETED
                    scenarioStates[scenarioName] = ScenarioState(counter, true)
                }

                if (!scenarioStates[scenarioName]!!.completed) {
                    scenario.spec.phases[counter].actions.forEach { spec ->
                        val action = Actions(spec).getAction(spec.resourceType, spec.action) ?: kotlin.run {
                            val message = "Action for ${spec.action} ${spec.resourceType} is not found, skipping"
                            logger.error(message)
                            return UpdateControl.updateStatus(scenario.apply {
                                it.status?.status = ScenarioStatus.ScenarioStatus.ERROR
                                it.status?.errorMessage = message
                            })
                        }

                        logger.info("Applying action ${action.getName()}")
                        action.applyAction(client)
                        actions.add(action)
                    }

                    val scheduleIn = scenario.spec.phases[counter].duration
                    logger.info("Scheduling action in $scheduleIn")
                    return UpdateControl.updateStatus(scenario).rescheduleAfter(scheduleIn)
                }
                return UpdateControl.updateStatus(scenario.apply { it.status.status = ScenarioStatus.ScenarioStatus.COMPLETED })
            } catch (e: Exception) {
                return UpdateControl.updateStatus(scenario.apply {
                    it.status?.status = ScenarioStatus.ScenarioStatus.ERROR
                    it.status?.errorMessage = "Error in operator. Contact devs. Error message: ${e.message}"
                })
            }
        }
        return UpdateControl.updateStatus(scenario)
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

    private fun applyReverseActions() {
        actions.forEach {
            logger.info("Executing reverse action for ${it.getName()}")
            it.reverseAction(client)
        }

        actions.clear()
    }

    private fun applyActions() {

    }

    companion object {
        private val logger = LoggerFactory.getLogger("ScenarioReconciler")
    }
}

data class ScenarioState(
    var counter: Int,
    var completed: Boolean
)