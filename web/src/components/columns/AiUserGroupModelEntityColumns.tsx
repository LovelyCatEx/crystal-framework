import type {EntityTableColumns} from "../table/entity-table.types.ts";
import type {AiUserGroupModelEntity} from "@/api/ai/ai-user-group-model.api.ts";
import {useTranslation} from "react-i18next";
import {CopyableToolTip} from "../CopyableToolTip.tsx";
import {Space, Tag} from "antd";
import {AiModelChip} from "@/components/chip/AiModelChip.tsx";
import {AiProviderChip} from "@/components/chip/AiProviderChip.tsx";
import {useEffect, useState} from "react";
import {AiModelManagerController} from "@/api/ai/ai-model.api.ts";
import type {AiModelEntity} from "@/types/ai/ai.types.ts";

export function useAiUserGroupModelTableColumns(): EntityTableColumns<AiUserGroupModelEntity> {
    const { t } = useTranslation();

    return [
        {
            title: t('components.columns.aiUserGroupModel.recordId'),
            dataIndex: 'id',
            key: 'id',
            render: (_, row) => (
                <Space direction="vertical" size={0}>
                    <CopyableToolTip title={row.id}>
                        <span className="text-xs font-mono">{row.id}</span>
                    </CopyableToolTip>
                </Space>
            )
        },
        {
            title: t('components.columns.aiUserGroupModel.provider'),
            dataIndex: 'modelId',
            key: 'provider',
            render: (_, row) => {
                const ProviderDisplay = () => {
                    const [model, setModel] = useState<AiModelEntity | null>(null);
                    const [loading, setLoading] = useState(true);

                    useEffect(() => {
                        AiModelManagerController.getById(row.modelId)
                            .then((response) => {
                                setModel(response || null);
                            })
                            .catch(() => {
                                setModel(null);
                            })
                            .finally(() => {
                                setLoading(false);
                            });
                    }, []);

                    if (loading) return <span className="text-xs text-gray-400">...</span>;
                    if (!model) return <span className="text-xs text-gray-400">-</span>;

                    return <AiProviderChip providerId={model.providerId} />;
                };

                return <ProviderDisplay />;
            }
        },
        {
            title: t('components.columns.aiUserGroupModel.model'),
            dataIndex: 'modelId',
            key: 'modelId',
            render: (_, row) => (
                <AiModelChip modelId={row.modelId} />
            )
        },
        {
            title: t('components.columns.aiUserGroupModel.modelKey'),
            dataIndex: 'modelId',
            key: 'modelKey',
            render: (_, row) => {
                const ModelKeyDisplay = () => {
                    const [model, setModel] = useState<AiModelEntity | null>(null);
                    const [loading, setLoading] = useState(true);

                    useEffect(() => {
                        AiModelManagerController.getById(row.modelId)
                            .then((response) => {
                                setModel(response || null);
                            })
                            .catch(() => {
                                setModel(null);
                            })
                            .finally(() => {
                                setLoading(false);
                            });
                    }, []);

                    if (loading) return <span className="text-xs text-gray-400">...</span>;
                    if (!model) return <span className="text-xs text-gray-400">-</span>;

                    return (
                        <CopyableToolTip title={model.key}>
                            <span className="text-xs font-mono text-gray-600">{model.key}</span>
                        </CopyableToolTip>
                    );
                };

                return <ModelKeyDisplay />;
            }
        },
        {
            title: t('components.columns.aiUserGroupModel.modelStatus'),
            dataIndex: 'modelId',
            key: 'modelStatus',
            render: (_, row) => {
                const ModelStatusDisplay = () => {
                    const [model, setModel] = useState<AiModelEntity | null>(null);
                    const [loading, setLoading] = useState(true);

                    useEffect(() => {
                        AiModelManagerController.getById(row.modelId)
                            .then((response) => {
                                setModel(response || null);
                            })
                            .catch(() => {
                                setModel(null);
                            })
                            .finally(() => {
                                setLoading(false);
                            });
                    }, []);

                    if (loading) return <span className="text-xs text-gray-400">...</span>;
                    if (!model) return <span className="text-xs text-gray-400">-</span>;

                    return (
                        <Tag color={model.enabled ? 'green' : 'red'}>
                            {model.enabled ? t('components.columns.aiUserGroupModel.enabled') : t('components.columns.aiUserGroupModel.disabled')}
                        </Tag>
                    );
                };

                return <ModelStatusDisplay />;
            }
        }
    ];
}
