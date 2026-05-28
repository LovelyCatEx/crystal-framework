package com.lovelycatv.crystalframework.schedule.adapter.spring

import com.lovelycatv.crystalframework.schedule.annotations.CronTaskExecutor
import com.lovelycatv.crystalframework.schedule.api.TaskResult
import com.lovelycatv.crystalframework.schedule.api.context.DefaultTaskExecutionContext
import com.lovelycatv.crystalframework.schedule.event.TaskRegisteredEvent
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.support.CronTrigger
import org.springframework.stereotype.Component
import java.util.*

@Component
class SpringCronTaskAdapter(
    @Qualifier("taskScheduler")
    private val taskScheduler: TaskScheduler,
    private val environment: Environment,
) {

    private val logger = LoggerFactory.getLogger(SpringCronTaskAdapter::class.java)

    @EventListener
    fun onTaskRegistered(event: TaskRegisteredEvent) {
        val definition = event.definition

        val triggerAnnotation = definition.task::class.java.getAnnotation(CronTaskExecutor::class.java)
            ?: return

        if (triggerAnnotation.cron.isBlank()) {
            return
        }

        if (!definition.enabled) {
            logger.info("[SpringCron] Task ${definition.name} is disabled, skipping registration")
            return
        }

        // Resolve placeholders like ${my.cron.expression} from application.yaml
        val resolvedCron = environment.resolvePlaceholders(triggerAnnotation.cron)

        taskScheduler.schedule(
            {
                runBlocking {
                    val executionId = UUID.randomUUID().toString()
                    val context = DefaultTaskExecutionContext(executionId = executionId)

                    if (triggerAnnotation.enableLog) {
                        logger.info("[SpringCron] Executing task: ${definition.name}, executionId: $executionId")
                    }

                    try {
                        when (val result = definition.task.execute(context)) {
                            is TaskResult.Success -> {
                                if (triggerAnnotation.enableLog) {
                                    logger.info("[SpringCron] Task ${definition.name} executed successfully: ${result.message}")
                                }
                            }
                            is TaskResult.Failure -> {
                                logger.error("[SpringCron] Task ${definition.name} failed: ${result.message}", result.exception)
                            }
                        }
                    } catch (e: Exception) {
                        logger.error("[SpringCron] Task ${definition.name} exception: ${e.message}", e)
                    }
                }
            },
            CronTrigger(resolvedCron)
        )

        if (triggerAnnotation.enableLog) {
            logger.info("[SpringCron] Registered task: ${definition.name} with cron: $resolvedCron")
        }
    }
}