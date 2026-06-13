import i18n from "./index.ts";

function translateEnum(namespace: string, value: string | number): string {
    const key = `enums.${namespace}.${value}`;
    const translated = i18n.t(key);
    if (translated === key) {
        if (import.meta.env.DEV) {
            console.warn(`[i18n] missing enum translation: ${key}`);
        }
        const fallback = i18n.t('enums.unknown');
        return fallback === 'enums.unknown' ? String(value) : `${fallback} (${value})`;
    }
    return translated;
}

export function getTenantMemberStatus(status: number): string {
    return translateEnum('tenantMemberStatus', status);
}

export function getTenantStatus(status: number): string {
    return translateEnum('tenantStatus', status);
}

export function getResourceFileType(type: number): string {
    return translateEnum('resourceFileType', type);
}

export function getDepartmentMemberRoleType(roleType: number): string {
    return translateEnum('departmentMemberRoleType', roleType);
}

export function getActuatorMetric(metric: string): string {
    return translateEnum('actuatorMetrics', metric);
}

export function getTenantPermissionType(type: number): string {
    return translateEnum('tenantPermissionType', type);
}

export function getPermissionType(type: number): string {
    return translateEnum('permissionType', type);
}

export function getSystemSettingsItemValueType(type: string): string {
    return translateEnum('systemSettingsItemValueType', type);
}

export function getStorageProviderType(type: number): string {
    return translateEnum('storageProviderType', type);
}

export function getTenantBenefitType(type: number): string {
    return translateEnum('tenantBenefitType', type);
}

export function getOAuthPlatform(platform: number): string {
    return translateEnum('oAuthPlatform', platform);
}

export function getOAuthBindingScope(scope: number): string {
    return translateEnum('oAuthBindingScope', scope);
}

export function getChannelType(type: number): string {
    return translateEnum('channelType', type);
}

export function getMessageChannelPreset(channelType: number, presetKey: string): string {
    return translateEnum('messageChannelPreset', `${channelType}_${presetKey}`);
}

export function getGender(gender: number): string {
    return translateEnum('gender', gender);
}

export function getDictTypeStatus(status: number): string {
    return translateEnum('dictTypeStatus', status);
}

export function getDictItemStatus(status: number): string {
    return translateEnum('dictItemStatus', status);
}

export function getApprovalFlowDefinitionStatus(status: number): string {
    return translateEnum('approvalFlowDefinitionStatus', status);
}

export function getApprovalFlowNodeType(type: number): string {
    return translateEnum('approvalFlowNodeType', type);
}

export function getApprovalFlowApproveMode(mode: number): string {
    return translateEnum('approvalFlowApproveMode', mode);
}
