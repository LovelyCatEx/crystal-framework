import type {LoginResponse} from "../types/auth.types.ts";
import {doPost} from "./system-request.ts";

export async function login(username: string, password: string) {
    return doPost<LoginResponse>('/api/user/login', { username: username, password: password });
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

export async function requestResetEmailAddressEmailCode() {
    return doPost('/api/user/requestResetEmailAddressEmailCode', {});
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