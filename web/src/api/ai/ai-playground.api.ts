import {doPost} from "@/api/system-request.ts";

export interface AiPlaygroundMessage {
    role: "system" | "user" | "assistant";
    content: string;
    reasoningContent?: string;
}

export interface AiPlaygroundChatDTO {
    modelId: string;
    messages: AiPlaygroundMessage[];
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
