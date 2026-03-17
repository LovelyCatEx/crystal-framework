import i18n from "./index.ts";

export function getTenantMemberStatus(status: number): string {
    return i18n.t(`enums.tenantMemberStatus.${status}`);
}

export function getTenantStatus(status: number): string {
    return i18n.t(`enums.tenantStatus.${status}`);
}

export function getResourceFileType(type: number): string {
    return i18n.t(`enums.resourceFileType.${type}`);
}

export function getDepartmentMemberRoleType(roleType: number): string {
    return i18n.t(`enums.departmentMemberRoleType.${roleType}`);
}

export function getActuatorMetric(metric: string): string {
    return i18n.t(`enums.actuatorMetrics.${metric}`);
}

export function getTenantPermissionType(type: number): string {
    return i18n.t(`enums.tenantPermissionType.${type}`);
}

export function getPermissionType(type: number): string {
    return i18n.t(`enums.permissionType.${type}`);
}

export function getSystemSettingsItemValueType(type: string): string {
    return i18n.t(`enums.systemSettingsItemValueType.${type}`);
}

export function getStorageProviderType(type: number): string {
    return i18n.t(`enums.storageProviderType.${type}`);
}

export function getOAuthPlatform(platform: number): string {
    return i18n.t(`enums.oAuthPlatform.${platform}`);
}
