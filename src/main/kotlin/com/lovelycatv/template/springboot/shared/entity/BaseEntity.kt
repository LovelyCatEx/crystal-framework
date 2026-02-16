package com.lovelycatv.template.springboot.shared.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

abstract class BaseEntity(
    @Id
    @get:JsonSerialize(using = ToStringSerializer::class)
    private val id: Long = 0,
    @Column(value = "created_time")
    open val createdTime: Long = System.currentTimeMillis(),
    @Column(value = "modified_time")
    open var modifiedTime: Long = System.currentTimeMillis(),
    @Column(value = "deleted_time")
    open var deletedTime: Long? = null
) : Persistable<Long> {
    @Transient
    private var isNew = false

    fun newEntity() {
        this.isNew = true
    }

    override fun getId(): Long {
        return this.id
    }

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

    fun isDeleted() = deletedTime != null

    @Suppress("UNCHECKED_CAST")
    infix fun <E: BaseEntity> newEntity(v: Boolean): E {
        this.isNew = v
        return this as E
    }
}