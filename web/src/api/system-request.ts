import {del, get, patch, post, put} from "./request.ts";
import {clearUserAuthentication, getUserAuthentication} from "../utils/token.utils.ts";
import {message} from "antd";
import {menuPathLogin} from "@/router/paths.ts";
import i18n from "@/i18n";
import {
    AES_KEY_STORAGE_KEY,
    ENCRYPTED_DATA_PREFIX_IDENTIFIER,
    HEADER_API_AES_KEY,
    HEADER_API_ENCRYPTION_KEY,
    RSA_PRIVATE_KEY_STORAGE_KEY,
    RSA_PUBLIC_KEY_STORAGE_KEY
} from "@/global/constants.ts";
import {RSAUtils} from "@/utils/rsa-utils.ts";
import {AESUtils} from "@/utils/aes-utils.ts";
import type {AxiosResponse} from "axios";

export interface ApiResponse<T> {
    code: number;
    message: string;
    data: T | null;
}

export function emptyApiResponse<T>() {
    return {
        code: 200,
        message: '',
        data: null,
    } as ApiResponse<T>;
}

export function emptyApiResponseAsync<T>() {
    return Promise.resolve({
        code: 200,
        message: '',
        data: null,
    } as ApiResponse<T>);
}

/**
 * Extract AES key from response header if present (first request key exchange)
 */
async function extractAesKeyFromResponse(rawResult: AxiosResponse) {
    const encryptedAesKey = rawResult.headers[HEADER_API_AES_KEY.toLowerCase()];
    if (encryptedAesKey) {
        const rsaPrivKey = sessionStorage.getItem(RSA_PRIVATE_KEY_STORAGE_KEY);
        if (rsaPrivKey) {
            const aesKey = await RSAUtils.decrypt(encryptedAesKey, rsaPrivKey);
            sessionStorage.setItem(AES_KEY_STORAGE_KEY, aesKey);
        }
    }
}

export async function handleApiResponse<T>(response: ApiResponse<T>) {
    if (response.code === 200) {
        const aesKey = sessionStorage.getItem(AES_KEY_STORAGE_KEY);
        if (aesKey && response.data && typeof response.data === 'string' && response.data.startsWith(ENCRYPTED_DATA_PREFIX_IDENTIFIER)) {
            try {
                const decryptedJson = await AESUtils.decrypt(response.data.substring(ENCRYPTED_DATA_PREFIX_IDENTIFIER.length), aesKey);
                const parsed = JSON.parse(decryptedJson);
                return {
                    code: parsed.code ?? response.code,
                    message: parsed.message ?? response.message,
                    data: parsed.data,
                } as ApiResponse<T>;
            } catch {
                void message.error("Could not decrypt response");
                throw response;
            }
        } else if (!aesKey && response.data && typeof response.data === 'string' && response.data.startsWith(ENCRYPTED_DATA_PREFIX_IDENTIFIER)) {
            void message.error("Could not decrypt response");
            throw response;
        } else {
            // Data is not encrypted (or null)
            return response;
        }
    } else if (response.code === 401) {
        void message.warning(i18n.t('api.sessionExpired'));
        setTimeout(() => {
            // Fix: infinite redirect to login page
            clearUserAuthentication();

            const url = new URL(window.location.origin + menuPathLogin);
            url.searchParams.set('redirectTo', window.location.href);
            window.location.href = url.toString();
        }, 500);
        throw response;
    } else if (response.code === 403) {
        void message.warning(response.message || i18n.t('api.forbidden'));
        throw response;
    } else {
        void message.error(response.message || i18n.t('api.unknownError'))
        throw response;
    }
}

function preProcessHeaders(type: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH', headers: object | null) {
    let preHeaders: Record<string, string> = (headers || {}) as Record<string, string>;

    const authentication = getUserAuthentication();

    if (authentication) {
        preHeaders = {
            'Authorization': 'Bearer ' + authentication.token,
            ...preHeaders
        };
    }

    // Always send RSA public key (for AES key exchange on first request)
    const rsaPubKey = sessionStorage.getItem(RSA_PUBLIC_KEY_STORAGE_KEY);
    if (rsaPubKey) {
        preHeaders[HEADER_API_ENCRYPTION_KEY] = rsaPubKey;
    }

    if (type === 'GET') {
        return preHeaders;
    } else if (type === 'POST') {
        return {'Content-Type': 'application/x-www-form-urlencoded', ...preHeaders};
    } else if (type === 'DELETE') {
        return preHeaders;
    } else if (type === 'PUT') {
        return {'Content-Type': 'application/x-www-form-urlencoded', ...preHeaders};
    } else if (type === 'PATCH') {
        return {'Content-Type': 'application/x-www-form-urlencoded', ...preHeaders};
    } else {
        return preHeaders;
    }
}

export async function doGet<T>(url: string, query: object = {}, headers: object = {}): Promise<ApiResponse<T>> {
    console.log(`[G] <== ${url}`, query);
    const rawResult = await get<ApiResponse<T>>(
        url,
        query,
        preProcessHeaders('GET', headers)
    );
    await extractAesKeyFromResponse(rawResult);
    const result = rawResult.data;
    console.log(`[G] ==> ${url}`, rawResult.data);
    return await handleApiResponse(result);
}

export async function doPost<T>(url: string, body: object = {}, headers: object = {}): Promise<ApiResponse<T>> {
    console.log(`[P] <== ${url}`, body);
    const rawResult = await post<ApiResponse<T>>(
        url,
        body,
        preProcessHeaders('POST', headers)
    );
    await extractAesKeyFromResponse(rawResult);
    console.log(`[P] ==> ${url}`, rawResult.data);
    const result = rawResult.data;
    return await handleApiResponse(result);
}

export async function doDelete<T>(url: string, query: object = {}, headers: object = {}): Promise<ApiResponse<T>> {
    const rawResult = await del<ApiResponse<T>>(
        url,
        query,
        preProcessHeaders('DELETE', headers)
    );
    await extractAesKeyFromResponse(rawResult);
    const result = rawResult.data;
    return handleApiResponse(result);
}

export async function doPut<T>(url: string, body: object = {}, headers: object = {}): Promise<ApiResponse<T>> {
    const rawResult = await put<ApiResponse<T>>(
        url,
        body,
        preProcessHeaders('PUT', headers)
    );
    await extractAesKeyFromResponse(rawResult);
    const result = rawResult.data;
    return await handleApiResponse(result);
}

export async function doPatch<T>(url: string, body: object = {}, headers: object = {}): Promise<ApiResponse<T>> {
    const rawResult = await patch<ApiResponse<T>>(
        url,
        body,
        preProcessHeaders('PATCH', headers)
    );
    await extractAesKeyFromResponse(rawResult);
    const result = rawResult.data;
    return await handleApiResponse(result);
}
