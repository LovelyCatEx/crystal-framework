package com.lovelycatv.crystalframework.schedule.adapter.spring

import com.lovelycatv.crystalframework.schedule.annotations.CronTaskExecutor
import com.lovelycatv.crystalframework.schedule.api.ScheduleTaskDefinition
import com.lovelycatv.crystalframework.schedule.api.ScheduledTask
import com.lovelycatv.crystalframework.schedule.api.TaskResult
import com.lovelycatv.crystalframework.schedule.api.context.DefaultTaskExecutionContext
import com.lovelycatv.crystalframework.schedule.registry.TaskRegistry
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import org.springframework.scheduling.support.CronTrigger
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Spring Cron task adapter.
 * Automatically scans all ScheduledTask annotated with @TaskTrigger and registers them as Spring cron jobs.
 */
@Component
class SpringCronTaskAdapter(
    private val taskRegistry: TaskRegistry,
    private val applicationContext: ApplicationContext
) : SchedulingConfigurer, ApplicationListener<ContextRefreshedEvent> {

    private val logger = LoggerFactory.getLogger(SpringCronTaskAdapter::class.java)
    private val registeredTasks = ConcurrentHashMap<String, ScheduleTaskDefinition>()

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val taskBeans = applicationContext.getBeansOfType(ScheduledTask::class.java)
        taskBeans.values.forEach { task ->
            val triggerAnnotation = task::class.java.getAnnotation(CronTaskExecutor::class.java)
            val definition = ScheduleTaskDefinition.from(task)

            // Must have @TaskTrigger and a non-blank cron expression
            if (triggerAnnotation != null && triggerAnnotation.cron.isNotBlank()) {
                // Check if task is enabled
                if (!definition.enabled) {
                    logger.info("[SpringCron] Task ${definition.name} is disabled, skipping registration")
                    return@forEach
                }

                registeredTasks[definition.name] = definition
                logger.info("[SpringCron] Registered task: ${definition.name} with cron: ${triggerAnnotation.cron}")
            }
        }
    }

    override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
        registeredTasks.forEach { (name, definition) ->
            val triggerAnnotation = definition.task::class.java.getAnnotation(CronTaskExecutor::class.java)
            if (triggerAnnotation != null && triggerAnnotation.cron.isNotBlank()) {
                taskRegistrar.addTriggerTask(
                    {
                        runBlocking {
                            val executionId = UUID.randomUUID().toString()
                            val context = DefaultTaskExecutionContext(executionId = executionId)

                            logger.info("[SpringCron] Executing task: $name, executionId: $executionId")

                            try {
                                val result = definition.task.execute(context)
                                when (result) {
                                    is TaskResult.Success -> {
                                        logger.info("[SpringCron] Task $name executed successfully: ${result.message}")
                                    }
                                    is TaskResult.Failure -> {
                                        logger.error("[SpringCron] Task $name failed: ${result.message}", result.exception)
                                    }
                                }
                            } catch (e: Exception) {
                                logger.error("[SpringCron] Task $name exception: ${e.message}", e)
                            }
                        }
                    },
                    CronTrigger(triggerAnnotation.cron)
                )
            }
        }
    }
}
