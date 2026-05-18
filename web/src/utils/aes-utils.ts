/**
 * AES-GCM Encryption Utilities
 * Compatible with backend AES.kt (AES/GCM/NoPadding, 256-bit key, 12-byte IV)
 */
export class AESUtils {
    /**
     * Encrypt data using AES-GCM
     * @param data Plain text to encrypt
     * @param keyBase64 Base64-encoded AES key (256-bit)
     * @returns Base64-encoded ciphertext (IV + encrypted data)
     */
    static async encrypt(data: string, keyBase64: string): Promise<string> {
        const keyBytes = AESUtils.base64ToBuffer(keyBase64);
        const key = await crypto.subtle.importKey(
            'raw',
            keyBytes,
            { name: 'AES-GCM' },
            false,
            ['encrypt']
        );

        const iv = crypto.getRandomValues(new Uint8Array(12));
        const encoder = new TextEncoder();
        const dataBuffer = encoder.encode(data);

        const encrypted = await crypto.subtle.encrypt(
            { name: 'AES-GCM', iv, tagLength: 128 },
            key,
            dataBuffer
        );

        // Prepend IV to ciphertext (same format as backend)
        const result = new Uint8Array(iv.length + encrypted.byteLength);
        result.set(iv, 0);
        result.set(new Uint8Array(encrypted), iv.length);

        return AESUtils.bufferToBase64(result.buffer);
    }

    /**
     * Decrypt data using AES-GCM
     * @param encryptedBase64 Base64-encoded ciphertext (IV + encrypted data)
     * @param keyBase64 Base64-encoded AES key (256-bit)
     * @returns Decrypted plain text
     */
    static async decrypt(encryptedBase64: string, keyBase64: string): Promise<string> {
        const keyBytes = AESUtils.base64ToBuffer(keyBase64);
        const key = await crypto.subtle.importKey(
            'raw',
            keyBytes,
            { name: 'AES-GCM' },
            false,
            ['decrypt']
        );

        const encryptedData = new Uint8Array(AESUtils.base64ToBuffer(encryptedBase64));
        const iv = encryptedData.slice(0, 12);
        const ciphertext = encryptedData.slice(12);

        const decrypted = await crypto.subtle.decrypt(
            { name: 'AES-GCM', iv, tagLength: 128 },
            key,
            ciphertext
        );

        const decoder = new TextDecoder();
        return decoder.decode(decrypted);
    }

    private static bufferToBase64(buffer: ArrayBuffer): string {
        const bytes = new Uint8Array(buffer);
        let binary = '';
        for (let i = 0; i < bytes.byteLength; i++) {
            binary += String.fromCharCode(bytes[i]);
        }
        return btoa(binary);
    }

    private static base64ToBuffer(base64: string): ArrayBuffer {
        const binary = atob(base64);
        const bytes = new Uint8Array(binary.length);
        for (let i = 0; i < binary.length; i++) {
            bytes[i] = binary.charCodeAt(i);
        }
        return bytes.buffer;
    }
}
