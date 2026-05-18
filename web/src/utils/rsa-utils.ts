// rsa-utils.ts

/**
 * RSA Key Pair Generation and Encryption Utilities
 */
export class RSAUtils {
    /**
     * Generate RSA key pair
     * @param modulusLength Key length, recommended 2048 or 4096
     * @returns Key pair object with PEM-formatted public and private keys
     */
    static async generateKeyPair(modulusLength: number = 2048): Promise<{
        publicKey: string;
        privateKey: string;
    }> {
        try {
            // Generate key pair
            const keyPair = await crypto.subtle.generateKey(
                {
                    name: 'RSA-OAEP',
                    modulusLength: modulusLength,
                    publicExponent: new Uint8Array([1, 0, 1]), // 65537
                    hash: 'SHA-256',
                },
                true, // Extractable
                ['encrypt', 'decrypt'] // Key usages
            );

            // Export public key to PEM format
            const publicKeyBuffer = await crypto.subtle.exportKey('spki', keyPair.publicKey);
            const publicKey = RSAUtils.bufferToPem(publicKeyBuffer, 'PUBLIC KEY');

            // Export private key to PEM format
            const privateKeyBuffer = await crypto.subtle.exportKey('pkcs8', keyPair.privateKey);
            const privateKey = RSAUtils.bufferToPem(privateKeyBuffer, 'PRIVATE KEY');

            return { publicKey, privateKey };
        } catch (error) {
            console.error('Failed to generate RSA key pair:', error);
            throw error;
        }
    }

    /**
     * Encrypt data using public key
     * @param data Data to encrypt
     * @param publicKeyPem Public key in PEM format
     * @returns Base64-encoded encrypted data
     */
    static async encrypt(data: string, publicKeyPem: string): Promise<string> {
        try {
            const publicKey = await RSAUtils.importPublicKey(publicKeyPem);
            const encoder = new TextEncoder();
            const dataBuffer = encoder.encode(data);

            const encryptedBuffer = await crypto.subtle.encrypt(
                {
                    name: 'RSA-OAEP',
                },
                publicKey,
                dataBuffer
            );

            return RSAUtils.bufferToBase64(encryptedBuffer);
        } catch (error) {
            console.error('Encryption failed:', error);
            throw error;
        }
    }

    /**
     * Decrypt data using private key
     * @param encryptedBase64 Base64-encoded encrypted data
     * @param privateKeyPem Private key in PEM format
     * @returns Decrypted original string
     */
    static async decrypt(encryptedBase64: string, privateKeyPem: string): Promise<string> {
        try {
            const privateKey = await RSAUtils.importPrivateKey(privateKeyPem);
            const encryptedBuffer = RSAUtils.base64ToBuffer(encryptedBase64);

            const decryptedBuffer = await crypto.subtle.decrypt(
                {
                    name: 'RSA-OAEP',
                },
                privateKey,
                encryptedBuffer
            );

            const decoder = new TextDecoder();
            return decoder.decode(decryptedBuffer);
        } catch (error) {
            console.error('Decryption failed:', error);
            throw error;
        }
    }

    /**
     * Import public key from PEM format
     */
    private static async importPublicKey(pem: string): Promise<CryptoKey> {
        const base64 = RSAUtils.pemToBase64(pem);
        const binaryData = RSAUtils.base64ToBuffer(base64);

        return await crypto.subtle.importKey(
            'spki',
            binaryData,
            {
                name: 'RSA-OAEP',
                hash: 'SHA-256',
            },
            false,
            ['encrypt']
        );
    }

    /**
     * Import private key from PEM format
     */
    private static async importPrivateKey(pem: string): Promise<CryptoKey> {
        const base64 = RSAUtils.pemToBase64(pem);
        const binaryData = RSAUtils.base64ToBuffer(base64);

        return await crypto.subtle.importKey(
            'pkcs8',
            binaryData,
            {
                name: 'RSA-OAEP',
                hash: 'SHA-256',
            },
            false,
            ['decrypt']
        );
    }

    /**
     * Convert ArrayBuffer to Base64 string
     */
    private static bufferToBase64(buffer: ArrayBuffer): string {
        const bytes = new Uint8Array(buffer);
        let binary = '';
        for (let i = 0; i < bytes.byteLength; i++) {
            binary += String.fromCharCode(bytes[i]);
        }
        return btoa(binary);
    }

    /**
     * Convert Base64 string to ArrayBuffer
     */
    private static base64ToBuffer(base64: string): ArrayBuffer {
        const binary = atob(base64);
        const bytes = new Uint8Array(binary.length);
        for (let i = 0; i < binary.length; i++) {
            bytes[i] = binary.charCodeAt(i);
        }
        return bytes.buffer;
    }

    /**
     * Convert ArrayBuffer to PEM format
     */
    private static bufferToPem(buffer: ArrayBuffer, _: string): string {
        const base64 = RSAUtils.bufferToBase64(buffer);
        // return `-----BEGIN ${type}-----\n${lines.join('\n')}\n-----END ${type}-----\n`;
        return base64;
    }

    /**
     * Extract Base64 from PEM format
     */
    private static pemToBase64(pem: string): string {
        return pem
            .replace(/-----BEGIN .*-----/g, '')
            .replace(/-----END .*-----/g, '')
            .replace(/\s/g, '');
    }
}