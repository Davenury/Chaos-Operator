package com.github.davenury.operator

import com.github.davenury.operator.auditlog.InMemoryAuditLog
import com.github.davenury.operator.state.InMemoryScenarioStateHolder
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import io.javaoperatorsdk.operator.junit.LocallyRunOperatorExtension
import org.awaitility.kotlin.await
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.Clock
import java.time.Duration

abstract class BaseOperatorTest {

    private val client = KubernetesClientBuilder().build()
    private val stateHolder = InMemoryScenarioStateHolder()
    @RegisterExtension
    protected val operator = LocallyRunOperatorExtension.builder()
        .withReconciler(ScenarioReconciler(client, stateHolder, InMemoryAuditLog(Clock.systemUTC())))
        .build()

    protected fun finally(duration: Duration = Duration.ofSeconds(10), fn: () -> Unit) {
        await.await().atMost(duration).untilAsserted(fn)
    }
}