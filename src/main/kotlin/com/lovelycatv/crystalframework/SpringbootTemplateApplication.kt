package com.lovelycatv.crystalframework

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.scheduling.annotation.EnableAsync

@EnableConfigurationProperties
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableAsync
class SpringbootTemplateApplication

fun main(args: Array<String>) {
    runApplication<SpringbootTemplateApplication>(*args)
}