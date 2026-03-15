import {Button, Space, Spin} from "antd";
import {
    type ForwardedRef,
    forwardRef,
    type ReactNode,
    useEffect,
    useImperativeHandle,
    useState
} from "react";
import {EntitySelectorModal} from "./EntitySelector.tsx";
import type {BaseEntity} from "@/types/BaseEntity.ts";
import type {EntityTableColumns} from "../types/entity-table.types.ts";
import type {BaseManagerController} from "@/api/BaseManagerController.ts";

interface EntityIdSelectorProps<ENTITY extends BaseEntity> {
    value?: string | null;
    onChange?: (value: string | null) => void;
    onEntityChange?: (entity: ENTITY | null) => void;
    isRowDisabled?: (row: ENTITY) => boolean;
    entityName: string;
    columns: EntityTableColumns<ENTITY>;
    controller: BaseManagerController<ENTITY, object>,
    displayRender: (entity: ENTITY) => string;
    placeholder?: string;
    icon?: ReactNode;
    additionalQueryParams?: (props: Parameters<BaseManagerController<ENTITY, object>["query"]>[0]) => Record<string, string>
}

export interface EntityIdSelectorRef {
    openModal: () => void;
}

export type EntityIdSelectorReturnType =
    <ENTITY extends BaseEntity>(
        props: EntityIdSelectorProps<ENTITY> & React.RefAttributes<EntityIdSelectorRef>
    ) => ReactNode;

export const EntityIdSelector = forwardRef(EntityIdSelectorInner) as EntityIdSelectorReturnType;

function EntityIdSelectorInner<ENTITY extends BaseEntity>(
    {
        value,
        onChange,
        onEntityChange,
        isRowDisabled,
        entityName,
        columns,
        controller,
        displayRender,
        placeholder = "选择",
        icon,
        additionalQueryParams
    }: EntityIdSelectorProps<ENTITY>,
    ref: ForwardedRef<EntityIdSelectorRef>
) {
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedEntity, setSelectedEntity] = useState<ENTITY | null>(null);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (value) {
            setLoading(true);
            // As the implementation of getById is using query() too
            // So the additional parameters could be shared here
            controller.getById(value, (additionalQueryParams ? additionalQueryParams({ page: 1, pageSize: 1, id: value }) : {}))
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
            onEntityChange?.(entity);
        } else {
            setSelectedEntity(null);
            onChange?.(null);
            onEntityChange?.(null);
        }
        setIsModalOpen(false);
    };

    const handleClear = () => {
        setSelectedEntity(null);
        onChange?.(null);
        onEntityChange?.(null);
    };

    useImperativeHandle(ref, () => {
        return {
            openModal: () => {
                handleOpenModal();
            }
        };
    });

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
                query={async (props) => (await controller.query({
                    ...props,
                    ...(additionalQueryParams ? additionalQueryParams(props) : {})
                })).data!}
                onCancel={handleCancel}
                onOk={handleOk}
                isRowDisabled={isRowDisabled}
            />
        </>
    );
}
