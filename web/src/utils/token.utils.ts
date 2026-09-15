/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

export function setUserAuthentication(token: string, expiresIn: number) {
    localStorage.setItem('access_token', token);
    localStorage.setItem('expires', String(new Date().getTime() + expiresIn));
}

export function getUserAuthentication(): {
    token: string;
    expiresIn: number;
    expired: boolean;
} | null {
    const token = localStorage.getItem('access_token');
    if (!token) {
        return null;
    }

    const expires = Number(localStorage.getItem('expires'));

    return {
        token: token,
        expiresIn: Number(localStorage.getItem('expires')),
        expired: new Date().getTime() > expires,
    };
}

export function clearUserAuthentication() {
    localStorage.removeItem('access_token');
    localStorage.removeItem('expires');
}