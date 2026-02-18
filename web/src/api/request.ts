import axios, {type AxiosRequestConfig, type AxiosResponse} from "axios";

const axiosInstance = axios.create({
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
    },
});

export async function get<T>(url: string, query: object = {}, headers: object = {}): Promise<AxiosResponse<T>> {
    const config: AxiosRequestConfig = {
        method: 'GET',
        url,
        params: query,
        headers: headers,
    };

    return axiosInstance.request(config);
}

export async function post<T>(url: string, body: object = {}, headers: object = {}): Promise<AxiosResponse<T>> {
    const config: AxiosRequestConfig = {
        method: 'POST',
        url,
        data: body,
        headers: headers,
    };

    return axiosInstance.request(config);
}

export async function put<T>(url: string, body: object = {}, headers: object = {}): Promise<AxiosResponse<T>> {
    const config: AxiosRequestConfig = {
        method: 'PUT',
        url,
        data: body,
        headers: headers,
    };

    return axiosInstance.request(config);
}

export async function del<T>(url: string, query: object = {}, headers: object = {}): Promise<AxiosResponse<T>> {
    const config: AxiosRequestConfig = {
        method: 'DELETE',
        url,
        params: query,
        headers: headers,
    };

    return axiosInstance.request(config);
}

export async function patch<T>(url: string, body: object = {}, headers: object = {}): Promise<AxiosResponse<T>> {
    const config: AxiosRequestConfig = {
        method: 'PATCH',
        url,
        data: body,
        headers: headers,
    };

    return axiosInstance.request(config);
}