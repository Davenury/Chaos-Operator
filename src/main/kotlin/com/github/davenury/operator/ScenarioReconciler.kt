package com.github.davenury.operator

import com.github.davenury.operator.actions.Action
import com.github.davenury.operator.actions.Actions
import io.fabric8.kubernetes.client.KubernetesClient
import io.javaoperatorsdk.operator.api.reconciler.*
import org.slf4j.LoggerFactory

@ControllerConfiguration
class ScenarioReconciler(
    private val client: KubernetesClient
): Reconciler<Scenario>, ErrorStatusHandler<Scenario> {

    private var scenarioStates: MutableMap<String, ScenarioState> = mutableMapOf()
    private var actions: MutableList<Action> = mutableListOf()

    override fun reconcile(scenario: Scenario?, context: Context<Scenario>?): UpdateControl<Scenario> {
        scenario?.let {
            val scenarioName = scenario.crdName

            val counter = if (scenarioStates.containsKey(scenarioName)) {
                scenarioStates[scenarioName]!!.counter + 1
            } else {
                scenarioStates[scenarioName] = ScenarioState(0, false)
                0
            }
            logger.info("Beginning phase $counter")

            actions.forEach {
                logger.info("Executing reverse action for ${it.getName()}")
                it.reverseAction(client)
            }
            actions.clear()

            if (scenarioStates[scenarioName]!!.counter < scenario.spec.phases.size - 1) {
                scenarioStates[scenarioName] = ScenarioState(counter, false)
            } else {
                logger.info("Scenario $scenarioName is completed")
                scenarioStates[scenarioName] = ScenarioState(counter, true)
            }

            if (!scenarioStates[scenarioName]!!.completed) {
                scenario.spec.phases[counter].actions.forEach { spec ->
                    val action = Actions(spec).getAction(spec.resourceType, spec.action) ?: kotlin.run {
                        logger.error("Action for ${spec.action} ${spec.resourceType} is not found, skipping")
                        return UpdateControl.updateStatus(scenario)
                    }

                    logger.info("Applying action ${action.getName()}")
                    action.applyAction(client)
                    actions.add(action)
                }

                val scheduleIn = scenario.spec.phases[counter].duration
                logger.info("Scheduling action in $scheduleIn")
                return UpdateControl.updateStatus(scenario).rescheduleAfter(scheduleIn)
            }
            return UpdateControl.noUpdate()
        }
        return UpdateControl.updateStatus(scenario)
    }

    override fun updateErrorStatus(
        scenario: Scenario?,
        context: Context<Scenario>?,
        e: Exception?
    ): ErrorStatusUpdateControl<Scenario> {
        scenario?.status?.errorMessage = "Error: ${e?.message}"
        return ErrorStatusUpdateControl.updateStatus(scenario)
    }

    companion object {
        private val logger = LoggerFactory.getLogger("ScenarioReconciler")
    }
}

data class ScenarioState(
    var counter: Int,
    var completed: Boolean
)