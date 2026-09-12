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

export interface AiUsageJsonPathConfig {
    inputTokensPath?: string | null;
    outputTokensPath?: string | null;
    totalTokensPath?: string | null;
    cacheReadTokensPath?: string | null;
    cacheWriteTokensPath?: string | null;
}

export interface AiEndpointResponseConfig {
    contentPath?: string | null;
    finishReasonPath?: string | null;
    providerRequestIdPath?: string | null;
    usage?: AiUsageJsonPathConfig | null;
    errorMessagePath?: string | null;
}

export interface AiProviderResponseConfig {
    chatCompletions?: AiEndpointResponseConfig | null;
    embedding?: AiEndpointResponseConfig | null;
}

export interface DefaultProviderConfigsVO {
    openai: AiProviderResponseConfig;
    anthropic: AiProviderResponseConfig;
}

export enum AiHttpMethod {
    GET = 0,
    POST = 1
}

export interface AiGenericHttpConfig {
    method: AiHttpMethod;
    path: string;
    headers: Record<string, string>;
    bodyTemplate: Record<string, any>;
    response: AiEndpointResponseConfig;
}

export interface AiProviderRequestConfig {
    headers: Record<string, string>;
    genericHttp?: AiGenericHttpConfig | null;
}
