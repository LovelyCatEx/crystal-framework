import {Button, Space, Spin} from "antd";
import {type ReactNode, useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {EntitySelectorModal} from "./EntitySelector.tsx";
import type {BaseEntity} from "@/types/BaseEntity.ts";
import type {EntityTableColumns} from "../table/entity-table.types.ts";
import type {PaginatedResponseData} from "@/types/api.types.ts";

interface EntityIdsSelectorProps<ENTITY extends BaseEntity> {
    value?: string[] | null;
    onChange?: (value: string[]) => void;
    entityName: string;
    columns: EntityTableColumns<ENTITY>;
    query: (params: { page: number; pageSize: number }) => Promise<PaginatedResponseData<ENTITY>>;
    getById: (id: string) => Promise<ENTITY | null>;
    renderItem: (entity: ENTITY) => ReactNode;
    placeholder?: ReactNode;
}

export function EntityIdsSelector<ENTITY extends BaseEntity>({
    value,
    onChange,
    entityName,
    columns,
    query,
    getById,
    renderItem,
    placeholder,
}: EntityIdsSelectorProps<ENTITY>) {
    const { t } = useTranslation();
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedEntities, setSelectedEntities] = useState<ENTITY[]>([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        const ids = value ?? [];
        if (ids.length === 0) {
            setSelectedEntities([]);
            return;
        }
        setLoading(true);
        Promise.all(ids.map((id) => getById(id)))
            .then((entities) => setSelectedEntities(entities.filter(Boolean) as ENTITY[]))
            .finally(() => setLoading(false));
    }, [value]);

    const handleOk = (selected: ENTITY[]) => {
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
            <div className="flex flex-col gap-2">
                <div className="flex flex-wrap gap-2">
                    {loading ? (
                        <Spin size="small" />
                    ) : (
                        selectedEntities.map((entity) => (
                            <div key={entity.id}>{renderItem(entity)}</div>
                        ))
                    )}
                </div>
                <Space>
                    <Button onClick={() => setIsModalOpen(true)}>
                        {placeholder ?? t('components.selector.entityIdSelector.placeholder')}
                    </Button>
                    {selectedEntities.length > 0 && (
                        <Button type="link" danger onClick={handleClear}>
                            {t('components.selector.entityIdSelector.clear')}
                        </Button>
                    )}
                </Space>
            </div>

            <EntitySelectorModal<ENTITY>
                type="checkbox"
                visible={isModalOpen}
                title={t('components.selector.entitySelector.title', { entityName })}
                entityName={entityName}
                columns={columns}
                query={async (props) => await query(props)}
                onCancel={() => setIsModalOpen(false)}
                onOk={handleOk}
            />
        </>
    );
}
