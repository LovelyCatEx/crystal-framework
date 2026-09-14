/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import type {BaseEntity} from "../BaseEntity.ts";

export interface AiProviderEntity extends BaseEntity {
    name: string;
    key: string;
    description: string | null;
    protocolType: number;
    baseUrl: string;
    apiKey: string;
    chatCompletionsPath: string | null;
    embeddingPath: string | null;
    requestConfig: string;
    responseConfig: string;
    enabled: boolean;
    sort: number;
}

export interface AiModelEntity extends BaseEntity {
    providerId: string;
    key: string;
    modelName: string;
    displayName: string;
    description: string | null;
    capabilities: string;
    contextWindowTokens: string;
    maxOutputTokens: string | null;
    inputPricePerMillion: string;
    outputPricePerMillion: string;
    cacheReadPricePerMillion: string | null;
    cacheWritePricePerMillion: string | null;
    currency: string;
    requestConfig: string;
    enabled: boolean;
    sort: number;
}

export interface AiUserGroupEntity extends BaseEntity {
    name: string;
    key: string;
    description: string | null;
    billingMultiplier: string;
    enabled: boolean;
    isDefault: boolean;
    sort: number;
}

export interface AiModelInvocationRecordEntity extends BaseEntity {
    requestId: string;
    userId: string;
    tenantId: string | null;
    sessionId: string | null;
    providerId: string;
    modelId: string;
    promptTokens: number;
    cachedPromptTokens: number;
    completionTokens: number;
    reasoningTokens: number;
    cacheCreationTokens: number;
    toolCallsCount: number;
    messageCount: number;
    isStreaming: boolean;
    timeToFirstTokenMs: string;
    totalDurationMs: string;
    queueWaitMs: string | null;
    tokensPerSecond: number | null;
    promptUnitPrice: number;
    completionUnitPrice: number;
    cacheReadUnitPrice: number;
    cacheWriteUnitPrice: number;
    groupId: string | null;
    groupMultiplier: number;
    rawCost: number;
    finalCost: number;
    currency: string;
    temperature: number | null;
    topP: number | null;
    maxTokens: number | null;
    status: number;
    errorCode: string | null;
    errorMessage: string | null;
    stopReason: string | null;
    clientIp: string | null;
    userAgent: string | null;
    requestSizeBytes: string | null;
    responseSizeBytes: string | null;
}

export enum AiProviderProtocolType {
    OPENAI = 0,
    GEMINI = 1,
    ANTHROPIC = 2
}

export enum AiModelCapability {
    CHAT = 0,
    TEXT_GENERATION = 1,
    VISION = 2,
    EMBEDDING = 3,
    TOOL_CALLING = 4,
    STRUCTURED_OUTPUT = 5,
    AUDIO_INPUT = 6,
    AUDIO_OUTPUT = 7
}

/**
 * Corresponds to VertexLib's `ReasoningEffort` enum
 * (VertexLib/ai/llm/ReasoningEffort.kt), which carries no numeric id, so it travels by name the
 * same way `ForbiddenReason` does.
 */
export enum ReasoningEffort {
    DISABLED = "DISABLED",
    AUTO = "AUTO",
    MINIMAL = "MINIMAL",
    LOW = "LOW",
    MEDIUM = "MEDIUM",
    HIGH = "HIGH",
    EXTRA_HIGH = "EXTRA_HIGH",
    MAX = "MAX"
}

export enum AiModelInvocationStatus {
    FAILED = 0,
    SUCCESS = 1
}

/**
 * Mirrors VertexLib's `LLMUsageResolverJsonPathConfig` — the persisted `responseConfig` is parsed
 * straight into that type on the backend, so these key names are not interchangeable with the
 * earlier `inputTokensPath` / `outputTokensPath` spelling.
 */
export interface LlmUsageResolverJsonPathConfig {
    promptTokensPath?: string | null;
    completionTokensPath?: string | null;
    reasoningTokensPath?: string | null;
    cacheReadTokensPath?: string | null;
    cacheWriteTokensPath?: string | null;
}

/** Mirrors VertexLib's `LLMEndpointResponseConfig`. */
export interface LlmEndpointResponseConfig {
    errorMessageJsonPath?: string | null;
    usage?: LlmUsageResolverJsonPathConfig | null;
}

/** Mirrors VertexLib's `LLMResponseConfig`. */
export interface LlmResponseConfig {
    chatCompletions?: LlmEndpointResponseConfig | null;
    embedding?: LlmEndpointResponseConfig | null;
}

export interface DefaultProviderConfigsVO {
    openai: LlmResponseConfig;
    anthropic: LlmResponseConfig;
}

export interface AiProviderRequestConfig {
    headers: Record<string, string>;
}
