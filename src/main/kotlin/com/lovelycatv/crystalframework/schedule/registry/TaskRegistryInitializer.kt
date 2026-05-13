package com.lovelycatv.crystalframework.schedule.registry

import com.lovelycatv.crystalframework.schedule.api.ScheduleTaskDefinition
import com.lovelycatv.crystalframework.schedule.api.ScheduledTask
import com.lovelycatv.crystalframework.schedule.event.TaskRegisteredEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class TaskRegistryInitializer(
    private val taskRegistry: TaskRegistry,
    private val applicationContext: ApplicationContext,
    private val eventPublisher: ApplicationEventPublisher
) : ApplicationListener<ContextRefreshedEvent> {

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val taskBeans = applicationContext.getBeansOfType(ScheduledTask::class.java)
        taskBeans.values.forEach { task ->
            taskRegistry.register(task)
            val definition = ScheduleTaskDefinition.from(task)
            eventPublisher.publishEvent(TaskRegisteredEvent(definition))
        }
    }
}