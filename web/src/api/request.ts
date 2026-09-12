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

/**
 * Streams a server-sent-event response over a raw `fetch`, invoking [onChunk] for every parsed
 * `data:` payload until the stream ends.
 *
 * Uses `fetch` rather than the axios instance above because axios exposes no streaming reader, and
 * `EventSource` cannot send custom headers — callers pass their own `Authorization` etc. through
 * [extraHeaders]. A non-2xx response is surfaced as an `Error` whose message comes from the body's
 * `message` field when present.
 */
export async function sseRequest<T>(
    url: string,
    extraHeaders: Record<string, string>,
    body: unknown,
    onChunk: (chunk: T) => void,
): Promise<void> {
    const response = await fetch(url, {
        method: "POST",
        headers: {"Content-Type": "application/json", ...extraHeaders},
        body: JSON.stringify(body),
    });

    if (!response.ok || !response.body) {
        let errorMessage = "Stream request failed";
        try {
            const data = await response.json();
            if (data && typeof data.message === "string") {
                errorMessage = data.message;
            }
        } catch {
            // Non-JSON error body; keep the fallback message.
        }
        throw new Error(errorMessage);
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = "";

    while (true) {
        const {done, value} = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, {stream: true});

        let separatorIndex = buffer.indexOf("\n\n");
        while (separatorIndex !== -1) {
            const eventBlock = buffer.slice(0, separatorIndex);
            buffer = buffer.slice(separatorIndex + 2);

            const dataLine = eventBlock.split("\n").find(line => line.startsWith("data:"));
            if (dataLine) {
                const data = dataLine.slice("data:".length).trim();
                if (data && data !== "[DONE]") {
                    try {
                        onChunk(JSON.parse(data) as T);
                    } catch {
                        // Ignore a malformed frame; the stream continues.
                    }
                }
            }

            separatorIndex = buffer.indexOf("\n\n");
        }
    }
}