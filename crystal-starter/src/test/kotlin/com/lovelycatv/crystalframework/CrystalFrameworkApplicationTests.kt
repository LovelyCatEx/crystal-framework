package com.lovelycatv.crystalframework

import com.lovelycatv.crystalframework.config.ReactiveTestConfig
import com.lovelycatv.crystalframework.config.TestMockConfig
import com.lovelycatv.crystalframework.config.TestMockInitializer
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.utils.testWithTransactionalRollback
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.transaction.reactive.TransactionalOperator
import kotlin.reflect.javaType

@SpringBootTest
@Import(ReactiveTestConfig::class, TestMockConfig::class, TestMockInitializer::class)
abstract class CrystalFrameworkApplicationTests {
    @Autowired
    protected lateinit var transactionalOperator: TransactionalOperator

    protected fun withTransactionalRollback(
        actionName: String,
        action: suspend () -> Unit
    ) {
        testWithTransactionalRollback(actionName, transactionalOperator, action)
    }

    @OptIn(ExperimentalStdlibApi::class)
    protected final inline fun <reified T> getTestClassInstance(applicationContext: ApplicationContext): T {
        val constructor = T::class.constructors.minBy { it.parameters.size }
        return constructor.call(
            *constructor.parameters.map {
                val requiredType = it.type.javaType as Class<*>
                try {
                    if (ApplicationContext::class.java.isAssignableFrom(requiredType)) {
                        applicationContext
                    } else {
                        applicationContext.getBean(requiredType)
                    }
                } catch (e: Exception) {
                    throw BusinessException("Could not resolve required bean ${requiredType.canonicalName}", e)
                }
            }.toTypedArray()
        )
    }
}
