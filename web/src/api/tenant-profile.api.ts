import { doGet, doPost } from "./system-request.ts";
import type { TenantProfileVO } from "../types/tenant.types.ts";

export function getTenantProfile() {
    return doGet<TenantProfileVO>("/api/tenant/profile");
}

export function updateTenantProfile(dto: UpdateTenantProfileDTO) {
    return doPost<unknown>("/api/tenant/profile/update", dto);
}

export async function uploadTenantIcon(file: File) {
    return doPost('/api/tenant/profile/uploadIcon', {file: file}, {'Content-Type': 'multipart/form-data'});
}

export interface UpdateTenantProfileDTO {
    name?: string;
    description?: string | null;
    contactName?: string;
    contactEmail?: string;
    contactPhone?: string;
    address?: string;
    icon?: string | null;
}
