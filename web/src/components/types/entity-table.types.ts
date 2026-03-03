import React, {type JSX} from "react";
import type {BaseEntity} from "../../types/BaseEntity.ts";

export type EntityTableColumns<ENTITY extends BaseEntity, COLUMN_DATA_TYPE = unknown> = EntityTableColumn<ENTITY, COLUMN_DATA_TYPE>[]

export interface EntityTableColumn<ENTITY extends BaseEntity, COLUMN_DATA_TYPE> {
    title: string;
    dataIndex: string;
    key: string;
    fixed?: 'start' | 'end' | 'left' | 'right' | boolean;
    width?: number;
    render: (columnData: COLUMN_DATA_TYPE, row: ENTITY) => React.ReactNode | JSX.Element;
}