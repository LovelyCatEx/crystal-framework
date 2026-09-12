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

export enum AiProviderProtocolType {
    OPENAI = 0,
    GEMINI = 1,
    ANTHROPIC = 2
}

export enum AiModelCapability {
    TEXT_GENERATION = 0,
    IMAGE_GENERATION = 1,
    AUDIO_GENERATION = 2,
    VIDEO_GENERATION = 3,
    VISION = 4,
    AUDIO_TRANSCRIPTION = 5,
    EMBEDDING = 6,
    CODE_GENERATION = 7
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
