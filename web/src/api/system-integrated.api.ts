import {doGet} from "./system-request.ts";
import type {SystemIntegratedInfoVO} from "../types/system-integrated.types.ts";

export function getSystemIntegratedInfo() {
    return doGet<SystemIntegratedInfoVO>('/api/system/integratedInfo')
}
