import type {GroupNode} from '@/components/table/filter/filter-builder.types';

export interface PageQuery {
    page: number;
    pageSize: number;
}

export interface PaginatedResponseData<T> {
    page: number;
    pageSize: number;
    total: number;
    totalPages: number;
    records: T[];
}

export interface BaseManagerReadDTO extends PageQuery {
    id?: string;
    query?: GroupNode;
}

export interface BaseManagerReadScopedDTO extends BaseManagerReadDTO {
    scope: number;
    scopeId: string;
}

export interface BaseManagerDeleteDTO {
    ids: string[];
}

export interface BaseManagerUpdateDTO {
    id: string;
}
