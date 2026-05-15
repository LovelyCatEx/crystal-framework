package com.lovelycatv.crystalframework.script.sdk

import com.lovelycatv.vertex.log.logger

abstract class CrystalFrameworkScript {
    abstract val metadata: ScriptMetadata

    private val logger = Logger(metadata.scriptName)

    abstract suspend fun execute()

    class Logger(private val name: String) {
        private val logger = logger()

        fun info(message: String) {
            logger.info("[$name] $message")
        }

        fun warn(message: String) {
            logger.warn("[$name] $message")
        }

        fun error(message: String) {
            logger.error("[$name] $message")
        }
    }
}