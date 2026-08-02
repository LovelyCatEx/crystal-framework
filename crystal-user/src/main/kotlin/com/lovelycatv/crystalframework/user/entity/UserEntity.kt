package com.lovelycatv.crystalframework.user.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.lovelycatv.crystalframework.shared.annotations.NotQueryable
import com.lovelycatv.crystalframework.shared.types.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.types.tenant.entity.UserAuthenticatedTenantVO
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

@Table("users")
class UserEntity(
    id: Long = 0,
    @Column(value = "username")
    private var username: String = "",
    @Column(value = "password")
    @field:NotQueryable
    private var password: String = "",
    @Column(value = "email")
    var email: String? = null,
    @Column(value = "nickname")
    var nickname: String = "",
    @Column("avatar")
    @get:JsonSerialize(using = ToStringSerializer::class)
    var avatar: Long? = null,
    @Column(value = "enabled")
    private var enabled: Boolean = true,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null
) : BaseEntity(id, createdTime, modifiedTime, deletedTime), UserDetails {
    @Transient
    @JsonIgnore
    private var internalAuthorities: MutableCollection<out GrantedAuthority> = mutableSetOf()

    @Transient
    @JsonIgnore
    private var authenticatedTenant: UserAuthenticatedTenantVO? = null

    @Transient
    private var banned: Boolean = false

    fun getAuthenticatedTenant(): UserAuthenticatedTenantVO? {
        return this.authenticatedTenant
    }

    fun setAuthenticatedTenant(authenticatedTenant: UserAuthenticatedTenantVO) {
        this.authenticatedTenant = authenticatedTenant
    }

    /**
     * Derived, non-persistent flag telling the manager UI whether this user currently has an
     * effective ban. Populated by [com.lovelycatv.crystalframework.user.controller.manager.ManagerUserController]
     * when building the user list response; defaults to false everywhere else.
     */
    fun getBanned(): Boolean {
        return this.banned
    }

    fun setBanned(banned: Boolean) {
        this.banned = banned
    }

    fun setInternalRawAuthorities(authorities: Iterable<String>) {
        setInternalAuthorities(authorities.map { SimpleGrantedAuthority(it) })
    }

    fun setInternalAuthorities(authorities: Iterable<GrantedAuthority>) {
        this.internalAuthorities = authorities.toMutableList()
    }

    @JsonIgnore
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return this.internalAuthorities
    }

    @JsonIgnore
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

    @JsonIgnore
    override fun isAccountNonExpired(): Boolean {
        return super.isAccountNonExpired()
    }

    @JsonIgnore
    override fun isAccountNonLocked(): Boolean {
        return super.isAccountNonLocked()
    }

    @JsonIgnore
    override fun isCredentialsNonExpired(): Boolean {
        return super.isCredentialsNonExpired()
    }

    @JsonIgnore
    override fun isEnabled(): Boolean {
        return this.enabled
    }

    fun getEnabledFlag(): Boolean {
        return this.enabled
    }

    fun setEnabledFlag(enabled: Boolean) {
        this.enabled = enabled
    }
}
