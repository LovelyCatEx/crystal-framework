/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

export interface UserOAuthAccountVO {
    id: string;
    platformId: number;
    nickname: string | null;
    avatar: string | null;
}