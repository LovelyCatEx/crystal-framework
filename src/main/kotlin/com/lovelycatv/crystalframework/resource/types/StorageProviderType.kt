package com.lovelycatv.crystalframework.resource.types

enum class StorageProviderType(val typeId: Int) {
    ALIYUN_OSS(0),
    TENCENT_COS(1);

    companion object {
        fun getByTypeId(typeId: Int): StorageProviderType? {
            return entries.find { it.typeId == typeId }
        }
    }
}