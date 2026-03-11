import {Button, Space, Spin} from "antd";
import {type ReactNode, useEffect, useState} from "react";
import {EntitySelectorModal} from "./EntitySelector.tsx";
import type {BaseEntity} from "@/types/BaseEntity.ts";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import type {BaseManagerController} from "@/api/BaseManagerController.ts";

interface EntityIdSelectorProps<ENTITY extends BaseEntity> {
    value?: string | null;
    onChange?: (value: string | null) => void;
    isRowDisabled?: (row: ENTITY) => boolean;
    entityName: string;
    columns: EntityTableColumns<ENTITY>;
    controller: BaseManagerController<ENTITY, object>,
    displayRender: (entity: ENTITY) => string;
    placeholder?: string;
    icon?: ReactNode;
}

export function EntityIdSelector<ENTITY extends BaseEntity>({
    value,
    onChange,
    isRowDisabled,
    entityName,
    columns,
    controller,
    displayRender,
    placeholder = "选择",
    icon
}: EntityIdSelectorProps<ENTITY>) {
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedEntity, setSelectedEntity] = useState<ENTITY | null>(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (value) {
            setLoading(true);
            controller.getById(value)
                .then((entity) => {
                    if (entity) {
                        setSelectedEntity(entity);
                    }
                })
                .finally(() => {
                    setLoading(false);
                });
        } else {
            setSelectedEntity(null);
        }
    }, [value, controller]);

    const handleOpenModal = () => {
        setIsModalOpen(true);
    };

    const handleCancel = () => {
        setIsModalOpen(false);
    };

    const handleOk = (selected: ENTITY[]) => {
        if (selected.length > 0) {
            const entity = selected[0];
            setSelectedEntity(entity);
            onChange?.(entity.id);
        } else {
            setSelectedEntity(null);
            onChange?.(null);
        }
        setIsModalOpen(false);
    };

    const handleClear = () => {
        setSelectedEntity(null);
        onChange?.(null);
    };

    return (
        <>
            <Space>
                <Button className="h-10" onClick={handleOpenModal}>
                    {loading ? (
                        <Spin size="small" />
                    ) : selectedEntity ? (
                        <Space>
                            {icon}
                            <span>{displayRender(selectedEntity)}</span>
                        </Space>
                    ) : value ? (
                        <Space>
                            {icon}
                            <span>{entityName}ID: {value}</span>
                        </Space>
                    ) : (
                        placeholder
                    )}
                </Button>
                {(selectedEntity || value) && (
                    <Button type="link" danger onClick={handleClear}>
                        清除
                    </Button>
                )}
            </Space>

            <EntitySelectorModal
                type="radio"
                visible={isModalOpen}
                title={`选择${entityName}`}
                entityName={entityName}
                columns={columns}
                query={async (props) => (await controller.query(props)).data!}
                onCancel={handleCancel}
                onOk={handleOk}
                isRowDisabled={isRowDisabled}
            />
        </>
    );
}
