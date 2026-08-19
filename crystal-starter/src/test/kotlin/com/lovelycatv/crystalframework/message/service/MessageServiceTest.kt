package com.lovelycatv.crystalframework.message.service

import com.lovelycatv.crystalframework.CrystalFrameworkApplicationTests
import com.lovelycatv.crystalframework.message.repository.MsgConversationRepository
import com.lovelycatv.crystalframework.message.repository.MsgMessageRepository
import com.lovelycatv.crystalframework.message.types.ContentType
import com.lovelycatv.crystalframework.sdk.message.Party
import com.lovelycatv.crystalframework.sdk.message.Scope
import com.lovelycatv.crystalframework.sdk.message.types.PartyType
import com.lovelycatv.crystalframework.sdk.message.types.ScopeType
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.user.service.UserManagerService
import com.lovelycatv.crystalframework.user.service.UserServiceTest
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MessageServiceTest(
    @Autowired private val messageService: MessageService,
    @Autowired private val msgConversationRepository: MsgConversationRepository,
    @Autowired private val msgMessageRepository: MsgMessageRepository,
    @Autowired private val userManagerService: UserManagerService,
    @Autowired private val applicationContext: ApplicationContext,
) : CrystalFrameworkApplicationTests() {
    private val userServiceTest: UserServiceTest by lazy { getTestClassInstance(applicationContext) }

    @Test
    fun rejectsUnavailableUserTargetBeforePersisting() {
        withTransactionalRollback("message-rejects-unavailable-user-target") {
            val sender = userServiceTest.mockRegisteredUser()
            val conversationCount = msgConversationRepository.count().awaitFirstOrNull()
            val messageCount = msgMessageRepository.count().awaitFirstOrNull()

            val result = runCatching {
                messageService.send(
                    scope = Scope(ScopeType.SYSTEM, null),
                    sender = Party(PartyType.USER, sender.id),
                    target = Party(PartyType.USER, NONEXISTENT_TARGET_USER_ID),
                    content = TEST_MESSAGE_CONTENT,
                    contentType = ContentType.TEXT,
                    actingUserId = sender.id,
                )
            }

            assertIs<BusinessException>(result.exceptionOrNull())
            assertEquals(conversationCount, msgConversationRepository.count().awaitFirstOrNull())
            assertEquals(messageCount, msgMessageRepository.count().awaitFirstOrNull())
        }
    }

    @Test
    fun rejectsSelfSendBeforePersisting() {
        withTransactionalRollback("message-rejects-self-send") {
            val user = userServiceTest.mockRegisteredUser()
            val conversationCount = msgConversationRepository.count().awaitFirstOrNull()
            val messageCount = msgMessageRepository.count().awaitFirstOrNull()

            val result = runCatching {
                messageService.send(
                    scope = Scope(ScopeType.SYSTEM, null),
                    sender = Party(PartyType.USER, user.id),
                    target = Party(PartyType.USER, user.id),
                    content = TEST_MESSAGE_CONTENT,
                    contentType = ContentType.TEXT,
                    actingUserId = user.id,
                )
            }

            assertIs<BusinessException>(result.exceptionOrNull())
            assertEquals(conversationCount, msgConversationRepository.count().awaitFirstOrNull())
            assertEquals(messageCount, msgMessageRepository.count().awaitFirstOrNull())
        }
    }

    @Test
    fun rejectsDisabledUserTargetBeforePersisting() {
        withTransactionalRollback("message-rejects-disabled-user-target") {
            val sender = userServiceTest.mockRegisteredUser()
            val target = userServiceTest.mockRegisteredUser()
            userManagerService.setEnabled(target.id, false)
            userManagerService.removeCache(target.id)
            val conversationCount = msgConversationRepository.count().awaitFirstOrNull()
            val messageCount = msgMessageRepository.count().awaitFirstOrNull()

            val result = runCatching {
                messageService.send(
                    scope = Scope(ScopeType.SYSTEM, null),
                    sender = Party(PartyType.USER, sender.id),
                    target = Party(PartyType.USER, target.id),
                    content = TEST_MESSAGE_CONTENT,
                    contentType = ContentType.TEXT,
                    actingUserId = sender.id,
                )
            }

            assertIs<BusinessException>(result.exceptionOrNull())
            assertEquals(conversationCount, msgConversationRepository.count().awaitFirstOrNull())
            assertEquals(messageCount, msgMessageRepository.count().awaitFirstOrNull())
        }
    }

    private companion object {
        const val NONEXISTENT_TARGET_USER_ID = Long.MAX_VALUE
        const val TEST_MESSAGE_CONTENT = "test"
    }
}
