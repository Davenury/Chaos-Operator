package com.github.davenury.operator.state

import com.github.davenury.operator.ScenarioName
import com.github.davenury.operator.ScenarioStatus

interface ScenarioStateHolder {
    fun getScenarioState(name: ScenarioName): ScenarioState
    fun setError(name: ScenarioName)
}

data class ScenarioState(
    val phase: Int,
    val status: ScenarioStatus.ScenarioStatus
) {

    fun isCompleted() = this.status == ScenarioStatus.ScenarioStatus.COMPLETED

    companion object {
        fun new(): ScenarioState =
            ScenarioState(0, ScenarioStatus.ScenarioStatus.NEW)

    }
}