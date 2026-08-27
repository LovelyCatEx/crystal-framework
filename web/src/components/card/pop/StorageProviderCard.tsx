import {Card, Descriptions, Spin, Tag} from "antd";
import type {StorageProvider} from "@/types/resource/storage-provider.types.ts";
import {useSWRComposition} from "@/compositions/use-swr.ts";
import {StorageProviderManagerController} from "@/api/resource/storage-provider.api.ts";
import {useStorageProviderTypes} from "@/compositions/use-storage-provider-types.ts";
import {CopyableToolTip} from "../../CopyableToolTip.tsx";
import {useTranslation} from "react-i18next";

interface StorageProviderCardProps {
    providerId: string;
}

const BUILTIN_TYPE_COLORS: Record<number, string> = {
    0: "default",
    1: "orange",
    2: "blue",
    3: "purple",
};

export function StorageProviderCard({ providerId }: StorageProviderCardProps) {
    const { t } = useTranslation();
    const { getLabel } = useStorageProviderTypes();
    const { data: provider, isLoading } = useSWRComposition<StorageProvider | null>(
        `storage-provider-card-${providerId}`,
        async () => {
            return await StorageProviderManagerController.getById(providerId);
        }
    );

    if (isLoading) {
        return (
            <Card size="small" className="w-64">
                <div className="flex justify-center py-4">
                    <Spin size="small" />
                </div>
            </Card>
        );
    }

    if (!provider) {
        return (
            <Card size="small" className="w-64">
                <div className="text-center py-4 text-gray-400">
                    {t('components.popCard.storageProvider.notFound')}
                </div>
            </Card>
        );
    }

    return (
        <Card
            size="small"
            className="w-72"
            title={
                <div className="flex items-center gap-2">
                    <CopyableToolTip title={provider.name}>
                        <span className="font-bold">{provider.name}</span>
                    </CopyableToolTip>
                </div>
            }
        >
            <Descriptions column={1} size="small" className="text-xs">
                <Descriptions.Item label="ID">
                    <CopyableToolTip title={provider.id}>
                        <Tag color="blue" className="text-xs">{provider.id}</Tag>
                    </CopyableToolTip>
                </Descriptions.Item>
                <Descriptions.Item label={t('components.popCard.storageProvider.type')}>
                    <Tag color={BUILTIN_TYPE_COLORS[provider.type] ?? 'default'} className="text-xs">
                        {getLabel(provider.type)}
                    </Tag>
                </Descriptions.Item>
                <Descriptions.Item label={t('components.popCard.storageProvider.description')}>
                    <span className="text-gray-600">
                        {provider.description ?? "-"}
                    </span>
                </Descriptions.Item>
                <Descriptions.Item label="Base URL">
                    <CopyableToolTip title={provider.baseUrl}>
                        <span className="text-xs font-mono text-gray-500 truncate max-w-[200px] block">
                            {provider.baseUrl}
                        </span>
                    </CopyableToolTip>
                </Descriptions.Item>
                <Descriptions.Item label={t('components.popCard.storageProvider.status')}>
                    <Tag color={provider.active ? "green" : "red"} className="text-xs">
                        {provider.active ? t('components.popCard.storageProvider.enabled') : t('components.popCard.storageProvider.disabled')}
                    </Tag>
                </Descriptions.Item>
            </Descriptions>
        </Card>
    );
}
