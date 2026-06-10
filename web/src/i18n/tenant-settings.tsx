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
        ['notification.memberJoin.email', t('pages.tenantSettingsManager.keys.notification.memberJoin.email')],
        ['notification.memberJoin.channels', t('pages.tenantSettingsManager.keys.notification.memberJoin.channels')],
        ['notification.memberJoin.content', t('pages.tenantSettingsManager.keys.notification.memberJoin.content')],
        ['notification.memberJoinReview.email', t('pages.tenantSettingsManager.keys.notification.memberJoinReview.email')],
        ['notification.memberJoinReview.channels', t('pages.tenantSettingsManager.keys.notification.memberJoinReview.channels')],
        ['notification.memberJoinReview.content', t('pages.tenantSettingsManager.keys.notification.memberJoinReview.content')],
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
        ['notification.memberJoin', {label: t('pages.tenantSettingsManager.groups.notification.memberJoin'), icon: <NotificationOutlined/>}],
        ['notification.memberJoinReview', {label: t('pages.tenantSettingsManager.groups.notification.memberJoinReview'), icon: <NotificationOutlined/>}],
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
