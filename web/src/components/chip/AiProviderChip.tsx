/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {Popover, Spin, Tag} from "antd";
import {AiProviderCard} from "@/components/card/pop/AiProviderCard.tsx";
import {useAiProvider} from "@/compositions/use-ai-provider.ts";

interface AiProviderChipProps {
    providerId: string;
    variant?: 'chip' | 'text';
}

export function AiProviderChip({ providerId, variant = 'chip' }: AiProviderChipProps) {
    const {provider, isLoading} = useAiProvider(providerId);

    if (isLoading) {
        return <Spin size="small" />;
    }

    const label = provider?.name || providerId;

    const trigger = variant === 'text'
        ? <span className="cursor-pointer">{label}</span>
        : (
            <Tag
                color="purple"
                className="cursor-pointer transition hover:opacity-80"
                style={{
                    borderRadius: '4px',
                }}
            >
                {label}
            </Tag>
        );

    return (
        <Popover content={<AiProviderCard providerId={providerId} />} trigger="click" placement="bottomLeft">
            {trigger}
        </Popover>
    );
}
