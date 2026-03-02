package com.lovelycatv.crystalframework.user.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Table("users")
class UserEntity(
    id: Long = 0,
    @Column(value = "username")
    private var username: String = "",
    @Column(value = "password")
    private var password: String = "",
    @Column(value = "email")
    var email: String = "",
    @Column(value = "nickname")
    var nickname: String = "",
    @Column("avatar")
    var avatar: Long? = null,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime), UserDetails {
    @Transient
    @JsonIgnore
    private var internalAuthorities: MutableCollection<out GrantedAuthority> = mutableSetOf()

    fun setInternalRawAuthorities(authorities: Iterable<String>) {
        setInternalAuthorities(authorities.map { SimpleGrantedAuthority(it) })
    }

    fun setInternalAuthorities(authorities: Iterable<GrantedAuthority>) {
        this.internalAuthorities = authorities.toMutableList()
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return this.internalAuthorities
    }

    override fun getPassword(): String {
        return if (!password.startsWith("{")) {
            "{bcrypt}$password"
        } else {
            password
        }
    }

    fun setPassword(rawPassword: String) {
        this.password = rawPassword
    }

    override fun getUsername(): String {
        return this.username
    }
}
