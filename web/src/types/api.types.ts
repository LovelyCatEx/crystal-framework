/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

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
