import {del, get, patch, post, put} from "./request.ts";
import {getUserAuthentication} from "../utils/token.utils.ts";
import {message} from "antd";

export interface ApiResponse<T> {
    code: number;
    message: string;
    data: T | null;
}

export interface PageQuery {
    page: number;
    pageSize: number;
}

export interface PaginatedResponseData<T> {
    page: number,
    pageSize: number,
    total: number,
    totalPages: number,
    records: T[]
}

function handleApiResponse<T>(response: ApiResponse<T>) {
    if (response.code === 200) {
        return response;
    } else if (response.code === 401) {
        void message.warning('登录信息已过期');
        setTimeout(() => {
            window.location.pathname = '/auth/login';
        }, 500);
        throw response;
    } else if (response.code === 403) {
        void message.warning('你无权访问当前资源');
        throw response;
    } else {
        void message.error(response.message)
        // console.error("未知错误", response)
        throw response;
    }
}

function preProcessHeaders(type: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH', headers: object | null) {
    let preHeaders = headers || {};

    const authentication = getUserAuthentication();
    if (authentication) {
        preHeaders = {
            'Authorization': 'Bearer ' + authentication.token,
            ...preHeaders
        };
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
    const result = rawResult.data;
    console.log(`[G] ==> ${url}`, rawResult.data);
    return handleApiResponse(result);
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
    return handleApiResponse(result);
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
    return handleApiResponse(result);
}

export async function doPatch<T>(url: string, body: object = {}, headers: object = {}): Promise<ApiResponse<T>> {
    const rawResult = await patch<ApiResponse<T>>(
        url,
        body,
        preProcessHeaders('PATCH', headers)
    );
    const result = rawResult.data;
    return handleApiResponse(result);
}