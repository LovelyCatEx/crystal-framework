package com.lovelycatv.template.springboot.shared.service.mail

import com.lovelycatv.vertex.log.logger
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class MailServiceImpl : MailService {
    private val logger = logger()

    override fun getJavaMailSender(): JavaMailSender {
        TODO("Not yet implemented")
    }

    override fun sendMail(to: String, subject: String, content: String) {
        TODO("Not yet implemented")
    }
}