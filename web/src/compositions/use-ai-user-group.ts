/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {useSWRComposition} from "@/compositions/use-swr.ts";
import type {AiUserGroupEntity} from "@/types/ai/ai.types.ts";
import {AiUserGroupManagerController} from "@/api/ai/ai-user-group.api.ts";

export const useAiUserGroup = (groupId?: string | null) => {
    const {data: group, isLoading} = useSWRComposition<AiUserGroupEntity | null>(
        groupId ? ['ai-user-group', groupId] : undefined,
        () => AiUserGroupManagerController.getById(groupId!),
        // Shared config entity; failures are surfaced per-component as an "Unknown" tag.
        () => undefined
    );

    return {group, isLoading};
};
