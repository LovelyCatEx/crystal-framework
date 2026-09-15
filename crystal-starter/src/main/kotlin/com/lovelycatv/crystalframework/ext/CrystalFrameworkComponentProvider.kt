/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.ext

import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.type.classreading.MetadataReader
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.stereotype.Component

/**
 * [ClassPathScanningCandidateComponentProvider] that uses a custom [java.lang.ClassLoader]
 * and only includes default component stereotypes (@Component, @Service, @Repository,
 * @Controller, @Configuration, etc.).
 */
internal class CrystalFrameworkComponentProvider : ClassPathScanningCandidateComponentProvider(false) {
    init {
        addIncludeFilter(AnnotationTypeFilter(Component::class.java))
    }

    override fun isCandidateComponent(metadataReader: MetadataReader): Boolean {
        return try {
            super.isCandidateComponent(metadataReader)
        } catch (_: Exception) {
            false
        }
    }
}
