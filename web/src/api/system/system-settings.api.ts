import {doGet, doPost} from "../system-request.ts";
import type {GetSystemSettingsSchemaData, SystemMaintenanceStatusVO} from "@/types/system/system-settings.types.ts";

export function getSystemSettingsSchema() {
    return doGet<GetSystemSettingsSchemaData>('/api/manager/settings/schema')
}

export function updateSystemSettings(settings: Record<string, string | null>) {
    return doPost('/api/manager/settings/update', settings, { 'Content-Type': 'application/json' })
}

export function testSendEmail(email: string) {
    return doPost('/api/manager/settings/test-send-email', { email })
}

export function getSystemMaintenanceMode() {
    return doGet<SystemMaintenanceStatusVO>('/api/manager/system/maintenance')
}


export function updateSystemMaintenanceMode(enable: boolean) {
    return doPost<boolean>('/api/manager/system/maintenance', { enable: enable })
}