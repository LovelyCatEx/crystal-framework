import {doGet} from "./system-request.ts";
import type {UserOAuthAccountVO} from "../types/user-oauth.types.ts";

export function getUserOAuthAccounts() {
    return doGet<UserOAuthAccountVO[]>('/api/user/oauth/accounts');
}