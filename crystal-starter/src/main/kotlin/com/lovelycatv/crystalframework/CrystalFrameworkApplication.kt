package com.lovelycatv.crystalframework

import com.aizuda.snailjob.client.starter.EnableSnailJob
import com.lovelycatv.crystalframework.shared.config.CrystalFrameworkConfiguration
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.vertex.log.logger
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.scheduling.annotation.EnableAsync
import reactor.core.publisher.Hooks

@EnableConfigurationProperties
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@EnableAsync
@EnableSnailJob
@Order(Ordered.HIGHEST_PRECEDENCE)
class SpringbootTemplateApplication(
    private val config: CrystalFrameworkConfiguration
) : CommandLineRunner {
    private val logger = logger()

    override fun run(vararg args: String) {
        logger.info("""
            
   ____                _        _ _____                                            _    
  / ___|_ __ _   _ ___| |_ __ _| |  ___| __ __ _ _ __ ___   _____      _____  _ __| | __
 | |   | '__| | | / __| __/ _` | | |_ | '__/ _` | '_ ` _ \ / _ \ \ /\ / / _ \| '__| |/ /
 | |___| |  | |_| \__ \ || (_| | |  _|| | | (_| | | | | | |  __/\ V  V / (_) | |  |   < 
  \____|_|   \__, |___/\__\__,_|_|_|  |_|  \__,_|_| |_| |_|\___| \_/\_/ \___/|_|  |_|\_\
             |___/                                                                      
 :: Crystal Framework ::                                                       (v${GlobalConstants.APP_VERSION})                                                                
    
    DATACENTER: ${config.sharding.snowflake.dataCenterId}
    WORKER: ${config.sharding.snowflake.workerId}
    
        """.trimIndent())
    }

}

fun main(args: Array<String>) {
    Hooks.onOperatorDebug()
    runApplication<SpringbootTemplateApplication>(*args)
}