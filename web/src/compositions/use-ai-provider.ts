/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {useSWRComposition} from "@/compositions/use-swr.ts";
import type {AiProviderEntity} from "@/types/ai/ai.types.ts";
import {AiProviderManagerController} from "@/api/ai/ai-provider.api.ts";

export const useAiProvider = (providerId?: string | null) => {
    const {data: provider, isLoading} = useSWRComposition<AiProviderEntity | null>(
        providerId ? ['ai-provider', providerId] : undefined,
        () => AiProviderManagerController.getById(providerId!),
        // Shared config entity; failures are surfaced per-component as an "Unknown" tag.
        () => undefined
    );

    return {provider, isLoading};
};
