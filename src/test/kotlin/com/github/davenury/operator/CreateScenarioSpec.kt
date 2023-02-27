package com.github.davenury.operator

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder
import org.junit.jupiter.api.Test
import java.time.Duration

class CreateScenarioSpec : BaseOperatorTest() {

    @Test
    fun `should be able to run`() {
        val scenario = Scenario()

        scenario.apply {
            this.metadata = ObjectMetaBuilder()
                .withName("sample-scenario")
                .withNamespace("default")
                .build()
            this.spec = ScenarioSpec(
                phases = listOf(
                    Phase(
                        duration = Duration.ZERO, actions = listOf(
                            ActionSpec(
                                namespace = "default",
                                resourceType = "deployment",
                                "",
                                "scale",
                                null,
                                null,
                                null
                            )
                        )
                    )
                )
            )
        }

        operator.kubernetesClient.resource(scenario).createOrReplace()

        finally {
            val deployedScenario =
                operator.kubernetesClient.resources(Scenario::class.java).inNamespace("default")
                    .withName("sample-scenario").get()
            assert(deployedScenario.spec.phases.size == 1)
        }
    }

}