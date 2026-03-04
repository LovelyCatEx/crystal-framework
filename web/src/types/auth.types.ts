export interface LoginResponse {
    token: string;
    expiresIn: number;
}

export interface OAuth2UserInfo {
    oauthAccountId: string;
    platform: string;
    avatar: string;
    nickname: string;
    identifier: string;
}

export type OAuth2LoginResponse = LoginResponse | OAuth2UserInfo;