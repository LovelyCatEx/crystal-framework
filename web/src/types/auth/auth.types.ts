/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

export interface LoginResponse {
    token: string;
    expiresIn: number;
    oauthAccountId?: string;
}

export interface OAuth2UserInfo {
    oauthBindToken: string;
    platform: string;
    avatar: string;
    nickname: string;
    identifier: string;
}

export type OAuth2LoginResponse = LoginResponse | OAuth2UserInfo;