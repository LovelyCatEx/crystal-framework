package com.lovelycatv.crystalframework.system.controller

import com.lovelycatv.crystalframework.shared.annotations.Unauthorized
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.crystalframework.shared.constants.RedisConstants
import com.lovelycatv.crystalframework.shared.constants.SystemPermission
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.response.ApiResponse
import com.lovelycatv.crystalframework.shared.utils.RbacUtils
import com.lovelycatv.crystalframework.system.controller.dto.SwitchSystemMaintenanceModeDTO
import com.lovelycatv.crystalframework.system.controller.vo.MaintenanceInfoVO
import com.lovelycatv.crystalframework.system.filter.SystemMaintenanceGuardFilter
import com.lovelycatv.vertex.log.logger
import jakarta.annotation.PostConstruct
import jakarta.validation.Valid
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.availability.AvailabilityChangeEvent
import org.springframework.boot.availability.ReadinessState
import org.springframework.context.ApplicationContext
import org.springframework.context.event.EventListener
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

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
                val status = message.message.replace("\"", "")
                when (status) {
                    maintenanceTopicMessageTrue -> {
                        setRefusing()
                    }

                    maintenanceTopicMessageFalse -> {
                        setAccepting()
                    }

                    else -> {
                        logger.warn(
                            "Message from ${maintenanceTopic.topic} was abort, " +
                                    "message: $status, but accept $maintenanceTopicMessageTrue " +
                                    "and $maintenanceTopicMessageFalse only."
                        )
                    }
                }
            }
    }

    @Unauthorized
    @GetMapping("/maintenance")
    suspend fun getSystemMaintenance(): ApiResponse<MaintenanceInfoVO> {
        val canAccessInMaintenance = RbacUtils.hasAuthority(SystemMaintenanceGuardFilter.MAINTENANCE_ACCESS_PERMISSION)
        val isInMaintenance = isInMaintenance()

        return ApiResponse.success(
            MaintenanceInfoVO(
                canAccess = !isInMaintenance || canAccessInMaintenance,
                maintenanceMode = isInMaintenance
        )
        )
    }

    @PreAuthorize("hasAnyAuthority('${SystemPermission.ACTION_SYSTEM_MAINTENANCE_UPDATE}')")
    @PostMapping("/maintenance")
    suspend fun setSystemMaintenance(
        @ModelAttribute
        @Valid
        dto: SwitchSystemMaintenanceModeDTO
    ): ApiResponse<*> {
        if (dto.enable) {
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

        return ApiResponse.success(true, "success")
    }

    @EventListener
    fun onReadinessEvent(event: AvailabilityChangeEvent<ReadinessState>) {
        logger.warn("SYSTEM READINESS STATUS CHANGED TO ==> ${event.state.name}")
        readinessState = event.getState()
    }

    companion object {
        private var readinessState: ReadinessState = ReadinessState.REFUSING_TRAFFIC
        private var appCtx: ApplicationContext? = null

        private fun getAppCtx(): ApplicationContext {
            if (appCtx == null) {
                throw BusinessException("ApplicationContext is not initialized")
            } else {
                return appCtx!!
            }
        }

        fun isInMaintenance(): Boolean {
            return this.readinessState == ReadinessState.REFUSING_TRAFFIC
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