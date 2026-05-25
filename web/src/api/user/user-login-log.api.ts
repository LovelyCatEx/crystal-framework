import {BaseManagerController} from "../BaseManagerController.ts";
import type {
    UserLoginLogEntity,
    ManagerCreateUserLoginLogDTO,
    ManagerReadUserLoginLogDTO,
    ManagerUpdateUserLoginLogDTO,
    ManagerDeleteUserLoginLogDTO
} from "@/types/user/user-login-log.types.ts";

export const UserLoginLogManagerController = new BaseManagerController<
    UserLoginLogEntity,
    ManagerCreateUserLoginLogDTO,
    ManagerReadUserLoginLogDTO,
    ManagerUpdateUserLoginLogDTO,
    ManagerDeleteUserLoginLogDTO
>('/manager/user-login-logs');