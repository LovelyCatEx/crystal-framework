package com.lovelycatv.crystalframework.schedule.registry

import com.lovelycatv.crystalframework.schedule.api.ScheduleTaskDefinition
import com.lovelycatv.crystalframework.schedule.api.ScheduledTask
import org.springframework.stereotype.Component

/**
 * Task registry for managing and retrieving all registered scheduled tasks.
 */
@Component
class TaskRegistry {

    private val tasks = mutableMapOf<String, ScheduleTaskDefinition>()

    /**
     * Register a task. Throws [IllegalStateException] if a task with the same name is already registered.
     */
    fun register(task: ScheduledTask) {
        val definition = ScheduleTaskDefinition.from(task)
        if (tasks.putIfAbsent(definition.name, definition) != null) {
            throw IllegalStateException("TaskRegistry: duplicate task name '${definition.name}' (class: ${task::class.qualifiedName})")
        }
    }

    /**
     * Get task definition by name.
     */
    fun getDefinition(name: String): ScheduleTaskDefinition? {
        return tasks[name]
    }

    /**
     * Get task instance by name.
     */
    fun getTask(name: String): ScheduledTask? {
        return tasks[name]?.task
    }

    /**
     * Get all registered task definitions.
     */
    fun getAllDefinitions(): Map<String, ScheduleTaskDefinition> {
        return tasks.toMap()
    }

    /**
     * Get all registered task instances.
     */
    fun getAllTasks(): Map<String, ScheduledTask> {
        return tasks.mapValues { it.value.task }
    }

    /**
     * Check if a task is registered.
     */
    fun contains(name: String): Boolean {
        return tasks.containsKey(name)
    }
}
