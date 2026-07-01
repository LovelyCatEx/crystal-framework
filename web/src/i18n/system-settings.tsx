import type {ReactNode} from "react";
import {
    AppstoreOutlined,
    CopyrightCircleOutlined,
    InfoCircleOutlined,
    LockOutlined,
    MailOutlined,
    RobotOutlined,
    SettingOutlined,
    ApiOutlined
} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import {pluginRegistry} from "@/plugin/registry.ts";

function usePluginSystemSettingsKeys(): string[] {
    return pluginRegistry.getSettingsKeys('system');
}

function usePluginSystemSettingsGroups(): string[] {
    return pluginRegistry.getSettingsGroups('system');
}

function usePluginSystemSettingsTabs(): string[] {
    return pluginRegistry.getSettingsTabs('system');
}

export function useSettingsKeyToTranslationMap(): Map<string, string> {
    const { t } = useTranslation();
    const pluginKeys = usePluginSystemSettingsKeys();

    const map = new Map<string, string>([
        ['basic.baseUrl', t('pages.systemSettingsManager.keys.basic.baseUrl')],
        ['basic.frontendBaseUrl', t('pages.systemSettingsManager.keys.basic.frontendBaseUrl')],
        ['basic.waterMark.enabled', t('pages.systemSettingsManager.keys.basic.waterMark.enabled')],
        ['basic.waterMark.type', t('pages.systemSettingsManager.keys.basic.waterMark.type')],
        ['basic.waterMark.customValue', t('pages.systemSettingsManager.keys.basic.waterMark.customValue')],
        ['basic.waterMark.fontColor', t('pages.systemSettingsManager.keys.basic.waterMark.fontColor')],
        ['bootstrap.autoCheckRbacTableData', t('pages.systemSettingsManager.keys.bootstrap.autoCheckRbacTableData')],
        ['mail.smtp.username', t('pages.systemSettingsManager.keys.mail.smtp.username')],
        ['mail.smtp.password', t('pages.systemSettingsManager.keys.mail.smtp.password')],
        ['mail.smtp.host', t('pages.systemSettingsManager.keys.mail.smtp.host')],
        ['mail.smtp.port', t('pages.systemSettingsManager.keys.mail.smtp.port')],
        ['mail.smtp.ssl', t('pages.systemSettingsManager.keys.mail.smtp.ssl')],
        ['mail.smtp.fromEmail', t('pages.systemSettingsManager.keys.mail.smtp.fromEmail')],
        ['messageChannel.lark.appId', t('pages.systemSettingsManager.keys.messageChannel.lark.appId')],
        ['messageChannel.lark.appSecret', t('pages.systemSettingsManager.keys.messageChannel.lark.appSecret')],
        ['messageChannel.lark.baseUrl', t('pages.systemSettingsManager.keys.messageChannel.lark.baseUrl')],
        ['security.api.encrypt.enabled', t('pages.systemSettingsManager.keys.security.api.encrypt.enabled')],
        ['security.api.encrypt.scope', t('pages.systemSettingsManager.keys.security.api.encrypt.scope')],
        ['security.api.encrypt.securityLevel', t('pages.systemSettingsManager.keys.security.api.encrypt.securityLevel')],
        ['oauth.github.enabled', t('pages.systemSettingsManager.keys.oauth.github.enabled')],
        ['oauth.github.useDefault', t('pages.systemSettingsManager.keys.oauth.github.useDefault')],
        ['oauth.github.authorizationUri', t('pages.systemSettingsManager.keys.oauth.github.authorizationUri')],
        ['oauth.github.tokenUri', t('pages.systemSettingsManager.keys.oauth.github.tokenUri')],
        ['oauth.github.userInfoUri', t('pages.systemSettingsManager.keys.oauth.github.userInfoUri')],
        ['oauth.github.userNameAttribute', t('pages.systemSettingsManager.keys.oauth.github.userNameAttribute')],
        ['oauth.github.clientId', t('pages.systemSettingsManager.keys.oauth.github.clientId')],
        ['oauth.github.clientSecret', t('pages.systemSettingsManager.keys.oauth.github.clientSecret')],
        ['oauth.github.scope', t('pages.systemSettingsManager.keys.oauth.github.scope')],
        ['oauth.google.enabled', t('pages.systemSettingsManager.keys.oauth.google.enabled')],
        ['oauth.google.useDefault', t('pages.systemSettingsManager.keys.oauth.google.useDefault')],
        ['oauth.google.authorizationUri', t('pages.systemSettingsManager.keys.oauth.google.authorizationUri')],
        ['oauth.google.tokenUri', t('pages.systemSettingsManager.keys.oauth.google.tokenUri')],
        ['oauth.google.userInfoUri', t('pages.systemSettingsManager.keys.oauth.google.userInfoUri')],
        ['oauth.google.userNameAttribute', t('pages.systemSettingsManager.keys.oauth.google.userNameAttribute')],
        ['oauth.google.clientId', t('pages.systemSettingsManager.keys.oauth.google.clientId')],
        ['oauth.google.clientSecret', t('pages.systemSettingsManager.keys.oauth.google.clientSecret')],
        ['oauth.google.scope', t('pages.systemSettingsManager.keys.oauth.google.scope')],
        ['oauth.oicq.enabled', t('pages.systemSettingsManager.keys.oauth.oicq.enabled')],
        ['oauth.oicq.authorizationUri', t('pages.systemSettingsManager.keys.oauth.oicq.authorizationUri')],
        ['oauth.oicq.tokenUri', t('pages.systemSettingsManager.keys.oauth.oicq.tokenUri')],
        ['oauth.oicq.userInfoUri', t('pages.systemSettingsManager.keys.oauth.oicq.userInfoUri')],
        ['oauth.oicq.userNameAttribute', t('pages.systemSettingsManager.keys.oauth.oicq.userNameAttribute')],
        ['oauth.oicq.clientId', t('pages.systemSettingsManager.keys.oauth.oicq.clientId')],
        ['oauth.oicq.clientSecret', t('pages.systemSettingsManager.keys.oauth.oicq.clientSecret')],
        ['oauth.oicq.scope', t('pages.systemSettingsManager.keys.oauth.oicq.scope')],
        ['module.tenant.enabled', t('pages.systemSettingsManager.keys.module.tenant.enabled')],
        ['module.approval.enabled', t('pages.systemSettingsManager.keys.module.approval.enabled')],
    ]);

    for (const key of pluginKeys) {
        if (!map.has(key)) {
            map.set(key, t(`pages.systemSettingsManager.keys.${key}`));
        }
    }

    return map;
}

export function useSettingsGroupToTranslationMap(): Map<string, {label: string, icon?: ReactNode}> {
    const { t } = useTranslation();
    const pluginGroups = usePluginSystemSettingsGroups();

    const map = new Map<string, {label: string, icon?: ReactNode}>([
        ['basic', { label: t('pages.systemSettingsManager.groups.basic'), icon: <InfoCircleOutlined /> }],
        ['basic.waterMark', { label: t('pages.systemSettingsManager.groups.basic.waterMark'), icon: <CopyrightCircleOutlined /> }],
        ['bootstrap', { label: t('pages.systemSettingsManager.groups.bootstrap'), icon: <SettingOutlined /> }],
        ['mail.smtp', { label: t('pages.systemSettingsManager.groups.mail.smtp'), icon: <MailOutlined /> }],
        ['messageChannel.lark', { label: t('pages.systemSettingsManager.groups.messageChannel.lark'), icon: <RobotOutlined /> }],
        ['security.api.encrypt', { label: t('pages.systemSettingsManager.groups.security.api.encrypt'), icon: <LockOutlined /> }],
        ['oauth.github', { label: t('pages.systemSettingsManager.groups.oauth.github'), icon: <ApiOutlined /> }],
        ['oauth.google', { label: t('pages.systemSettingsManager.groups.oauth.google'), icon: <ApiOutlined /> }],
        ['oauth.oicq', { label: t('pages.systemSettingsManager.groups.oauth.oicq'), icon: <ApiOutlined /> }],
        ['module.tenant', { label: t('pages.systemSettingsManager.groups.module.tenant'), icon: <AppstoreOutlined /> }],
        ['module.approval', { label: t('pages.systemSettingsManager.groups.module.approval'), icon: <AppstoreOutlined /> }],
    ]);

    for (const group of pluginGroups) {
        if (!map.has(group)) {
            map.set(group, { label: t(`pages.systemSettingsManager.groups.${group}`) });
        }
    }

    return map;
}

export function useSettingsTabToTranslationMap(): Map<string, string> {
    const { t } = useTranslation();
    const pluginTabs = usePluginSystemSettingsTabs();

    const map = new Map<string, string>([
        ['basic', t('pages.systemSettingsManager.tabs.basic')],
        ['bootstrap', t('pages.systemSettingsManager.tabs.bootstrap')],
        ['mail', t('pages.systemSettingsManager.tabs.mail')],
        ['messageChannel', t('pages.systemSettingsManager.tabs.messageChannel')],
        ['security', t('pages.systemSettingsManager.tabs.security')],
        ['oauth', t('pages.systemSettingsManager.tabs.oauth')],
        ['module', t('pages.systemSettingsManager.tabs.module')],
    ]);

    for (const tab of pluginTabs) {
        if (!map.has(tab)) {
            map.set(tab, t(`pages.systemSettingsManager.tabs.${tab}`));
        }
    }

    return map;
}
