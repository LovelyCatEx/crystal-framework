/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {useSWRComposition} from "@/compositions/use-swr.ts";
import type {AiModelEntity} from "@/types/ai/ai.types.ts";
import {AiModelManagerController} from "@/api/ai/ai-model.api.ts";

export const useAiModel = (modelId?: string | null) => {
    const {data: model, isLoading} = useSWRComposition<AiModelEntity | null>(
        modelId ? ['ai-model', modelId] : undefined,
        () => AiModelManagerController.getById(modelId!),
        // Shared config entity; failures are surfaced per-component as an "Unknown" tag.
        () => undefined
    );

    return {model, isLoading};
};
