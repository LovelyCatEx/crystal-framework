import {BaseManagerController} from "../BaseManagerController.ts";
import type {
    UserBanRecord,
    ManagerReadUserBanRecordDTO
} from "@/types/user/user-ban-record.types.ts";

/**
 * Readonly manager controller for user ban records.
 * Backend: ReadonlyManagerController at /api/manager/user-ban-record ({list,query}).
 */
export const UserBanRecordManagerController = new BaseManagerController<
    UserBanRecord,
    Record<string, never>,
    ManagerReadUserBanRecordDTO
>('/manager/user-ban-record');
