/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.resource.config

import com.lovelycatv.crystalframework.resource.interfaces.RuleBasedStorageProviderRouter
import com.lovelycatv.crystalframework.resource.interfaces.StorageProviderRouter
import com.lovelycatv.crystalframework.resource.service.StorageProviderRoutingRuleService
import com.lovelycatv.crystalframework.resource.service.StorageProviderService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Default [StorageProviderRouter] bean, replaceable via [ConditionalOnMissingBean] by any
 * consumer that registers its own bean (e.g. reverting to a plain random router).
 */
@Configuration
class StorageProviderRouterConfiguration {
    @Bean
    @ConditionalOnMissingBean(StorageProviderRouter::class)
    fun storageProviderRouter(
        storageProviderRoutingRuleService: StorageProviderRoutingRuleService,
        storageProviderService: StorageProviderService
    ): RuleBasedStorageProviderRouter {
        return RuleBasedStorageProviderRouter(storageProviderRoutingRuleService, storageProviderService)
    }
}