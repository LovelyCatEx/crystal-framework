package com.lovelycatv.crystalframework.mail.config

import com.lovelycatv.crystalframework.mail.types.MailTemplateCategoryDeclaration
import com.lovelycatv.crystalframework.mail.types.MailTemplateDeclaration
import com.lovelycatv.crystalframework.mail.types.MailTemplateTypeDeclaration

interface MailModuleConfigure {
    fun configureTemplateCategory(
        categories: MutableList<MailTemplateCategoryDeclaration>
    ) {}

    fun configureTemplateType(
        categories: Map<String, MailTemplateCategoryDeclaration>,
        templateTypes: MutableList<MailTemplateTypeDeclaration>
    ) {}

    fun configureTemplate(
        categories: Map<String, MailTemplateCategoryDeclaration>,
        templateTypes: List<MailTemplateTypeDeclaration>,
        templates: MutableList<MailTemplateDeclaration>
    ) {}
}