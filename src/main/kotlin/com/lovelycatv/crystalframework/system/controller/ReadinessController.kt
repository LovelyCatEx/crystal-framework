package com.lovelycatv.crystalframework.system.controller

import com.lovelycatv.crystalframework.rbac.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.system.types.RedisConstants
import com.lovelycatv.vertex.log.logger
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.availability.AvailabilityChangeEvent
import org.springframework.boot.availability.ReadinessState
import org.springframework.context.ApplicationContext
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("${GlobalConstants.REQUEST_MAPPING_PREFIX}/manager/system")
class ReadinessController(
    private val applicationContext: ApplicationContext,
    private val reactiveRedisTemplate: ReactiveRedisTemplate<String, Any>,
    private val redisMessageListenerContainer: ReactiveRedisMessageListenerContainer,
) : InitializingBean {
    private val logger = logger()

    private val maintenanceTopic = ChannelTopic(RedisConstants.SYSTEM_MAINTENANCE_TOPIC)
    private val maintenanceTopicMessageTrue = "true"
    private val maintenanceTopicMessageFalse = "false"

    override fun afterPropertiesSet() {
        appCtx = this.applicationContext
    }

    @PostConstruct
    fun subscribeMaintenanceTopic() {
        redisMessageListenerContainer
            .receive(maintenanceTopic)
            .subscribe { message ->
                val status = message.message
                if (status == maintenanceTopicMessageTrue) {
                    setRefusing()
                } else if (status == maintenanceTopicMessageFalse) {
                    setAccepting()
                } else {
                    logger.warn(
                        "Message from ${maintenanceTopic.topic} was abort, " +
                                "message: $status, but accept $maintenanceTopicMessageTrue " +
                                "and $maintenanceTopicMessageFalse only."
                    )
                }
            }
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_SYSTEM_MAINTENANCE_UPDATE}')")
    @PostMapping("/maintenance")
    fun setSystemMaintenance(
        @RequestParam("enabled") enabled: Boolean
    ): ApiResponse<*> {
        if (enabled) {
            setRefusing()
            reactiveRedisTemplate
                .convertAndSend(maintenanceTopic.topic, maintenanceTopicMessageTrue)
                .subscribe()
        } else {
            setAccepting()
            reactiveRedisTemplate
                .convertAndSend(maintenanceTopic.topic, maintenanceTopicMessageFalse)
                .subscribe()
        }

        return ApiResponse.success(enabled)
    }


    companion object {
        private var appCtx: ApplicationContext? = null

        private fun getAppCtx(): ApplicationContext {
            if (appCtx == null) {
                throw BusinessException("ApplicationContext is not initialized")
            } else {
                return appCtx!!
            }
        }

        fun setRefusing() {
            AvailabilityChangeEvent.publish(
                this.getAppCtx(),
                this,
                ReadinessState.REFUSING_TRAFFIC
            )
        }

        fun setAccepting() {
            AvailabilityChangeEvent.publish(
                this.getAppCtx(),
                this,
                ReadinessState.ACCEPTING_TRAFFIC
            )
        }
    }
}