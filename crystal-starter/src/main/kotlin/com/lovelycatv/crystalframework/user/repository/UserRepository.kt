package com.lovelycatv.crystalframework.user.repository

import com.lovelycatv.crystalframework.shared.repository.BaseRepository
import com.lovelycatv.crystalframework.user.entity.UserEntity
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface UserRepository : BaseRepository<UserEntity> {
    fun findByUsernameOrEmail(
        username: String,
        email: String
    ): Mono<UserEntity>

    fun findByEmail(email: String): Mono<UserEntity>

    @Query(
        """
        SELECT * FROM users 
        WHERE (LOWER(username) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(email) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(nickname) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY created_time DESC
        LIMIT :limit
        OFFSET :offset
    """
    )
    override fun searchByKeyword(
        keyword: String,
        limit: Int,
        offset: Int
    ): Flux<UserEntity>

    @Query(
        """
        SELECT COUNT(*) FROM users 
        WHERE LOWER(username) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(email) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """
    )
    override fun countByKeyword(
        keyword: String
    ): Mono<Long>

    @Modifying
    @Query("UPDATE users SET avatar = :avatar WHERE id = :id")
    fun updateAvatar(id: Long, avatar: Long): Mono<Long>
    fun findByUsername(username: String): Mono<UserEntity>

    @Query(
        """
        SELECT * FROM users 
        WHERE (:#{#keyword == null} = true
           OR (LOWER(username) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(email) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))))
        AND (:#{#username == null} = true OR LOWER(username) LIKE LOWER(CONCAT('%', :username, '%')))
        AND (:#{#email == null} = true OR LOWER(email) LIKE LOWER(CONCAT('%', :email, '%')))
        AND (:#{#nickname == null} = true OR LOWER(nickname) LIKE LOWER(CONCAT('%', :nickname, '%')))
        AND (:#{#startTime == null} = true OR created_time >= :startTime)
        AND (:#{#endTime == null} = true OR created_time <= :endTime)
        ORDER BY created_time DESC
        LIMIT :limit
        OFFSET :offset
    """
    )
    fun advanceSearch(
        keyword: String?,
        username: String?,
        email: String?,
        nickname: String?,
        startTime: Long?,
        endTime: Long?,
        limit: Int,
        offset: Int
    ): Flux<UserEntity>

    @Query(
        """
        SELECT COUNT(*) FROM users 
        WHERE (:#{#keyword == null} = true
           OR (LOWER(username) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(email) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(nickname) LIKE LOWER(CONCAT('%', :keyword, '%'))))
        AND (:#{#username == null} = true OR LOWER(username) LIKE LOWER(CONCAT('%', :username, '%')))
        AND (:#{#email == null} = true OR LOWER(email) LIKE LOWER(CONCAT('%', :email, '%')))
        AND (:#{#nickname == null} = true OR LOWER(nickname) LIKE LOWER(CONCAT('%', :nickname, '%')))
        AND (:#{#startTime == null} = true OR created_time >= :startTime)
        AND (:#{#endTime == null} = true OR created_time <= :endTime)
    """
    )
    fun countAdvanceSearch(
        keyword: String?,
        username: String?,
        email: String?,
        nickname: String?,
        startTime: Long?,
        endTime: Long?,
    ): Mono<Long>

    @Query("SELECT COUNT(*) FROM users WHERE created_time >= :startTime AND created_time < :endTime")
    fun countByCreatedTimeBetween(startTime: Long, endTime: Long): Mono<Long>
}