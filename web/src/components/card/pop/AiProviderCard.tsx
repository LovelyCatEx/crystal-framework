import {Card, Descriptions, Spin, Tag} from "antd";
import {CopyableToolTip} from "../../CopyableToolTip.tsx";
import {useEffect, useState} from "react";
import {AiProviderManagerController} from "@/api/ai/ai-provider.api.ts";
import type {AiProviderEntity} from "@/types/ai/ai.types.ts";
import {useTranslation} from "react-i18next";

interface AiProviderCardProps {
    providerId?: string | null;
}

export function AiProviderCard({ providerId }: AiProviderCardProps) {
    const { t } = useTranslation();
    const [provider, setProvider] = useState<AiProviderEntity | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        if (!providerId) {
            setIsLoading(false);
            return;
        }

        setIsLoading(true);
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
        return (
            <Card size="small" className="w-80">
                <div className="flex justify-center py-4">
                    <Spin size="small" />
                </div>
            </Card>
        );
    }

    if (!provider) {
        return (
            <Card size="small" className="w-80">
                <div className="text-center py-4 text-gray-400">
                    {t('components.popCard.aiProvider.notFound')}
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
                        <CopyableToolTip title={provider.name}>
                            <span className="font-bold text-base">{provider.name}</span>
                        </CopyableToolTip>
                    </div>
                    <div className="inline-block">
                        <CopyableToolTip title={provider.key}>
                            <span className="text-xs text-gray-400">{provider.key}</span>
                        </CopyableToolTip>
                    </div>
                </div>
            }
        >
            <Descriptions column={1} size="small" className="text-xs">
                <Descriptions.Item label="ID">
                    <CopyableToolTip title={provider.id}>
                        <Tag color="blue" className="text   -xs">{provider.id}</Tag>
                    </CopyableToolTip>
                </Descriptions.Item>
                {provider.description && (
                    <Descriptions.Item label={t('components.popCard.aiProvider.description')}>
                        {provider.description}
                    </Descriptions.Item>
                )}
                <Descriptions.Item label={t('components.popCard.aiProvider.baseUrl')}>
                    <CopyableToolTip title={provider.baseUrl}>
                        {provider.baseUrl}
                    </CopyableToolTip>
                </Descriptions.Item>
                <Descriptions.Item label={t('components.popCard.aiProvider.status')}>
                    <Tag color={provider.enabled ? 'green' : 'red'}>
                        {provider.enabled ? t('components.popCard.aiProvider.enabled') : t('components.popCard.aiProvider.disabled')}
                    </Tag>
                </Descriptions.Item>
            </Descriptions>
        </Card>
    );
}
