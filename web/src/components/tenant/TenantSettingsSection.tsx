import {Button, Form, message} from "antd";
import {SaveOutlined} from "@ant-design/icons";
import {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {useSWRComposition} from "@/compositions/use-swr.ts";
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

export const tenantSettingsItemRenderers = new Map<string, SettingsItemRenderer>();

export const tenantSettingsGroupExtraRenderers = new Map<string, SettingsGroupExtraRenderer>();

export function TenantSettingsSection() {
    const {t} = useTranslation();
    const [refreshing, setRefreshing] = useState(false);
    const [saving, setSaving] = useState(false);
    const [form] = Form.useForm();
    const tenantSettingsTabToTranslationMap = useTenantSettingsTabToTranslationMap();
    const tenantSettingsGroupToTranslationMap = useTenantSettingsGroupToTranslationMap();
    const tenantSettingsKeyToTranslationMap = useTenantSettingsKeyToTranslationMap();
    const itemRenderers = mergeRenderers(tenantSettingsItemRenderers, pluginRegistry.getSettingsItemRenderers('tenant'));
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
                showTabs={false}
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
