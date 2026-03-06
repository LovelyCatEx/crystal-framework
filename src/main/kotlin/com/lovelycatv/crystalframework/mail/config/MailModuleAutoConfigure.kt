package com.lovelycatv.crystalframework.mail.config

import com.lovelycatv.crystalframework.mail.entity.MailTemplateCategoryEntity
import com.lovelycatv.crystalframework.mail.entity.MailTemplateTypeEntity
import com.lovelycatv.crystalframework.mail.repository.MailTemplateCategoryRepository
import com.lovelycatv.crystalframework.mail.repository.MailTemplateTypeRepository
import com.lovelycatv.crystalframework.mail.types.MailTemplateCategoryDeclaration
import com.lovelycatv.crystalframework.mail.types.MailTemplateTypeDeclaration
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.shared.utils.SnowIdGenerator
import com.lovelycatv.crystalframework.shared.utils.awaitListWithTimeout
import com.lovelycatv.vertex.log.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.getBeansOfType
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.core.OrderComparator
import tools.jackson.databind.ObjectMapper

@Configuration
class MailModuleAutoConfigure(
    private val applicationContext: ApplicationContext,
    private val mailTemplateCategoryRepository: MailTemplateCategoryRepository,
    private val mailTemplateTypeRepository: MailTemplateTypeRepository,
    private val snowIdGenerator: SnowIdGenerator,
    private val objectMapper: ObjectMapper,
) : InitializingBean {
    private val logger = logger()

    private val categories = mutableListOf<MailTemplateCategoryDeclaration>()
    private val templateTypes = mutableListOf<MailTemplateTypeDeclaration>()


    override fun afterPropertiesSet() {
        val configures = applicationContext
            .getBeansOfType<MailModuleConfigure>()
            .values
            .sortedWith(OrderComparator.INSTANCE)

        configures.forEach {
            it.configureTemplateCategory(categories)
        }

        val categoryMap = this.categories.associateBy { it.name }

        configures.forEach {
            it.configureTemplateType(
                categoryMap,
                templateTypes
            )
        }

        runBlocking(Dispatchers.IO) {
            this@MailModuleAutoConfigure.process()
        }
    }

    private suspend fun process() {
        logger.info("=".repeat(64))

        val allCategoriesInDatabase = mailTemplateCategoryRepository
            .findAll()
            .awaitListWithTimeout()
            .associateBy { it.name }

        logger.info("Checking mail template categories...")

        val categoryWithEntityMap = this.categories.associateWith { categoryDeclaration ->
             if(!allCategoriesInDatabase.containsKey(categoryDeclaration.name)) {
                 val e = mailTemplateCategoryRepository.save(
                     MailTemplateCategoryEntity(
                         id = snowIdGenerator.nextId(),
                         name = categoryDeclaration.name,
                         description = categoryDeclaration.description
                     ) newEntity true
                 ).awaitFirstOrNull() ?: throw BusinessException("Could not create mail template category ${categoryDeclaration.name}")

                 logger.info("* ${categoryDeclaration.name}")

                 e
            } else {
                 logger.info("√ ${categoryDeclaration.name}")
                allCategoriesInDatabase[categoryDeclaration.name]!!
            }
        }

        val allTypesInDatabase = mailTemplateTypeRepository
            .findAll()
            .awaitListWithTimeout()
            .associateBy { it.name }

        logger.info("Checking mail template types...")

        val typeWithEntityMap = this.templateTypes.associateWith { typeDeclaration ->
            val variables = objectMapper.writeValueAsString(typeDeclaration.variables)

            if (!allTypesInDatabase.containsKey(typeDeclaration.name)) {
                val e = mailTemplateTypeRepository.save(
                    MailTemplateTypeEntity(
                        id = snowIdGenerator.nextId(),
                        name = typeDeclaration.name,
                        description = typeDeclaration.description,
                        variables = variables,
                        categoryId = categoryWithEntityMap[typeDeclaration.categoryDeclaration]!!.id,
                        allowMultiple = typeDeclaration.allowMultiple
                    ) newEntity true
                ).awaitFirstOrNull() ?: throw BusinessException("Could not create mail template type ${typeDeclaration.name}")

                logger.info("* ${typeDeclaration.name} (variables: $variables, allowMultiple: ${typeDeclaration.allowMultiple})")

                e
            } else {
                logger.info("√ ${typeDeclaration.name} (variables: $variables, allowMultiple: ${typeDeclaration.allowMultiple})")
                allTypesInDatabase[typeDeclaration.name]!!
            }
        }

        logger.info("=".repeat(64))
    }
}