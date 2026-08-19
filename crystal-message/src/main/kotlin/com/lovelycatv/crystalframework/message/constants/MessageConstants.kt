package com.lovelycatv.crystalframework.message.constants

/**
 * Module-wide constants for the messaging domain. Keeping the dedupe-key
 * separators here (rather than inline) is what lets the isolation key be built
 * from one place and stay stable across the codebase.
 */
object MessageConstants {
    /** Separates the top-level segments of a conversation dedupe key (scope | kind | parties). */
    const val DEDUPE_SEGMENT_SEPARATOR = "|"

    /** Separates individual party keys within the parties segment. */
    const val DEDUPE_PARTY_SEPARATOR = "-"

    /** Rejected when a direct-message target is absent, deleted, disabled, or inactive. */
    const val TARGET_UNAVAILABLE_MESSAGE = "Message target is unavailable"

    /** Rejected when a direct-message sender and target resolve to the same party. */
    const val SELF_SEND_MESSAGE = "Cannot send a direct message to yourself"

    /** Upper bound for a single page of conversation messages (write-diffusion pull). */
    const val MAX_CONVERSATION_PAGE_SIZE = 20

    /** Upper bound for a single page of the broadcast history listing (read-diffusion pull). */
    const val MAX_BROADCAST_PAGE_SIZE = 20
}
