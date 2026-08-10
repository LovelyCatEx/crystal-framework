import {doGet, doPost, type ApiResponse} from "../system-request.ts";
import type {PaginatedResponseData} from "@/types/api.types.ts";
import type {
    ContactableTenantView,
    ConversationInboxVO,
    MsgMessage,
} from "@/types/message/message.types.ts";

/**
 * Frontend client for {@link com.lovelycatv.crystalframework.message.controller.MessageController}
 * (user-facing messaging endpoints). DTOs mirror backend exactly; every backend `Long` is
 * transmitted as `string`.
 */

// Mirrors SendMessageDTO. Peer message: user (sender) → another user, SYSTEM scope.
export interface SendMessageDTO {
    targetUserId: string;
    content: string;
    contentType?: number;
}

// Mirrors SendTenantMessageDTO. A tenant member replies as the tenant desk → an end user, TENANT scope.
export interface SendTenantMessageDTO {
    tenantId: string;
    targetUserId: string;
    content: string;
    contentType?: number;
}

// Mirrors SendInTenantMessageDTO. A tenant member sends directly to another member in the same tenant.
export interface SendInTenantMessageDTO {
    tenantId: string;
    targetUserId: string;
    content: string;
    contentType?: number;
}

// Mirrors SendToTenantDTO. User (sender) proactively contacts a tenant's customer-service desk.
export interface SendToTenantDTO {
    tenantId: string;
    content: string;
    contentType?: number;
}

// Query params for GET /message/contactable-tenants (paginated tenant directory for initiating conversations).
export interface ContactableTenantQueryParams {
    keyword?: string;
    page?: number;
    pageSize?: number;
}

/**
 * Send a message from the acting user to a tenant's customer-service desk. The message
 * is sent as USER (sender) → TENANT (target) inside TENANT scope, fanning out only to
 * members holding the reception permission. Finds or creates the isolated conversation.
 */
export async function sendToTenant(dto: SendToTenantDTO): Promise<ApiResponse<MsgMessage>> {
    return doPost('/api/message/send-to-tenant', dto, { 'Content-Type': 'application/json' });
}

/**
 * Send a peer message from the acting user to another user (SYSTEM scope). Finds or
 * creates the isolated user↔user conversation. Mirrors POST /message/send.
 */
export async function send(dto: SendMessageDTO): Promise<ApiResponse<MsgMessage>> {
    return doPost('/api/message/send', dto, { 'Content-Type': 'application/json' });
}

/**
 * Reply as a tenant's customer-service desk to an end user (TENANT scope). The acting
 * member's authority to act as the tenant is enforced server-side. Mirrors POST
 * /message/send-as-tenant.
 */
export async function sendAsTenant(dto: SendTenantMessageDTO): Promise<ApiResponse<MsgMessage>> {
    return doPost('/api/message/send-as-tenant', dto, { 'Content-Type': 'application/json' });
}

/** Send a direct message to a fellow member inside the supplied tenant scope. */
export async function sendInTenant(dto: SendInTenantMessageDTO): Promise<ApiResponse<MsgMessage>> {
    return doPost('/api/message/send-in-tenant', dto, { 'Content-Type': 'application/json' });
}

/**
 * Page through all tenants a user may initiate customer-service conversations with.
 * Results are capped at `pageSize` ≤ 20 per call. Optionally filtered by `keyword`
 * (name substring match).
 */
export async function queryContactableTenants(
    params: ContactableTenantQueryParams = {}
): Promise<ApiResponse<PaginatedResponseData<ContactableTenantView>>> {
    return doGet('/api/message/contactable-tenants', params);
}

/** GET /message/inbox-conversations — the acting user's conversations, enriched with counterpart party. */
export async function listInboxConversations(): Promise<ApiResponse<ConversationInboxVO[]>> {
    return doGet('/api/message/inbox-conversations');
}

/** GET /message/inbox-unread-count — total unread badge (conversation messages + announcements). */
export async function getInboxUnreadCount(): Promise<ApiResponse<number>> {
    return doGet('/api/message/inbox-unread-count');
}

/** GET /message/conversation-messages — page a conversation's messages (newest first). pageSize capped at 20 by backend. */
export async function queryConversationMessages(
    conversationId: string,
    page: number = 1,
    pageSize: number = 20,
): Promise<ApiResponse<PaginatedResponseData<MsgMessage>>> {
    return doGet('/api/message/conversation-messages', {conversationId, page, pageSize});
}

/** POST /message/mark-conversation-read — advance the acting user's read cursor to the conversation's last message. */
export async function markConversationRead(conversationId: string): Promise<ApiResponse<null>> {
    return doPost(`/api/message/mark-conversation-read?conversationId=${encodeURIComponent(conversationId)}`);
}
