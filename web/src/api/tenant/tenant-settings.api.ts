import {doGet, doPost} from "../system-request.ts";
import type {GetTenantSettingsSchemaData} from "@/types/tenant/tenant-settings.types.ts";

export function getTenantSettingsSchema() {
    return doGet<GetTenantSettingsSchemaData>('/api/tenant/settings/schema')
}

export function updateTenantSettings(settings: Record<string, string | null>) {
    return doPost('/api/tenant/settings/update', settings, {'Content-Type': 'application/json'})
}
