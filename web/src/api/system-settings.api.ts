import {doGet, doPost} from "./system-request.ts";
import type {GetSystemSettingsSchemaData} from "../types/system-settings.types.ts";

export function getSystemSettingsSchema() {
    return doGet<GetSystemSettingsSchemaData>('/api/manager/settings/schema')
}

export function updateSystemSettings(settings: Record<string, string | null>) {
    return doPost('/api/manager/settings/update', settings)
}

export function getSystemMaintenanceMode() {
    return doGet<boolean>('/api/manager/system/maintenance')
}


export function updateSystemMaintenanceMode(enable: boolean) {
    return doPost<boolean>('/api/manager/system/maintenance', { enable: enable })
}