/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {doGet} from "../system-request.ts";
import type {UserOAuthAccountVO} from "@/types/user/user-oauth.types.ts";

export function getUserOAuthAccounts() {
    return doGet<UserOAuthAccountVO[]>('/api/user/oauth/accounts');
}