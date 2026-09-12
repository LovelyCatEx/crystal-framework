import {doPost} from "@/api/system-request.ts";
import type {ReasoningEffort} from "@/types/ai/ai.types.ts";

export interface AiPlaygroundMessage {
    role: "system" | "user" | "assistant";
    content: string;
    reasoningContent?: string;
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
}

export function chat(dto: AiPlaygroundChatDTO) {
    return doPost<AiPlaygroundChatVO>(
        "/api/manager/ai/playground/chat",
        dto,
        {"Content-Type": "application/json"},
    );
}
