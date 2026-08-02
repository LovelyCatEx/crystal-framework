package com.lovelycatv.crystalframework.user.constants

/**
 * Constants shared by the username/password credential-verification paths (e.g. the unauthenticated
 * OAuth-bind flow in [com.lovelycatv.crystalframework.user.service.impl.UserServiceImpl.bindUserFromOAuthAccount]).
 *
 * Both "account does not exist" and "wrong password" must surface the SAME message so an unauthenticated
 * caller cannot use the response to tell whether an account exists (username enumeration / credential
 * probing). The account-absent branch additionally runs a bcrypt match against [DUMMY_PASSWORD_HASH] so the
 * two branches take comparable time and cannot be distinguished via a timing side-channel.
 */
object CredentialAuthConstants {
    /** Unified, non-differential error for any username/password verification failure. */
    const val MESSAGE_INVALID_CREDENTIALS = "Incorrect username or password"

    /**
     * A real bcrypt hash of a fixed dummy password, used only to burn an equivalent amount of CPU time when the
     * account does not exist. It never matches any user-supplied password, so the surrounding check always fails.
     */
    const val DUMMY_PASSWORD_HASH = "\$2b\$10\$H9Hh7TjMq.BHhcr49eIL/eEIVke.maPMJ41hFGXdneankaK3Qz1ka"
}
