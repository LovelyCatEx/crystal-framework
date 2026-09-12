import {Popover, Spin, Tag} from "antd";
import {AiProviderCard} from "@/components/card/pop/AiProviderCard.tsx";
import {useEffect, useState} from "react";
import {AiProviderManagerController} from "@/api/ai/ai-provider.api.ts";
import type {AiProviderEntity} from "@/types/ai/ai.types.ts";

interface AiProviderChipProps {
    providerId: string;
}

export function AiProviderChip({ providerId }: AiProviderChipProps) {
    const [provider, setProvider] = useState<AiProviderEntity | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        AiProviderManagerController.getById(providerId)
            .then((response) => {
                setProvider(response || null);
            })
            .catch((error) => {
                console.error('Failed to load provider:', error);
                setProvider(null);
            })
            .finally(() => {
                setIsLoading(false);
            });
    }, [providerId]);

    if (isLoading) {
        return <Spin size="small" />;
    }

    const chip = (
        <Tag
            color="purple"
            className="cursor-pointer transition hover:opacity-80"
            style={{
                borderRadius: '4px',
            }}
        >
            {provider?.name || providerId}
        </Tag>
    );

    return (
        <Popover content={<AiProviderCard providerId={providerId} />} trigger="click" placement="bottomLeft">
            {chip}
        </Popover>
    );
}
