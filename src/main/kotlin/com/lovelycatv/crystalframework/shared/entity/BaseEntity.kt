package com.lovelycatv.crystalframework.shared.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

abstract class BaseEntity(
    @Id
    @get:JsonSerialize(using = ToStringSerializer::class)
    private var id: Long = 0,
    @Column(value = "created_time")
    @get:JsonSerialize(using = ToStringSerializer::class)
    open val createdTime: Long = System.currentTimeMillis(),
    @Column(value = "modified_time")
    @get:JsonSerialize(using = ToStringSerializer::class)
    open var modifiedTime: Long = System.currentTimeMillis(),
    @Column(value = "deleted_time")
    open var deletedTime: Long? = null
) : Persistable<Long> {
    @Transient
    private var isNew = false

    fun newEntity() {
        this.isNew = true
    }

    @JsonSerialize(using = ToStringSerializer::class)
    override fun getId(): Long {
        return this.id
    }

    fun setId(id: Long) {
        this.id = id
    }

    @JsonIgnore
    override fun isNew(): Boolean {
        return isNew
    }

    fun onUpdate() {
        modifiedTime = System.currentTimeMillis()
    }

    fun softDelete() {
        deletedTime = System.currentTimeMillis()
    }

    fun restore() {
        deletedTime = null
    }

    @JsonIgnore
    fun isDeleted() = deletedTime != null

    @Suppress("UNCHECKED_CAST")
    infix fun <E: BaseEntity> newEntity(v: Boolean): E {
        this.isNew = v
        return this as E
    }
}