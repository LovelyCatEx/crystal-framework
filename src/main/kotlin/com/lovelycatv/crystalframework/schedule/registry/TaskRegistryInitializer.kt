package com.lovelycatv.crystalframework.schedule.registry

import com.lovelycatv.crystalframework.schedule.api.ScheduledTask
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component

/**
 * Task registry initializer.
 * Automatically scans and registers all ScheduledTask implementations after Spring context refresh.
 */
@Component
class TaskRegistryInitializer(
    private val taskRegistry: TaskRegistry,
    private val applicationContext: ApplicationContext
) : ApplicationListener<ContextRefreshedEvent> {

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val taskBeans = applicationContext.getBeansOfType(ScheduledTask::class.java)
        taskBeans.values.forEach { task ->
            taskRegistry.register(task)
        }
    }
}
