package com.lovelycatv.crystalframework.schedule.event

import com.lovelycatv.crystalframework.schedule.api.ScheduleTaskDefinition

class TaskRegisteredEvent(
    val definition: ScheduleTaskDefinition
)