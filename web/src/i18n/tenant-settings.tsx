import type {ReactNode} from "react";
import {NotificationOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";

export function useTenantSettingsKeyToTranslationMap(): Map<string, string> {
    const {t} = useTranslation();

    return new Map<string, string>([
        ['notification.memberJoinNotifyEmail', t('pages.tenantSettingsManager.keys.notification.memberJoinNotifyEmail')],
        ['notification.memberJoinReviewNotifyEmail', t('pages.tenantSettingsManager.keys.notification.memberJoinReviewNotifyEmail')],
    ]);
}

export function useTenantSettingsGroupToTranslationMap(): Map<string, { label: string; icon?: ReactNode }> {
    const {t} = useTranslation();

    return new Map<string, { label: string; icon?: ReactNode }>([
        ['notification', {label: t('pages.tenantSettingsManager.groups.notification'), icon: <NotificationOutlined/>}],
    ]);
}

export function useTenantSettingsTabToTranslationMap(): Map<string, string> {
    const {t} = useTranslation();

    return new Map<string, string>([
        ['notification', t('pages.tenantSettingsManager.tabs.notification')],
    ]);
}
