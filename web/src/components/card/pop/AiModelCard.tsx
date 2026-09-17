/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {Card, Descriptions, Spin, Tag} from "antd";
import {CopyableToolTip} from "../../CopyableToolTip.tsx";
import {useAiModel} from "@/compositions/use-ai-model.ts";
import {CurrencyAmountCodeDisplay} from "@/components/economy/CurrencyAmountCodeDisplay.tsx";
import {useTranslation} from "react-i18next";

interface AiModelCardProps {
    modelId?: string | null;
}

export function AiModelCard({ modelId }: AiModelCardProps) {
    const { t } = useTranslation();
    const {model, isLoading} = useAiModel(modelId);

    if (isLoading) {
        return (
            <Card size="small" className="w-80">
                <div className="flex justify-center py-4">
                    <Spin size="small" />
                </div>
            </Card>
        );
    }

    if (!model) {
        return (
            <Card size="small" className="w-80">
                <div className="text-center py-4 text-gray-400">
                    {t('components.popCard.aiModel.notFound')}
                </div>
            </Card>
        );
    }

    return (
        <Card
            size="small"
            className="w-80"
            title={
                <div className="flex flex-col gap-1 pt-2 pb-2">
                    <div className="inline-block">
                        <CopyableToolTip title={model.displayName}>
                            <span className="font-bold text-base">{model.displayName}</span>
                        </CopyableToolTip>
                    </div>
                    <div className="inline-block">
                        <CopyableToolTip title={model.key}>
                            <span className="text-xs text-gray-400">{model.key}</span>
                        </CopyableToolTip>
                    </div>
                </div>
            }
        >
            <Descriptions column={1} size="small" className="text-xs">
                <Descriptions.Item label="ID">
                    <CopyableToolTip title={model.id}>
                        <Tag color="blue" className="text-xs">{model.id}</Tag>
                    </CopyableToolTip>
                </Descriptions.Item>
                {model.description && (
                    <Descriptions.Item label={t('components.popCard.aiModel.description')}>
                        <span className="text-gray-600">{model.description}</span>
                    </Descriptions.Item>
                )}
                <Descriptions.Item label={t('components.popCard.aiModel.contextWindow')}>
                    <Tag color="green">{model.contextWindowTokens} tokens</Tag>
                </Descriptions.Item>
                {model.maxOutputTokens && (
                    <Descriptions.Item label={t('components.popCard.aiModel.maxOutput')}>
                        <Tag color="orange">{model.maxOutputTokens} tokens</Tag>
                    </Descriptions.Item>
                )}
                <Descriptions.Item label={t('components.popCard.aiModel.pricing')}>
                    <div className="flex flex-col gap-1 text-xs font-mono">
                        <div className="flex gap-2">
                            <span className="w-16">{t('components.popCard.aiModel.input')}</span>
                            <span><CurrencyAmountCodeDisplay currencyId={model.currencyId} amount={model.inputPricePerMillion} />/M</span>
                        </div>
                        <div className="flex gap-2">
                            <span className="w-16">{t('components.popCard.aiModel.cacheInput')}</span>
                            <span><CurrencyAmountCodeDisplay currencyId={model.currencyId} amount={model.cacheReadPricePerMillion} />/M</span>
                        </div>
                        <div className="flex gap-2">
                            <span className="w-16">{t('components.popCard.aiModel.output')}</span>
                            <span><CurrencyAmountCodeDisplay currencyId={model.currencyId} amount={model.outputPricePerMillion} />/M</span>
                        </div>
                        <div className="flex gap-2">
                            <span className="w-16">{t('components.popCard.aiModel.cacheWrite')}</span>
                            <span><CurrencyAmountCodeDisplay currencyId={model.currencyId} amount={model.cacheWritePricePerMillion} />/M</span>
                        </div>
                    </div>
                </Descriptions.Item>
                <Descriptions.Item label={t('components.popCard.aiModel.status')}>
                    <Tag color={model.enabled ? 'green' : 'red'}>
                        {model.enabled ? t('components.popCard.aiModel.enabled') : t('components.popCard.aiModel.disabled')}
                    </Tag>
                </Descriptions.Item>
            </Descriptions>
        </Card>
    );
}
