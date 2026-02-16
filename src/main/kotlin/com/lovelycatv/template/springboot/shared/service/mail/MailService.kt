package com.lovelycatv.template.springboot.shared.service.mail

import org.springframework.mail.javamail.JavaMailSender

interface MailService {
    fun getJavaMailSender(): JavaMailSender

    fun sendMail(to: String, subject: String, content: String)
}