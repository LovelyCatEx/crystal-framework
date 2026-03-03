package com.lovelycatv.crystalframework.resource.types

enum class StorageProviderType(val typeId: Int) {
    LOCAL_FILE_SYSTEM(0),
    ALIYUN_OSS(1),
    TENCENT_COS(2);

    companion object {
        fun getByTypeId(typeId: Int): StorageProviderType? {
            return entries.find { it.typeId == typeId }
        }
    }
}