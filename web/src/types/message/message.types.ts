/**
 * Types for user-facing messaging (conversations, customer-service contact).
 * Mirrors backend value objects from crystal-message/controller/vo and SDK types.
 */

/**
 * Minimal read-only tenant view for the contactable-tenant directory. Mirrors
 * {@link com.lovelycatv.crystalframework.sdk.message.config.ContactableTenantView}.
 */
export interface ContactableTenantView {
    id: string;      // Long serialized as String
    name: string;
}

/**
 * One inbox entry, expanded per *viewing party* (the identity the user acts as). Mirrors
 * {@link com.lovelycatv.crystalframework.message.controller.vo.ConversationInboxVO}. A user who
 * staffs the very desk they contacted gets two entries for one conversation — one under `USER`
 * (personal) and one under the `TENANT` desk. `viewingPartyType` / `counterpartType` are PartyType
 * typeIds (0=USER, 1=SYSTEM, 2=TENANT); `scopeType` is a ScopeType typeId (0=SYSTEM, 1=TENANT).
 * The viewing party, with the counterpart, tells the frontend which send endpoint a reply routes to.
 */
export interface ConversationInboxVO {
    conversationId: string;
    unreadCount: number;
    scopeType: number;
    scopeId: string | null;
    viewingPartyType: number;
    viewingPartyId: string | null;
    viewingPartyName: string | null;
    counterpartType: number;
    counterpartId: string | null;
    counterpartName: string | null;
    // Whether the counterpart is a member of the tenant I am currently acting as. Drives the org
    // view's internal-vs-external split; always false in a plain system-user session.
    counterpartInCurrentOrg: boolean;
    lastMessageTime: string | null;
}

/**
 * One message inside a conversation. Mirrors
 * {@link com.lovelycatv.crystalframework.message.entity.MsgMessageEntity}. `senderPartyType`
 * is a PartyType typeId (0=USER, 1=SYSTEM, 2=TENANT); `contentType` is a ContentType typeId
 * (0=TEXT, 1=IMAGE, 2=LINK).
 */
export interface MsgMessage {
    id: string;
    conversationId: string;
    senderPartyType: number;
    senderPartyId: string | null;
    actingUserId: string | null;
    contentType: number;
    content: string;
    createdTime: string;
    modifiedTime: string;
}
