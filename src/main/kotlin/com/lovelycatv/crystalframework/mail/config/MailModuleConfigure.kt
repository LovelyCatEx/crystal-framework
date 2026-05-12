package com.lovelycatv.crystalframework.mail.config

import com.lovelycatv.crystalframework.mail.types.MailTemplateCategoryDeclaration
import com.lovelycatv.crystalframework.mail.types.MailTemplateDeclaration
import com.lovelycatv.crystalframework.mail.types.MailTemplateTypeDeclaration

interface MailModuleConfigure {
    fun configureTemplateCategory(
        categories: MutableList<MailTemplateCategoryDeclaration>
    ) { // Register mail template categories
    }

    fun configureTemplateType(
        categories: Map<String, MailTemplateCategoryDeclaration>,
        templateTypes: MutableList<MailTemplateTypeDeclaration>
    ) { // Register mail template types under the given categories
    }

    fun configureTemplate(
        categories: Map<String, MailTemplateCategoryDeclaration>,
        templateTypes: List<MailTemplateTypeDeclaration>,
        templates: MutableList<MailTemplateDeclaration>
    ) { // Register concrete mail templates with their associated category and type
    }
}