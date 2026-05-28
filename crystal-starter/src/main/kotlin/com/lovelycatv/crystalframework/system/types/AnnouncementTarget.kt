package com.lovelycatv.crystalframework.system.types

enum class AnnouncementTarget(val code: Int) {
    USER_ONLY(0),
    MANAGER_ONLY(1),
    BOTH(2);

    companion object {
        fun fromCode(code: Int): AnnouncementTarget =
            entries.find { it.code == code }
                ?: throw IllegalArgumentException("Unknown announcement target code: $code")
    }
}
