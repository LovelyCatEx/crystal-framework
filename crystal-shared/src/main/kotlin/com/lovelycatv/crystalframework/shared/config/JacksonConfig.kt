package com.lovelycatv.crystalframework.shared.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.ToStringSerializer
import tools.jackson.module.kotlin.kotlinModule

@Configuration
class JacksonConfig {
    @Bean
    fun jacksonJsonMapper(): JsonMapper {
        val module = SimpleModule().apply {
            addSerializer(Long::class.java, ToStringSerializer.instance)
            addSerializer(Long::class.javaPrimitiveType, ToStringSerializer.instance)
        }

        return JsonMapper
            .builder()
            .addModule(kotlinModule())
            .addModule(module)
            .build()
    }

    @Bean
    fun jacksonObjectMapper(): ObjectMapper {
        val objectMapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()

        objectMapper.registerModule(com.fasterxml.jackson.databind.module.SimpleModule().apply {
            addSerializer(Long::class.java, com.fasterxml.jackson.databind.ser.std.ToStringSerializer.instance)
            addSerializer(Long::class.javaPrimitiveType, com.fasterxml.jackson.databind.ser.std.ToStringSerializer.instance)
        })

        return objectMapper
    }
}