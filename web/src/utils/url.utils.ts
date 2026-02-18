export const getQueryString = (name: string, url = window.location.search) => {
    const searchParams = new URLSearchParams(url);
    return searchParams.get(name);
};

export const getAllQueryParams = (url = window.location.search) => {
    const searchParams = new URLSearchParams(url);
    const params: Record<string, string> = {};

    for (const [key, value] of searchParams.entries()) {
        params[key] = value;
    }

    return params;
};

export const removeQueryParam = (name: string, url = window.location.href) => {
    const urlObj = new URL(url);
    urlObj.searchParams.delete(name);
    return urlObj.toString();
};

export const getUrlHostname = (url: string) => {
    const match = url.match(/^(?:https?:\/\/)?(?:[^@\n]+@)?(?:www\.)?([^:/\n?]+)/);
    return match ? match[1] : '';
}