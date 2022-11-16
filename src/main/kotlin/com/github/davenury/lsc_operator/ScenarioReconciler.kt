package com.github.davenury.lsc_operator

import com.github.davenury.lsc_operator.actions.Actions
import com.github.davenury.lsc_operator.actions.Scheduler
import io.fabric8.kubernetes.client.KubernetesClient
import io.javaoperatorsdk.operator.api.reconciler.*

@ControllerConfiguration
class ScenarioReconciler(
    private val client: KubernetesClient
): Reconciler<Scenario>, ErrorStatusHandler<Scenario> {

    private var scenarioStates: MutableMap<String, ScenarioState> = mutableMapOf()

    override fun reconcile(scenario: Scenario?, context: Context<Scenario>?): UpdateControl<Scenario> {
        scenario?.let {
            val scenarioName = scenario.crdName

            val counter = if (scenarioStates.containsKey(scenarioName)) {
                scenarioStates[scenarioName]!!.counter
            } else {
                scenarioStates[scenarioName] = ScenarioState(0, false)
                0
            }

            if (!scenarioStates[scenarioName]!!.completed) {
                val spec = scenario.spec.actions[counter]
                val action = Actions(spec).getAction(spec.resourceType, spec.action) ?: kotlin.run {
                    println("Action for ${spec.action} ${spec.resourceType} is not found, skipping")
                    return UpdateControl.updateStatus(scenario)
                }

                action.applyAction(client)
                Scheduler.schedule(spec.durationInMillis) { action.reverseAction(client) }
            }

            if (scenarioStates[scenarioName]!!.counter < scenario.spec.actions.size) {
               scenarioStates[scenarioName] = ScenarioState(counter + 1, false)
            } else {
                scenarioStates[scenarioName] = ScenarioState(counter, true)
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
        return ErrorStatusUpdateControl.updateStatus(scenario)
    }
}

data class ScenarioState(
    var counter: Int,
    var completed: Boolean
)