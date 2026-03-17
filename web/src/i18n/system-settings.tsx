import type {ReactNode} from "react";
import {InfoCircleOutlined, MailOutlined, SettingOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";

export function useSettingsKeyToTranslationMap(): Map<string, string> {
    const { t } = useTranslation();
    return new Map<string, string>([
        ['basic.baseUrl', t('pages.systemSettingsManager.keys.basic.baseUrl')],
        ['bootstrap.autoCheckRbacTableData', t('pages.systemSettingsManager.keys.bootstrap.autoCheckRbacTableData')],
        ['mail.smtp.username', t('pages.systemSettingsManager.keys.mail.smtp.username')],
        ['mail.smtp.password', t('pages.systemSettingsManager.keys.mail.smtp.password')],
        ['mail.smtp.host', t('pages.systemSettingsManager.keys.mail.smtp.host')],
        ['mail.smtp.port', t('pages.systemSettingsManager.keys.mail.smtp.port')],
        ['mail.smtp.ssl', t('pages.systemSettingsManager.keys.mail.smtp.ssl')],
        ['mail.smtp.fromEmail', t('pages.systemSettingsManager.keys.mail.smtp.fromEmail')],
    ]);
}

export function useSettingsGroupToTranslationMap(): Map<string, {label: string, icon?: ReactNode}> {
    const { t } = useTranslation();
    return new Map<string, {label: string, icon?: ReactNode}>([
        ['basic', { label: t('pages.systemSettingsManager.groups.basic'), icon: <InfoCircleOutlined /> }],
        ['bootstrap', { label: t('pages.systemSettingsManager.groups.bootstrap'), icon: <SettingOutlined /> }],
        ['mail.smtp', { label: t('pages.systemSettingsManager.groups.mail.smtp'), icon: <MailOutlined /> }]
    ]);
}
