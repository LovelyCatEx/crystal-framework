import {Button, Space, Spin, Tag} from "antd";
import {RobotOutlined} from "@ant-design/icons";
import {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {EntitySelectorModal} from "../EntitySelector.tsx";
import {useTenantMessageChannelTableColumns} from "@/components/columns/TenantMessageChannelEntityColumns.tsx";
import {TenantMessageChannelManagerController} from "@/api/tenant/tenant-message-channel.api.ts";
import type {TenantMessageChannel} from "@/types/tenant/tenant-message-channel.types.ts";

interface TenantMessageChannelIdsSelectorProps {
    tenantId: string;
    value?: string[] | null;
    onChange?: (value: string[]) => void;
}

export function TenantMessageChannelIdsSelector({ tenantId, value, onChange }: TenantMessageChannelIdsSelectorProps) {
    const { t } = useTranslation();
    const columns = useTenantMessageChannelTableColumns();
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedEntities, setSelectedEntities] = useState<TenantMessageChannel[]>([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const ids = value ?? [];
        if (ids.length === 0) {
            setSelectedEntities([]);
            return;
        }
        setLoading(true);
        Promise.all(ids.map((id) => TenantMessageChannelManagerController.getById(id, { tenantId })))
            .then((entities) => setSelectedEntities(entities.filter((it): it is TenantMessageChannel => it != null)))
            .finally(() => setLoading(false));
    }, [value, tenantId]);

    const handleOk = (selected: TenantMessageChannel[]) => {
        setSelectedEntities(selected);
        onChange?.(selected.map((it) => it.id));
        setIsModalOpen(false);
    };

    const handleClear = () => {
        setSelectedEntities([]);
        onChange?.([]);
    };

    return (
        <>
            <Space wrap>
                <Button className="h-10" onClick={() => setIsModalOpen(true)}>
                    {loading ? (
                        <Spin size="small"/>
                    ) : selectedEntities.length > 0 ? (
                        <Space size={4} wrap>
                            <RobotOutlined/>
                            {selectedEntities.map((channel) => (
                                <Tag key={channel.id} className="m-0">{channel.name}</Tag>
                            ))}
                        </Space>
                    ) : (
                        t('pages.tenantSettingsManager.channelSelectPlaceholder')
                    )}
                </Button>
                {selectedEntities.length > 0 && (
                    <Button type="link" danger onClick={handleClear}>
                        {t('components.selector.entityIdSelector.clear')}
                    </Button>
                )}
            </Space>

            <EntitySelectorModal<TenantMessageChannel>
                type="checkbox"
                visible={isModalOpen}
                title={t('components.selector.entitySelector.title', { entityName: t('entityNames.tenantMessageChannel') })}
                entityName={t('entityNames.tenantMessageChannel')}
                columns={columns}
                query={async (props) => (await TenantMessageChannelManagerController.query({
                    ...props,
                    tenantId,
                })).data!}
                onCancel={() => setIsModalOpen(false)}
                onOk={handleOk}
            />
        </>
    );
}
