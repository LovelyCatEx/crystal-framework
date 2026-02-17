package com.lovelycatv.template.springboot.shared.service.mail

import org.springframework.mail.javamail.JavaMailSender

interface MailService {
    fun refreshInstance()

    suspend fun getJavaMailSender(): JavaMailSender

    suspend fun sendMail(to: String, subject: String, content: String)
}