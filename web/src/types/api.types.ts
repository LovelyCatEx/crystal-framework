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
    searchKeyword?: string;
}

export interface BaseManagerDeleteDTO {
    ids: string[];
}

export interface BaseManagerUpdateDTO {
    id: string;
}