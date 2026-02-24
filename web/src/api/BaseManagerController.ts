import type {BaseManagerDeleteDTO, BaseManagerReadDTO, BaseManagerUpdateDTO} from "../types/api.types.ts";
import {doGet, doPost, type PaginatedResponseData} from "./system-request.ts";

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