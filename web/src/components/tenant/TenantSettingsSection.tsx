import {Button, Form, message} from "antd";
import {SaveOutlined} from "@ant-design/icons";
import {useEffect, useMemo, useState} from "react";
import {useTranslation} from "react-i18next";
import {useSearchParams} from "react-router-dom";
import {useSWRComposition} from "@/compositions/use-swr.ts";
import {useUserTenants} from "@/compositions/use-tenant.ts";
import {getTenantSettingsSchema, updateTenantSettings} from "@/api/tenant/tenant-settings.api.ts";
import {SettingsRendererContainer} from "@/components/settings/SettingsRendererContainer.tsx";
import {mergeRenderers} from "@/components/settings/merge-renderers.ts";
import {pluginRegistry} from "@/plugin/registry.ts";
import {
    useTenantSettingsGroupToTranslationMap,
    useTenantSettingsKeyToTranslationMap,
    useTenantSettingsTabToTranslationMap,
} from "@/i18n/tenant-settings.tsx";
import type {SettingsGroupExtraRenderer, SettingsItemRenderer} from "@/components/settings/types.ts";
import {deserializeSettingsValues, serializeSettingsValues} from "@/utils/settings-value.ts";
import {TenantMessageChannelIdsSelector} from "@/components/selector/TenantMessageChannelIdsSelector.tsx";
import {MessageChainEditor} from "@/components/message-chain-editor";

const xmlContentRenderer: SettingsItemRenderer = (ctx) => (
    <MessageChainEditor disabled={ctx.loading} minRows={3} maxRows={10} />
);

export const tenantSettingsItemRenderers = new Map<string, SettingsItemRenderer>([
    ['notification.memberJoin.content', xmlContentRenderer],
    ['notification.memberJoinReview.content', xmlContentRenderer],
]);

export const tenantSettingsGroupExtraRenderers = new Map<string, SettingsGroupExtraRenderer>();

export function TenantSettingsSection() {
    const {t} = useTranslation();
    const {currentTenant} = useUserTenants();
    const tenantId = currentTenant?.tenantId ?? null;
    const [refreshing, setRefreshing] = useState(false);
    const [saving, setSaving] = useState(false);
    const [form] = Form.useForm();
    const [searchParams, setSearchParams] = useSearchParams();
    const activeTab = searchParams.get('tab') || undefined;
    const tenantSettingsTabToTranslationMap = useTenantSettingsTabToTranslationMap();
    const tenantSettingsGroupToTranslationMap = useTenantSettingsGroupToTranslationMap();
    const tenantSettingsKeyToTranslationMap = useTenantSettingsKeyToTranslationMap();

    const handleTabChange = (key: string) => {
        setSearchParams({tab: key});
    };

    // Channel selectors are bound to the current tenant, so they are built here rather than
    // at module scope; merged on top of the static (content) and plugin renderers.
    const channelRenderers = useMemo<Map<string, SettingsItemRenderer>>(() => {
        if (!tenantId) {
            return new Map();
        }
        const renderChannelSelect: SettingsItemRenderer = () => (
            <TenantMessageChannelIdsSelector tenantId={tenantId}/>
        );
        return new Map<string, SettingsItemRenderer>([
            ['notification.memberJoin.channels', renderChannelSelect],
            ['notification.memberJoinReview.channels', renderChannelSelect],
        ]);
    }, [tenantId]);

    const itemRenderers = mergeRenderers(
        mergeRenderers(tenantSettingsItemRenderers, channelRenderers),
        pluginRegistry.getSettingsItemRenderers('tenant'),
    );
    const groupExtraRenderers = mergeRenderers(tenantSettingsGroupExtraRenderers, pluginRegistry.getSettingsGroupExtraRenderers('tenant'));

    const {data, isLoading, mutate} = useSWRComposition(
        'tenant-settings-schema',
        () => getTenantSettingsSchema().then((res) => res.data),
        () => void message.error(t('pages.tenantSettingsManager.fetchFailed'))
    );

    useEffect(() => {
        if (!data) {
            return;
        }
        const kv = deserializeSettingsValues(data.items);
        form.setFieldsValue(kv);
    }, [data]);

    const onSave = (values: Record<string, unknown>) => {
        setSaving(true);
        updateTenantSettings(serializeSettingsValues(data?.items ?? {}, values))
            .then(() => {
                void message.success(t('pages.tenantSettingsManager.saveSuccess'));
            })
            .catch(() => {
                void message.error(t('pages.tenantSettingsManager.saveFailed'));
            })
            .finally(() => {
                setRefreshing(true);
                mutate().finally(() => {
                    setRefreshing(false);
                    setSaving(false);
                });
            });
    };

    return (
        <Form form={form} layout="vertical" onFinish={onSave}>
            <SettingsRendererContainer
                data={data}
                loading={isLoading || refreshing}
                tabTranslationMap={tenantSettingsTabToTranslationMap}
                groupTranslationMap={tenantSettingsGroupToTranslationMap}
                keyTranslationMap={tenantSettingsKeyToTranslationMap}
                enumTranslator={(key, value) =>
                    t(`pages.tenantSettingsManager.enums.${key}.${value}`)
                }
                itemRenderers={itemRenderers}
                groupExtraRenderers={groupExtraRenderers}
                maxColumns={2}
                activeTab={activeTab}
                onTabChange={handleTabChange}
            />

            <div className="mt-8 flex justify-end">
                <Button
                    type="primary"
                    icon={<SaveOutlined/>}
                    htmlType="submit"
                    loading={saving}
                    className="rounded-xl px-8 h-auto py-2"
                >
                    {t('pages.tenantSettingsManager.saveSettings')}
                </Button>
            </div>
        </Form>
    );
}
