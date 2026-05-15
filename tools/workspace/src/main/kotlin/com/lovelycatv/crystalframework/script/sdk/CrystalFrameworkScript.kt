package com.lovelycatv.crystalframework.script.sdk

abstract class CrystalFrameworkScript {
    abstract val metadata: ScriptMetadata

    private val logger = Logger(metadata.scriptName)

    abstract suspend fun execute()

    class Logger(private val name: String) {
        fun info(message: String) {
            println("[$name] $message")
        }

        fun warn(message: String) {
            println("[$name] $message")
        }

        fun error(message: String) {
            println("[$name] $message")
        }
    }
}