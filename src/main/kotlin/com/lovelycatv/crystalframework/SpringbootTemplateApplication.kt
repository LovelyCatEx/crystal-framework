package com.lovelycatv.crystalframework

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties
@SpringBootApplication
class SpringbootTemplateApplication

fun main(args: Array<String>) {
    runApplication<SpringbootTemplateApplication>(*args)
}