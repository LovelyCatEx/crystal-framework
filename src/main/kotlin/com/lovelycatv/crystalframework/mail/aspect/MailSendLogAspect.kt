package com.lovelycatv.crystalframework.mail.aspect

import com.lovelycatv.crystalframework.mail.service.MailSendLogService
import com.lovelycatv.crystalframework.shared.context.CurrentTenantId
import com.lovelycatv.crystalframework.shared.context.CurrentUserId
import com.lovelycatv.crystalframework.system.service.SystemSettingsService
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import kotlin.coroutines.Continuation

@Aspect
@Component
@Order(100)
class MailSendLogAspect(
    private val mailSendLogService: MailSendLogService,
    private val systemSettingsService: SystemSettingsService
) {
    private val logger = logger()
    private val mailLogScope = CoroutineScope(Dispatchers.IO)

    @Around("execution(* com.lovelycatv.crystalframework.mail.service.MailService.sendMail(..))")
    fun recordMailSendLog(joinPoint: ProceedingJoinPoint): Any? {
        val args = joinPoint.args

        if (args.any { it !is String && it !is Continuation<*> }) {
            return joinPoint.proceed()
        }

        val toEmail = args[0] as String
        val subject = args[1] as String
        val content = args[2] as String

        var success = true
        var errorMessage: String? = null
        var result: Any? = null

        try {
            result = joinPoint.proceed()
        } catch (e: Exception) {
            success = false
            errorMessage = e.message
            throw e
        } finally {
            mailLogScope.launch {
                try {
                    val userId = CurrentUserId.current()
                    val tenantId = CurrentTenantId.current()
                    val fromEmail = systemSettingsService.getSystemSettings().mail.smtp.fromEmail

                    mailSendLogService.record(
                        fromEmail = fromEmail,
                        toEmail = toEmail,
                        subject = subject,
                        content = content,
                        success = success,
                        errorMessage = errorMessage,
                        userId = userId,
                        tenantId = tenantId
                    )
                } catch (e: Exception) {
                    logger.error("Failed to record mail send log: ${e.message}", e)
                }
            }
        }

        return result
    }
}
