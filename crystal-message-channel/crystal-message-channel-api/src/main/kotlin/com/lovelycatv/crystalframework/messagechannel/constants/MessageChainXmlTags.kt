package com.lovelycatv.crystalframework.messagechannel.constants

/**
 * Centralized XML tag / attribute names for [MessageChain] serialization.
 * Keep all magic strings here to avoid drift between parser and writer.
 */
object MessageChainXmlTags {
    const val TAG_AT = "at"
    const val TAG_IMAGE = "image"
    const val TAG_LINK = "link"
    const val TAG_BR = "br"

    const val ATTR_AT_USER = "user"
    const val ATTR_AT_TENANT = "tenant"
    const val ATTR_AT_DISPLAY_NAME = "name"

    const val ATTR_IMAGE_SRC = "src"

    const val ATTR_LINK_HREF = "href"
    const val ATTR_LINK_TITLE = "title"
}
