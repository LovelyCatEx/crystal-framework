package com.lovelycatv.crystalframework.message.controller.vo

import tools.jackson.databind.annotation.JsonSerialize
import tools.jackson.databind.ser.std.ToStringSerializer

/**
 * Outward-facing view of one conversation entry in the acting user's inbox, expanded per *viewing
 * party* rather than per membership. A user may sit in one conversation under more than one identity
 * (e.g. the customer who also staffs the desk they contacted); each such identity yields its own entry.
 *
 * - [viewingPartyType] / [viewingPartyId] — the party the user is acting *as* in this entry
 *   (USER = personal, TENANT = a customer-service desk they staff). Drives which inbox section the
 *   entry lands in and, with the counterpart, which send endpoint a reply routes to.
 * - [counterpartType] / [counterpartId] / [counterpartName] — the *other* party, resolved so the
 *   frontend can render a title without a second round-trip.
 * - [counterpartInCurrentOrg] — whether the counterpart is a member of the caller's currently-acting
 *   tenant. Drives the org view's internal-vs-external split: a peer who is not a member of the org I
 *   am currently acting in is external. `false` whenever the caller supplied no acting tenant, when the
 *   counterpart is not a single user, or when the counterpart belongs to no such membership.
 *
 * [scopeType] / [scopeId] carry the conversation's isolation boundary. All `Long` fields are emitted
 * as `String` per the Long-serialization rule.
 */
data class ConversationInboxVO(
    @get:JsonSerialize(using = ToStringSerializer::class)
    val conversationId: Long,
    val unreadCount: Int,
    val scopeType: Int,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val scopeId: Long?,
    val viewingPartyType: Int,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val viewingPartyId: Long?,
    val viewingPartyName: String?,
    val counterpartType: Int,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val counterpartId: Long?,
    val counterpartName: String?,
    val counterpartInCurrentOrg: Boolean,
    @get:JsonSerialize(using = ToStringSerializer::class)
    val lastMessageTime: Long?,
)
