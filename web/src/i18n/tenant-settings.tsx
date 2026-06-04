import type {ReactNode} from "react";
import {NotificationOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import {pluginRegistry} from "@/plugin/registry.ts";

function usePluginTenantSettingsKeys(): string[] {
    return pluginRegistry.getSettingsKeys('tenant');
}

function usePluginTenantSettingsGroups(): string[] {
    return pluginRegistry.getSettingsGroups('tenant');
}

function usePluginTenantSettingsTabs(): string[] {
    return pluginRegistry.getSettingsTabs('tenant');
}

export function useTenantSettingsKeyToTranslationMap(): Map<string, string> {
    const {t} = useTranslation();
    const pluginKeys = usePluginTenantSettingsKeys();

    const map = new Map<string, string>([
        ['notification.memberJoinNotifyEmail', t('pages.tenantSettingsManager.keys.notification.memberJoinNotifyEmail')],
        ['notification.memberJoinReviewNotifyEmail', t('pages.tenantSettingsManager.keys.notification.memberJoinReviewNotifyEmail')],
    ]);

    for (const key of pluginKeys) {
        if (!map.has(key)) {
            map.set(key, t(`pages.tenantSettingsManager.keys.${key}`));
        }
    }

    return map;
}

export function useTenantSettingsGroupToTranslationMap(): Map<string, { label: string; icon?: ReactNode }> {
    const {t} = useTranslation();
    const pluginGroups = usePluginTenantSettingsGroups();

    const map = new Map<string, { label: string; icon?: ReactNode }>([
        ['notification', {label: t('pages.tenantSettingsManager.groups.notification'), icon: <NotificationOutlined/>}],
    ]);

    for (const group of pluginGroups) {
        if (!map.has(group)) {
            map.set(group, {label: t(`pages.tenantSettingsManager.groups.${group}`)});
        }
    }

    return map;
}

export function useTenantSettingsTabToTranslationMap(): Map<string, string> {
    const {t} = useTranslation();
    const pluginTabs = usePluginTenantSettingsTabs();

    const map = new Map<string, string>([
        ['notification', t('pages.tenantSettingsManager.tabs.notification')],
    ]);

    for (const tab of pluginTabs) {
        if (!map.has(tab)) {
            map.set(tab, t(`pages.tenantSettingsManager.tabs.${tab}`));
        }
    }

    return map;
}
