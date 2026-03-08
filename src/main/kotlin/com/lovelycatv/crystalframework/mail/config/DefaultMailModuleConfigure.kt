package com.lovelycatv.crystalframework.mail.config

import com.lovelycatv.crystalframework.mail.constants.SystemMailDeclaration
import com.lovelycatv.crystalframework.mail.types.MailTemplateCategoryDeclaration
import com.lovelycatv.crystalframework.mail.types.MailTemplateTypeDeclaration
import org.springframework.context.annotation.Configuration

@Configuration
class DefaultMailModuleConfigure : MailModuleConfigure {
    override fun configureTemplateCategory(categories: MutableList<MailTemplateCategoryDeclaration>) {
        categories.addAll(SystemMailDeclaration.categories)
    }

    override fun configureTemplateType(
        categories: Map<String, MailTemplateCategoryDeclaration>,
        templateTypes: MutableList<MailTemplateTypeDeclaration>
    ) {
        templateTypes.addAll(SystemMailDeclaration.types)
    }
}