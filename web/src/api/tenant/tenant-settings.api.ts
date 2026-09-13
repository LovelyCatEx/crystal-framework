/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {doGet, doPost} from "../system-request.ts";
import type {GetTenantSettingsSchemaData} from "@/types/tenant/tenant-settings.types.ts";

export function getTenantSettingsSchema() {
    return doGet<GetTenantSettingsSchemaData>('/api/tenant/settings/schema')
}

export function updateTenantSettings(settings: Record<string, string | null>) {
    return doPost('/api/tenant/settings/update', settings, {'Content-Type': 'application/json'})
}
