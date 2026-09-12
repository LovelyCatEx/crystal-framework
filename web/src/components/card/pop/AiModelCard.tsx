import {Card, Descriptions, Spin, Tag} from "antd";
import {CopyableToolTip} from "../../CopyableToolTip.tsx";
import {useEffect, useState} from "react";
import {AiModelManagerController} from "@/api/ai/ai-model.api.ts";
import type {AiModelEntity} from "@/types/ai/ai.types.ts";
import {useTranslation} from "react-i18next";

interface AiModelCardProps {
    modelId?: string | null;
}

export function AiModelCard({ modelId }: AiModelCardProps) {
    const { t } = useTranslation();
    const [model, setModel] = useState<AiModelEntity | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        if (!modelId) {
            setIsLoading(false);
            return;
        }

        setIsLoading(true);
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
                            <span>{parseFloat(model.inputPricePerMillion || '0').toFixed(2)} {model.currency}/M</span>
                        </div>
                        <div className="flex gap-2">
                            <span className="w-16">{t('components.popCard.aiModel.cacheInput')}</span>
                            <span>{parseFloat(model.cacheReadPricePerMillion || '0').toFixed(2)} {model.currency}/M</span>
                        </div>
                        <div className="flex gap-2">
                            <span className="w-16">{t('components.popCard.aiModel.output')}</span>
                            <span>{parseFloat(model.outputPricePerMillion || '0').toFixed(2)} {model.currency}/M</span>
                        </div>
                        <div className="flex gap-2">
                            <span className="w-16">{t('components.popCard.aiModel.cacheWrite')}</span>
                            <span>{parseFloat(model.cacheWritePricePerMillion || '0').toFixed(2)} {model.currency}/M</span>
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
