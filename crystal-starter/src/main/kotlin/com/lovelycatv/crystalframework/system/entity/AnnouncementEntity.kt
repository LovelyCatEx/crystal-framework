package com.lovelycatv.crystalframework.system.entity

import com.lovelycatv.crystalframework.shared.entity.BaseEntity
import com.lovelycatv.crystalframework.shared.exception.BusinessException
import com.lovelycatv.crystalframework.system.types.AnnouncementStatus
import com.lovelycatv.crystalframework.system.types.AnnouncementTarget
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

/**
 * System announcement entity.
 *
 * status: 0=draft, 1=published, 2=offline
 * target: 0=user-side only, 1=manager-side only, 2=both
 */
@Table("system_announcements")
class AnnouncementEntity(
    id: Long = 0,
    @Column("title")
    var title: String = "",
    @Column("content")
    var content: String = "",
    @Column("status")
    var status: Int = 0,
    @Column("target")
    var target: Int = 2,
    @Column("priority")
    var priority: Int = 0,
    createdTime: Long = System.currentTimeMillis(),
    modifiedTime: Long = System.currentTimeMillis(),
    deletedTime: Long? = null,
) : BaseEntity(id, createdTime, modifiedTime, deletedTime) {

    fun getRealStatus(): AnnouncementStatus =
        AnnouncementStatus.entries.find { it.code == status }
            ?: throw BusinessException("Invalid announcement status code: $status")

    fun getRealTarget(): AnnouncementTarget =
        AnnouncementTarget.entries.find { it.code == target }
            ?: throw BusinessException("Invalid announcement target code: $target")
}
