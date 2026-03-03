package com.lovelycatv.crystalframework

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy

@EnableConfigurationProperties
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
class SpringbootTemplateApplication

fun main(args: Array<String>) {
    runApplication<SpringbootTemplateApplication>(*args)
}