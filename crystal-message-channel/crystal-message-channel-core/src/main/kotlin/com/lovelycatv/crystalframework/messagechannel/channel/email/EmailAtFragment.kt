package com.lovelycatv.crystalframework.messagechannel.channel.email

/**
 * Output of an [EmailAtResolver]. Rendered into the email body as an inline link
 * (or bold text when [profileUrl] is null).
 */
data class EmailAtFragment(
    val displayName: String,
    val profileUrl: String? = null,
)
