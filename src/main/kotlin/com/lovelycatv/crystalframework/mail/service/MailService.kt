package com.lovelycatv.crystalframework.mail.service

import org.springframework.mail.javamail.JavaMailSender

interface MailService {
    fun refreshInstance()

    suspend fun getJavaMailSender(): JavaMailSender

    suspend fun sendMail(to: String, subject: String, content: String)
}