package com.lovelycatv.crystalframework.mail.service

import com.lovelycatv.crystalframework.mail.entity.MailTemplateEntity
import org.springframework.mail.javamail.JavaMailSender

interface MailService {
    fun refreshInstance()

    suspend fun getJavaMailSender(): JavaMailSender

    suspend fun sendMail(to: String, subject: String, content: String)

    suspend fun sendMailByType(to: String, templateTypeName: String, placeholders: Map<String, String?>)

    suspend fun sendMail(to: String, templateName: String, placeholders: Map<String, String?>)

    suspend fun sendMail(to: String, template: MailTemplateEntity, placeholders: Map<String, String?>)
}