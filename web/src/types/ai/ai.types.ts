import type {BaseEntity} from "../BaseEntity.ts";

export interface AiProviderEntity extends BaseEntity {
    name: string;
    key: string;
    description: string | null;
    protocolType: number;
    baseUrl: string;
    apiKey: string;
    requestConfig: string;
    responseConfig: string;
    enabled: boolean;
    sort: number;
}

export interface AiModelEntity extends BaseEntity {
    providerId: string;
    name: string;
    key: string;
    description: string | null;
    capabilities: string;
    inputPricePerMillion: string;
    outputPricePerMillion: string;
    maxTokens: number | null;
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
