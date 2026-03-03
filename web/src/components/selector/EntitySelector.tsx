import {Modal} from "antd";
import {useCallback, useRef, useState} from "react";
import {EntityTable, type EntityTableProps, type EntityTableRef} from "../EntityTable.tsx";
import type {BaseEntity} from "../../types/BaseEntity.ts";

interface EntitySelectorModalProps<ENTITY extends BaseEntity> {
    type: NonNullable<EntityTableProps<ENTITY>['tableSelection']>['type'];
    onChange?: (selected: ENTITY[]) => void;
    entityName: EntityTableProps<ENTITY>['entityName'];
    columns: EntityTableProps<ENTITY>['columns'];
    query: EntityTableProps<ENTITY>['query'];
    visible?: boolean;
    title?: string;
    cancelText?: string;
    okText?: string;
    width?: string | number;
    onCancel?: () => void;
    onOk?: (selected: ENTITY[]) => void;
}

export function EntitySelectorModal<ENTITY extends BaseEntity>(props: EntitySelectorModalProps<ENTITY>) {
    const {
        type,
        onChange,
        entityName,
        columns,
        query,
        visible = true,
        title = `选择${props.entityName}`,
        cancelText,
        okText,
        width = 1000,
        onCancel,
        onOk
    } = props;

    const entityTableRef = useRef<EntityTableRef | null>(null);

    const [selectedEntities, setSelectedEntities] = useState<ENTITY[]>([]);

    const handleSelectionChange = useCallback((entities: ENTITY[]) => {
        setSelectedEntities(entities);
        onChange?.(entities);
    }, [onChange]);

    const handleOk = () => {
        onOk?.(selectedEntities);
    }
    const handleCancel = () => {
        onCancel?.();
    }

    return (
        <Modal
            title={title}
            open={visible}
            onCancel={handleCancel}
            onOk={handleOk}
            okText={okText}
            cancelText={cancelText}
            width={width}
            mask={{closable: false}}
        >
            <EntityTable
                ref={entityTableRef}
                entityName={entityName}
                columns={columns}
                query={query}
                tableSelection={{
                    type,
                    onChange: handleSelectionChange
                }}
            />
        </Modal>
    );
}