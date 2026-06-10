package com.lovelycatv.crystalframework.messagechannel.types.chain

/**
 * Refers to a file stored via crystal-resource. The email/feishu renderer resolves
 * the resource id to a URL (or uploads to feishu and gets an image_key) when sending.
 */
data class ResourceImageSource(val resourceId: String) : ImageSource
