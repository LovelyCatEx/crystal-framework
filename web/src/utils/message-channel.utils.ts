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
