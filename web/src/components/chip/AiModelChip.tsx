import {Popover, Spin, Tag} from "antd";
import {AiModelCard} from "@/components/card/pop/AiModelCard.tsx";
import {useEffect, useState} from "react";
import {AiModelManagerController} from "@/api/ai/ai-model.api.ts";
import type {AiModelEntity} from "@/types/ai/ai.types.ts";

interface AiModelChipProps {
    modelId: string;
}

export function AiModelChip({ modelId }: AiModelChipProps) {
    const [model, setModel] = useState<AiModelEntity | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        AiModelManagerController.getById(modelId)
            .then((response) => {
                setModel(response || null);
            })
            .catch((error) => {
                console.error('Failed to load model:', error);
                setModel(null);
            })
            .finally(() => {
                setIsLoading(false);
            });
    }, [modelId]);

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
