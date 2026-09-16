/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {Popover, Spin, Tag} from "antd";
import {AiModelCard} from "@/components/card/pop/AiModelCard.tsx";
import {useAiModel} from "@/compositions/use-ai-model.ts";

interface AiModelChipProps {
    modelId: string;
}

export function AiModelChip({ modelId }: AiModelChipProps) {
    const {model, isLoading} = useAiModel(modelId);

    if (isLoading) {
        return <Spin size="small" />;
    }

    const chip = (
        <Tag
            color="blue"
            className="cursor-pointer transition hover:opacity-80"
            style={{
                borderRadius: '4px',
            }}
        >
            {model?.displayName || modelId} ({model?.modelName})
        </Tag>
    );

    return (
        <Popover content={<AiModelCard modelId={modelId} />} trigger="click" placement="bottomLeft">
            {chip}
        </Popover>
    );
}
