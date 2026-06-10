import type {BaseEntity} from "@/types/BaseEntity.ts";

export interface TenantDictItem extends BaseEntity {
    typeId: string;
    itemCode: string;
    itemValue: string;
    parentId: string | null;
    sortOrder: number;
    isDefault: boolean;
    status: number;
}

export interface TenantDictItemTreeNode {
    id: string;
    itemCode: string;
    itemValue: string;
    parentId: string | null;
    sortOrder: number;
    isDefault: boolean;
    status: number;
    createdTime: string;
    modifiedTime: string;
    children: TenantDictItemTreeNode[];
}

export enum DictItemStatus {
    DISABLED = 0,
    ENABLED = 1,
}
