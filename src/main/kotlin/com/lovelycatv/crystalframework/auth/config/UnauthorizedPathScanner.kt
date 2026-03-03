package com.lovelycatv.crystalframework.auth.config

import com.lovelycatv.crystalframework.shared.annotations.Unauthorized
import org.springframework.aop.framework.AopProxyUtils
import org.springframework.beans.factory.getBeansWithAnnotation
import org.springframework.context.ApplicationContext
import org.springframework.http.server.PathContainer
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.pattern.PathPattern
import org.springframework.web.util.pattern.PathPatternParser

@Component
class UnauthorizedPathScanner(
    private val applicationContext: ApplicationContext
) {
    private val pathPatternParser = PathPatternParser()

    fun getUnauthorizedEndpointsSimple(): List<String> {
        val endpoints = mutableListOf<String>()
        val controllerBeans = applicationContext.getBeansWithAnnotation<RestController>()

        controllerBeans.values.forEach { bean ->
            // Fix: annotations will be lost in proxy class object,
            // use ultimateTargetClass to find the original object
            val beanType = AopProxyUtils.ultimateTargetClass(bean)

            val classUnauthorized = beanType.getAnnotation(Unauthorized::class.java)

            val classRequestMapping = beanType.getAnnotation(RequestMapping::class.java)
            val classPathPrefixes = classRequestMapping?.value?.toList() ?: listOf("")

            beanType.declaredMethods.forEach { method ->
                val methodUnauthorized = method.getAnnotation(Unauthorized::class.java)

                if (classUnauthorized != null || methodUnauthorized != null) {
                    val requestMapping = method.getAnnotation(RequestMapping::class.java)
                    val getMapping = method.getAnnotation(GetMapping::class.java)
                    val postMapping = method.getAnnotation(PostMapping::class.java)
                    val putMapping = method.getAnnotation(PutMapping::class.java)
                    val deleteMapping = method.getAnnotation(DeleteMapping::class.java)
                    val patchMapping = method.getAnnotation(PatchMapping::class.java)

                    val methodPaths = when {
                        requestMapping != null -> requestMapping.value.toList()
                        getMapping != null -> getMapping.value.toList()
                        postMapping != null -> postMapping.value.toList()
                        putMapping != null -> putMapping.value.toList()
                        deleteMapping != null -> deleteMapping.value.toList()
                        patchMapping != null -> patchMapping.value.toList()
                        else -> listOf("")
                    }

                    classPathPrefixes.forEach { classPrefix ->
                        methodPaths.forEach { methodPath ->
                            val fullPath = if (classPrefix.endsWith("/") && methodPath.startsWith("/")) {
                                "$classPrefix${methodPath.substring(1)}"
                            } else if (!classPrefix.endsWith("/") && !methodPath.startsWith("/") && methodPath.isNotEmpty()) {
                                "$classPrefix/$methodPath"
                            } else {
                                "$classPrefix$methodPath"
                            }

                            endpoints.add(fullPath)
                        }
                    }
                }
            }
        }

        return endpoints.distinct()
    }

    /**
     * Parse endpoint patterns to PathPattern for efficient matching
     */
    fun getUnauthorizedPathPatterns(): List<PathPattern> {
        return getUnauthorizedEndpointsSimple().map { endpoint ->
            // Convert path variables to Spring's path pattern syntax
            // e.g., /api/{version}/user -> /api/*/user for matching
            val pattern = endpoint.replace(Regex("\\{[^}]+\\}"), "*")
            pathPatternParser.parse(pattern)
        }
    }

    /**
     * Check if the given path matches any unauthorized endpoint pattern
     */
    fun matches(path: String): Boolean {
        val pathContainer = PathContainer.parsePath(path)
        return getUnauthorizedPathPatterns().any { pattern ->
            pattern.matches(pathContainer)
        }
    }
}