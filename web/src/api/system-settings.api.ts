import {doGet, doPost} from "./system-request.ts";
import type {GetSystemSettingsSchemaData} from "../types/system-settings.types.ts";

export function getSystemSettingsSchema() {
    return doGet<GetSystemSettingsSchemaData>('/api/manager/settings/schema')
}

export function updateSystemSettings(settings: Record<string, string | null>) {
    return doPost('/api/manager/settings/update', settings, { 'Content-Type': 'application/json' })
}