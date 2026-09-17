/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {doGet, doPost} from "@/api/system-request.ts";
import {sseRequest} from "@/api/request.ts";
import {getUserAuthentication} from "@/utils/token.utils.ts";
import type {ReasoningEffort} from "@/types/ai/ai.types.ts";

export interface AiPlaygroundMessage {
    role: "system" | "user" | "assistant";
    content: string;
    reasoningContent?: string;
    usage?: AiPlaygroundUsage;
    toolCalls?: AiPlaygroundToolCall[];
}

export interface AiPlaygroundUsage {
    promptTokens: number;
    completionTokens: number;
    reasoningTokens: number;
    cachedPromptTokens: number;
    cacheCreationTokens: number;
}

export interface AiPlaygroundToolCall {
    toolName: string;
    arguments: Record<string, any> | null;
    result: string;
}

export interface AiPlaygroundChatDTO {
    modelId: string;
    messages: AiPlaygroundMessage[];
    /** Omitted to let the provider's protocol pick the level. */
    reasoningEffort?: ReasoningEffort;
    sessionId?: string;
    groupId?: string;
}

export interface AiPlaygroundChatVO {
    content: string;
    reasoningContent?: string;
    usage?: AiPlaygroundUsage | null;
    toolCalls?: AiPlaygroundToolCall[] | null;
}

export interface AiPlaygroundStreamChunk {
    content: string | null;
    reasoningContent: string | null;
    finished: boolean;
    usage?: AiPlaygroundUsage | null;
    toolCall?: AiPlaygroundToolCall | null;
}

export interface AiPlaygroundProvider {
    name: string;
    modelIds: string[];
}

export interface AiPlaygroundGroup {
    name: string;
    billingMultiplier: string;
    modelIds: string[];
}

export interface AiPlaygroundModel {
    displayName: string;
    key: string;
    inputPricePerMillion: string;
    outputPricePerMillion: string;
    cacheReadPricePerMillion: string | null;
    cacheWritePricePerMillion: string | null;
    capabilities: number[];
    contextWindowTokens: string;
    currencyId: string;
}

export interface AiPlaygroundData {
    providers: Record<string, AiPlaygroundProvider>;
    groups: Record<string, AiPlaygroundGroup>;
    models: Record<string, AiPlaygroundModel>;
}

export function getPlaygroundData() {
    return doGet<AiPlaygroundData>("/api/manager/ai/playground/data");
}

export function chat(dto: AiPlaygroundChatDTO) {
    return doPost<AiPlaygroundChatVO>(
        "/api/manager/ai/playground/chat",
        dto,
        {"Content-Type": "application/json"},
    );
}

/**
 * Streams a chat completion, invoking [onChunk] for every SSE frame until the stream ends.
 *
 * The SSE reading/parsing lives in `sseRequest`; this only supplies the endpoint, the auth header
 * and the chunk type.
 */
export async function chatStream(
    dto: AiPlaygroundChatDTO,
    onChunk: (chunk: AiPlaygroundStreamChunk) => void,
): Promise<void> {
    const headers: Record<string, string> = {};
    const authentication = getUserAuthentication();
    if (authentication && !authentication.expired) {
        headers["Authorization"] = `Bearer ${authentication.token}`;
    }

    await sseRequest<AiPlaygroundStreamChunk>(
        "/api/manager/ai/playground/chat-stream",
        headers,
        dto,
        onChunk,
    );
}
