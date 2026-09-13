/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.lovelycatv.crystalframework.messagechannel.channel.email

/**
 * Output of an [EmailAtResolver]. Rendered into the email body as an inline link
 * (or bold text when [profileUrl] is null).
 */
data class EmailAtFragment(
    val displayName: String,
    val profileUrl: String? = null,
)
