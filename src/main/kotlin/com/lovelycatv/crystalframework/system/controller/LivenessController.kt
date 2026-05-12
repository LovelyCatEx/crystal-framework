package com.lovelycatv.crystalframework.system.controller

import com.lovelycatv.crystalframework.shared.exception.BusinessException
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.availability.AvailabilityChangeEvent
import org.springframework.boot.availability.LivenessState
import org.springframework.context.ApplicationContext
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class LivenessController(
    private val applicationContext: ApplicationContext
) : InitializingBean {
    override fun afterPropertiesSet() {
        appCtx = this.applicationContext
    }

    @EventListener
    fun onLivenessEvent(event: AvailabilityChangeEvent<LivenessState>) {
        livenessState = event.getState()
    }

    companion object {
        private var livenessState: LivenessState = LivenessState.BROKEN
        private var appCtx: ApplicationContext? = null

        private fun getAppCtx(): ApplicationContext {
            if (appCtx == null) {
                throw BusinessException("ApplicationContext is not initialized")
            } else {
                return appCtx!!
            }
        }

        fun isLiveness(): Boolean {
            return this.livenessState == LivenessState.CORRECT
        }

        fun setCorrect() {
            AvailabilityChangeEvent.publish(
                this.getAppCtx(),
                this,
                LivenessState.CORRECT
            )
        }

        fun setBroken() {
            AvailabilityChangeEvent.publish(
                this.getAppCtx(),
                this,
                LivenessState.BROKEN
            )
        }
    }
}