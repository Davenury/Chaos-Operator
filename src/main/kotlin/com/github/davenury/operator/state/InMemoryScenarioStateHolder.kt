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
        setStatus(name, ScenarioStatus.ScenarioStatus.ERROR)
        clearScenario(name)
    }

    override fun setCompleted(name: ScenarioName) {
        setStatus(name, ScenarioStatus.ScenarioStatus.COMPLETED)
        clearScenario(name)
    }

    private fun setStatus(name: ScenarioName, status: ScenarioStatus.ScenarioStatus) {
        val current = scenarioStates[name]

        if (current != null) {
            scenarioStates[name] = current.copy(status = status)
        }
    }

    private fun clearScenario(name: ScenarioName) {
        scenarioStates.remove(name)
    }

    private fun ScenarioState.next(): ScenarioState {
        if (this.status == ScenarioStatus.ScenarioStatus.ERROR || this.status == ScenarioStatus.ScenarioStatus.COMPLETED) {
            return this
        }
        return ScenarioState(this.phase + 1, this.status.next())
    }

}
