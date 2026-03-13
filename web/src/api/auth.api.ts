import type {LoginResponse, OAuth2LoginResponse} from "../types/auth.types.ts";
import {doGet, doPost} from "./system-request.ts";

export async function login(username: string, password: string, tenantId?: string) {
    return doPost<LoginResponse>('/api/user/login', { username: username, password: password, tenantId: tenantId });
}

export async function register(
    username: string,
    password: string,
    email: string,
    emailCode: string
) {
    return doPost(
        '/api/user/register',
        { username: username, password: password, email: email, emailCode: emailCode }
    );
}

export async function requestRegisterEmailCode(email: string) {
    return doPost('/api/user/requestRegisterEmailCode', { email: email });
}

export async function requestPasswordResetEmailCode(email: string) {
    return doPost('/api/user/requestPasswordResetEmailCode', { email: email });
}

export async function resetPassword(dto: ResetPasswordDTO) {
    return doPost(
        '/api/user/resetPassword',
        {...dto}
    );
}

export async function requestResetEmailAddressEmailCode(newEmail: string) {
    return doPost('/api/user/requestResetEmailAddressEmailCode', { newEmail: newEmail });
}

export async function resetEmail(dto: ResetEmailDTO) {
    return doPost(
        '/api/user/resetEmail',
        {...dto}
    );
}

export interface ResetPasswordDTO {
    email: string;
    emailCode: string;
    newPassword: string;
}

export interface ResetEmailDTO {
    emailCode: string;
    newEmail: string;
}

export async function loginByOAuth2Code(code: string, state: string) {
    return doGet<OAuth2LoginResponse>('/raw/login/oauth2/code/github', { code: code, state: state });
}

export interface BindOAuthAccountDTO {
    oauthAccountId: string;
    username?: string;
    password?: string;
}

export async function bindOAuthAccount(dto: BindOAuthAccountDTO) {
    return doPost<LoginResponse>('/api/user/oauth/bindOAuthAccount', { ...dto });
}

export interface UnbindOAuthAccountDTO {
    oauthAccountId: string;
}

export async function unbindOAuthAccount(dto: UnbindOAuthAccountDTO) {
    return doPost<unknown>('/api/user/oauth/unbind', { ...dto });
}

export interface RegisterFromOAuthAccountDTO {
    oauthAccountId: string;
    username: string;
    password: string;
    nickname: string;
}

export async function registerFromOAuthAccount(dto: RegisterFromOAuthAccountDTO) {
    return doPost<LoginResponse>('/api/user/oauth/registerFromOAuthAccount', { ...dto });
}

export interface SwitchTenantDTO {
    tenantId: string;
}

export async function switchTenant(dto: SwitchTenantDTO) {
    return doPost<LoginResponse>('/api/user/auth/switchTenant', dto);
}