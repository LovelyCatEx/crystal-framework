package com.lovelycatv.crystalframework

import com.aizuda.snailjob.client.starter.EnableSnailJob
import com.lovelycatv.crystalframework.shared.config.CrystalFrameworkConfiguration
import com.lovelycatv.crystalframework.shared.config.SnowflakeNodeLease
import com.lovelycatv.crystalframework.shared.constants.GlobalConstants
import com.lovelycatv.vertex.log.logger
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.info.GitProperties
import org.springframework.boot.runApplication
import org.springframework.boot.security.oauth2.client.autoconfigure.reactive.ReactiveOAuth2ClientAutoConfiguration
import org.springframework.boot.security.oauth2.client.autoconfigure.reactive.ReactiveOAuth2ClientWebSecurityAutoConfiguration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.scheduling.annotation.EnableAsync
import reactor.core.publisher.Hooks
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@EnableConfigurationProperties
@SpringBootApplication(exclude = [
    ReactiveOAuth2ClientAutoConfiguration::class,
    ReactiveOAuth2ClientWebSecurityAutoConfiguration::class
])
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@EnableAsync
@EnableSnailJob
@Order(Ordered.HIGHEST_PRECEDENCE)
class SpringbootTemplateApplication(
    private val config: CrystalFrameworkConfiguration,
    private val gitProperties: GitProperties,
    private val snowflakeNodeLease: SnowflakeNodeLease
) : CommandLineRunner {
    private val logger = logger()

    private val dateFormatter = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.systemDefault())

    override fun run(vararg args: String) {
        val (dataCenterId, workerId) = if (config.sharding.snowflake.autoAllocate) {
            snowflakeNodeLease.nodeIds()
        } else {
            longArrayOf(
                config.sharding.snowflake.dataCenterId,
                config.sharding.snowflake.workerId
            )
        }

        logger.info("""
            
   ____                _        _ _____                                            _    
  / ___|_ __ _   _ ___| |_ __ _| |  ___| __ __ _ _ __ ___   _____      _____  _ __| | __
 | |   | '__| | | / __| __/ _` | | |_ | '__/ _` | '_ ` _ \ / _ \ \ /\ / / _ \| '__| |/ /
 | |___| |  | |_| \__ \ || (_| | |  _|| | | (_| | | | | | |  __/\ V  V / (_) | |  |   < 
  \____|_|   \__, |___/\__\__,_|_|_|  |_|  \__,_|_| |_| |_|\___| \_/\_/ \___/|_|  |_|\_\
             |___/                                                                      
 :: Crystal Framework ::                                                       (v${GlobalConstants.APP_VERSION})                                                                
    
    DATACENTER: $dataCenterId / ${1 shl config.sharding.snowflake.dataCenterIdLength}
    WORKER: $workerId / ${1 shl config.sharding.snowflake.workerIdLength}
    
    Git Properties:
      - Branch: ${gitProperties.branch}
      - CommitId: ${gitProperties.shortCommitId} (${gitProperties.commitId})
      - Time: ${gitProperties.commitTime?.let { dateFormatter.format(it) }}
      - Message: ${gitProperties.get("commit.message.short")}

        """.trimIndent())
    }

}

fun main(args: Array<String>) {
    Hooks.onOperatorDebug()
    runApplication<SpringbootTemplateApplication>(*args)
}