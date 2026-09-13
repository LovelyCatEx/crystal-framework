import {doPost} from "@/api/system-request.ts";
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
