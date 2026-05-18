import { OAuthPlatform } from "../types/oauth-account.types";

export const PLATFORM_REGISTRATION_ID_MAP: Record<OAuthPlatform, string> = {
    [OAuthPlatform.GITHUB]: "github",
    [OAuthPlatform.GOOGLE]: "google",
    [OAuthPlatform.OICQ]: "oicq"
};

export const HEADER_API_ENCRYPTION_KEY = 'X-Secure-Key';
export const HEADER_API_AES_KEY = 'X-Secure-AES-Key';
export const RSA_PUBLIC_KEY_STORAGE_KEY = "RSA_PUBLIC_KEY";
export const RSA_PRIVATE_KEY_STORAGE_KEY = "RSA_PRIVATE_KEY";
export const AES_KEY_STORAGE_KEY = 'AES_SECRET_KEY';
export const ENCRYPTED_DATA_PREFIX_IDENTIFIER = '$2a$12$';