import {doGet, doPost} from "../system-request.ts";
import type {GetSystemSettingsSchemaData, SystemMaintenanceStatusVO} from "@/types/system/system-settings.types.ts";

export const ChannelType = {
    EMAIL: 1,
    LARK: 2,
} as const;
export type ChannelTypeValue = typeof ChannelType[keyof typeof ChannelType];

export interface TestSendMessageDTO {
    channelType: ChannelTypeValue;
    recipient: Record<string, string | undefined>;
    title?: string;
    content?: string;
}

export interface TestSendMessageResultVO {
    channelType: number;
    success: boolean;
    errorCode?: string;
    errorMessage?: string;
    providerMessageId?: string;
}

export function getSystemSettingsSchema() {
    return doGet<GetSystemSettingsSchemaData>('/api/manager/settings/schema')
}

export function updateSystemSettings(settings: Record<string, string | null>) {
    return doPost('/api/manager/settings/update', settings, { 'Content-Type': 'application/json' })
}

export function testSendEmail(email: string) {
    return doPost('/api/manager/settings/test-send-email', { email })
}

export function testSendMessage(dto: TestSendMessageDTO) {
    return doPost<TestSendMessageResultVO>('/api/manager/settings/test-send-message', dto, { 'Content-Type': 'application/json' })
}

export function getSystemMaintenanceMode() {
    return doGet<SystemMaintenanceStatusVO>('/api/manager/system/maintenance')
}


export function updateSystemMaintenanceMode(enable: boolean) {
    return doPost<boolean>('/api/manager/system/maintenance', { enable: enable })
}