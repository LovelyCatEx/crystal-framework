package com.lovelycatv.crystalframework.tenant.config

import com.lovelycatv.crystalframework.sdk.rbac.tenant.benefit.TenantBenefitRegistry
import com.lovelycatv.crystalframework.sdk.rbac.tenant.benefit.config.TenantBenefitConfigurer
import com.lovelycatv.crystalframework.tenant.constants.TenantBenefit
import org.springframework.stereotype.Component

@Component
class TenantBenefitBuiltinConfigurer : TenantBenefitConfigurer {
    override fun configure(registry: TenantBenefitRegistry) {
        registry.benefits(TenantBenefit.allBenefits())
    }
}
