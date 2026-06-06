package com.lovelycatv.crystalframework.system.types

enum class AnnouncementStatus(val code: Int) {
    DRAFT(0),
    PUBLISHED(1),
    OFFLINE(2);

    companion object {
        fun fromCode(code: Int): AnnouncementStatus =
            entries.find { it.code == code }
                ?: throw IllegalArgumentException("Unknown announcement status code: $code")
    }
}
