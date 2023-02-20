package com.github.davenury.operator.actions

import com.github.davenury.operator.ActionSpec
import io.fabric8.kubernetes.client.KubernetesClient
import org.reflections.Reflections
import org.slf4j.LoggerFactory

interface Action {
    fun applyAction(client: KubernetesClient)
    fun reverseAction(client: KubernetesClient)
    fun getName(): String
}

object Actions {

    private val reflections = Reflections("com.github.davenury.operator")
    private var annotated: Set<Class<*>> = reflections.getTypesAnnotatedWith(RegisterAction::class.java)

    private fun getActionBySpec(spec: ActionSpec): Action? {
        return annotated.find {
            val annotation = it.getAnnotation(RegisterAction::class.java)
            annotation.verb == spec.action && annotation.resourceType == spec.resourceType
        }?.getConstructor(ActionSpec::class.java)?.let {
            it.newInstance(spec) as Action
        }
    }

    fun getAction(spec: ActionSpec): Action? {
        return getActionBySpec(spec)
    }
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RegisterAction(
    val resourceType: String,
    val verb: String
)