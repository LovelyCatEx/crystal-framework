import type {
    BaseManagerDeleteDTO,
    BaseManagerReadDTO,
    BaseManagerUpdateDTO,
    PaginatedResponseData
} from "../types/api.types.ts";
import {doGet, doPost} from "./system-request.ts";

export class BaseManagerController<
    ENTITY,
    C extends object,
    R extends BaseManagerReadDTO = BaseManagerReadDTO,
    U extends BaseManagerUpdateDTO = BaseManagerUpdateDTO,
    D extends BaseManagerDeleteDTO = BaseManagerDeleteDTO
> {
    constructor(
        private readonly baseUrl: string
    ) {}

    async getById(id: string, additional: Record<string, string> = {}) {
        const result = await this.query({ page: 1, pageSize: 1, id: id, ...additional } as R)
        const records = (result.data?.records ?? [])
        return records.length > 0 ? records[0] : null
    }

    list(queryParams: Record<string, string> = {}) {
        const queryString = new URLSearchParams(queryParams).toString();
        const url = queryString ? `/api${this.baseUrl}/list?${queryString}` : `/api${this.baseUrl}/list`;
        return doGet<ENTITY[]>(url);
    }

    create(dto: C) {
        return doPost<unknown>(`/api${this.baseUrl}/create`, dto);
    }

    query(dto: R) {
        return doGet<PaginatedResponseData<ENTITY>>(`/api${this.baseUrl}/query`, dto);
    }

    update(dto: U) {
        return doPost<unknown>(`/api${this.baseUrl}/update`, dto);
    }

    delete(dto: D) {
        return doPost<unknown>(`/api${this.baseUrl}/delete`, dto);
    }
}