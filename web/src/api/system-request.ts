import {del, get, patch, post, put} from "./request.ts";
import {getUserAuthentication} from "../utils/token.utils.ts";
import {message} from "antd";
import {menuPathLogin} from "@/router";
import i18n from "@/i18n";
import {HEADER_API_ENCRYPTION_KEY} from "@/utils/global-constants.ts";
import {RSA_PRIVATE_KEY_STORAGE_KEY, RSA_PUBLIC_KEY_STORAGE_KEY} from "@/ProtectedApp.tsx";
import {RSAUtils} from "@/utils/rsa-utils.ts";

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

export async function handleApiResponse<T>(response: ApiResponse<T>) {
    if (response.code === 200) {
        const rsaPrivKey = sessionStorage.getItem(RSA_PRIVATE_KEY_STORAGE_KEY) || undefined;
        if (rsaPrivKey && response.data) {
            const decryptedJson = await RSAUtils.decrypt(response.data as string, rsaPrivKey);
            const parsed = JSON.parse(decryptedJson);
            return {
                code: parsed.code ?? response.code,
                message: parsed.message ?? response.message,
                data: parsed.data,
            } as ApiResponse<T>;
        } else {
            void message.error("Could not decrypt response");
            throw response;
        }
    } else if (response.code === 401) {
        void message.warning(i18n.t('api.sessionExpired'));
        setTimeout(() => {
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
    let preHeaders = headers || {};

    const authentication = getUserAuthentication();
    const rsaPubKey = sessionStorage.getItem(RSA_PUBLIC_KEY_STORAGE_KEY) || undefined;

    if (authentication) {
        preHeaders = {
            'Authorization': 'Bearer ' + authentication.token,
            ...preHeaders
        };
    }

    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-expect-error
    preHeaders[`${HEADER_API_ENCRYPTION_KEY}`] = rsaPubKey;

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
    const result = rawResult.data;
    return handleApiResponse(result);
}

export async function doPut<T>(url: string, body: object = {}, headers: object = {}): Promise<ApiResponse<T>> {
    const rawResult = await put<ApiResponse<T>>(
        url,
        body,
        preProcessHeaders('PUT', headers)
    );
    const result = rawResult.data;
    return await handleApiResponse(result);
}

export async function doPatch<T>(url: string, body: object = {}, headers: object = {}): Promise<ApiResponse<T>> {
    const rawResult = await patch<ApiResponse<T>>(
        url,
        body,
        preProcessHeaders('PATCH', headers)
    );
    const result = rawResult.data;
    return await handleApiResponse(result);
}