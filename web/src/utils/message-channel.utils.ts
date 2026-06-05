import {ChannelType} from "@/types/tenant/tenant-message-channel.types.ts";

/**
 * Strips persisted ciphertext out of a channel's `config` JSON before it is shown in the
 * edit form.
 *
 * The backend persists sensitive fields (e.g. email `password`, Lark `appSecret`) AES-encrypted
 * with an `ENC:` prefix, and re-encrypts the whole `config` payload on every update. The plaintext
 * secret is never sent to the front-end, so any `ENC:`-prefixed value left in the form would be
 * double-encrypted (and corrupted) on save. We blank those fields so the operator must re-enter
 * them, which is also the only way to change them.
 */
const ENC_PREFIX = "ENC:";

export function sanitizeMessageChannelConfig(config: string): string {
    try {
        const parsed = JSON.parse(config);
        if (typeof parsed !== "object" || parsed === null || Array.isArray(parsed)) {
            return config;
        }
        let mutated = false;
        for (const [key, val] of Object.entries(parsed)) {
            if (typeof val === "string" && val.startsWith(ENC_PREFIX)) {
                (parsed as Record<string, unknown>)[key] = "";
                mutated = true;
            }
        }
        return mutated ? JSON.stringify(parsed, null, 2) : config;
    } catch {
        return config;
    }
}

export interface ChannelConfigPreset {
    key: string;
    config: Record<string, unknown>;
}

export const MESSAGE_CHANNEL_PRESETS: Record<ChannelType, ChannelConfigPreset[]> = {
    [ChannelType.EMAIL]: [
        {
            key: 'empty',
            config: {host: '', port: 465, username: '', password: '', ssl: true, fromEmail: ''}
        },
        {
            key: 'mail163',
            config: {host: 'smtp.163.com', port: 465, username: '', password: '', ssl: true, fromEmail: ''}
        }
    ],
    [ChannelType.LARK]: [
        {
            key: 'empty',
            config: {appId: '', appSecret: '', baseUrl: 'https://open.feishu.cn'}
        }
    ]
};

export function getDefaultPreset(type: ChannelType): ChannelConfigPreset | undefined {
    return MESSAGE_CHANNEL_PRESETS[type]?.[0];
}

export function serializePreset(preset: ChannelConfigPreset): string {
    return JSON.stringify(preset.config, null, 2);
}

export function isEmptyConfig(raw: string | undefined | null): boolean {
    if (raw == null) return true;
    const trimmed = raw.trim();
    return trimmed === '' || trimmed === '{}';
}
