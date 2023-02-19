package com.github.davenury.operator.state

import com.github.davenury.operator.ScenarioName
import com.github.davenury.operator.ScenarioStatus

class InMemoryScenarioStateHolder: ScenarioStateHolder {

    private var scenarioStates: MutableMap<ScenarioName, ScenarioState> = mutableMapOf()

    override fun getScenarioState(name: ScenarioName): ScenarioState {
        val state = if (scenarioStates.containsKey(name)) {
            scenarioStates[name]!!.next()
        } else {
            ScenarioState.new()
        }

        scenarioStates[name] = state
        return state
    }

    override fun setError(name: ScenarioName) {
        val current = scenarioStates[name]

        if (current != null) {
            scenarioStates[name] = current.copy(status = ScenarioStatus.ScenarioStatus.ERROR)
        }
    }

    private fun ScenarioState.next(): ScenarioState {
        if (this.status == ScenarioStatus.ScenarioStatus.ERROR || this.status == ScenarioStatus.ScenarioStatus.COMPLETED) {
            return this
        }
        return ScenarioState(this.phase + 1, this.status.next())
    }

}
