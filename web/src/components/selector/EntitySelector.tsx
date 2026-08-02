import {Modal} from "antd";
import {useCallback, useEffect, useRef, useState} from "react";
import {useTranslation} from "react-i18next";
import {EntityTable, type EntityTableProps, type EntityTableRef} from "../table/EntityTable.tsx";
import type {BaseEntity} from "@/types/BaseEntity.ts";

interface EntitySelectorModalProps<ENTITY extends BaseEntity> {
    type: NonNullable<EntityTableProps<ENTITY>['tableSelection']>['type'];
    onChange?: (selected: ENTITY[]) => void;
    isRowDisabled?: (row: ENTITY) => boolean;
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
    const { t } = useTranslation();
    const {
        type,
        onChange,
        isRowDisabled,
        entityName,
        columns,
        query,
        visible = true,
        title = t('components.selector.entitySelector.title', { entityName }),
        cancelText = t('components.selector.entitySelector.cancelText'),
        okText = t('components.selector.entitySelector.okText'),
        width = 1000,
        onCancel,
        onOk
    } = props;

    const entityTableRef = useRef<EntityTableRef | null>(null);

    // The modal is mounted once and toggled via `visible`, so the inner EntityTable only fetches on
    // mount and never re-runs when `query` changes (e.g. a caller swaps scope while the modal is
    // closed). Re-fetch on every closed→open edge so the table always reflects the current query.
    const prevVisibleRef = useRef(visible);
    useEffect(() => {
        if (visible && !prevVisibleRef.current) {
            entityTableRef.current?.refreshData({resetPage: true});
        }
        prevVisibleRef.current = visible;
    }, [visible]);

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
                    onChange: handleSelectionChange,
                    isDisabled: isRowDisabled
                }}
            />
        </Modal>
    );
}
