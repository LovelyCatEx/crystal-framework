/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.sdk.mail.types

data class MailTemplateDeclaration(
    val name: String,
    val description: String?,
    val title: String,
    val content: String,
    val active: Boolean,
    val type: MailTemplateTypeDeclaration,
)