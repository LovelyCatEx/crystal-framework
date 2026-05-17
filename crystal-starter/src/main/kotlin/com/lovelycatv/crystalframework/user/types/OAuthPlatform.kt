package com.lovelycatv.crystalframework.user.types

enum class OAuthPlatform(val typeId: Int) {
    GITHUB(0),
    GOOGLE(1),
    OICQ(2);

    companion object {
        fun getByTypeId(typeId: Int): OAuthPlatform? {
            return entries.find { it.typeId == typeId }
        }
    }
}