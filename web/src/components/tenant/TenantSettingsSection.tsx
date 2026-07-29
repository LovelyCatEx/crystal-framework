import {Button, Form, message} from "antd";
import {DiffOutlined, SaveOutlined} from "@ant-design/icons";
import {useEffect, useMemo, useState} from "react";
import {useTranslation} from "react-i18next";
import {useSearchParams} from "react-router-dom";
import {useSWRComposition} from "@/compositions/use-swr.ts";
import {useUserTenants} from "@/compositions/use-tenant.ts";
import {getTenantSettingsSchema, updateTenantSettings} from "@/api/tenant/tenant-settings.api.ts";
import {SettingsRendererContainer} from "@/components/settings/SettingsRendererContainer.tsx";
import {SettingsChangesModal} from "@/components/settings/SettingsChangesModal.tsx";
import {mergeRenderers} from "@/components/settings/merge-renderers.ts";
import {pluginRegistry} from "@/plugin/registry.ts";
import {
    useTenantSettingsGroupToTranslationMap,
    useTenantSettingsKeyToTranslationMap,
    useTenantSettingsTabToTranslationMap,
} from "@/i18n/tenant-settings.tsx";
import type {SettingsGroupExtraRenderer, SettingsItemRenderer} from "@/components/settings/types.ts";
import {
    deserializeSettingsValues,
    diffSettingsValues,
    serializeSettingsValues,
} from "@/utils/settings-value.ts";
import {TenantMessageChannelIdsSelector} from "@/components/selector/MessageChannelIdsSelector/TenantMessageChannelIdsSelector.tsx";
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

    const baseline = useMemo(
        () => (data ? deserializeSettingsValues(data.items) : {}),
        [data],
    );

    const watchedValues = Form.useWatch([], form);

    const changeCount = useMemo(
        () => diffSettingsValues(data?.items ?? {}, baseline, watchedValues ?? {}).length,
        [data, baseline, watchedValues],
    );

    const [changesModalOpen, setChangesModalOpen] = useState(false);

    useEffect(() => {
        if (!data) {
            return;
        }
        form.setFieldsValue(baseline);
    }, [data, baseline]);

    const computeChanges = () => {
        const items = data?.items ?? {};
        const current = form.getFieldsValue(true) as Record<string, unknown>;
        return diffSettingsValues(items, baseline, current);
    };

    const onSave = (values: Record<string, unknown>) => {
        const items = data?.items ?? {};
        const changes = diffSettingsValues(items, baseline, values);
        if (changes.length === 0) {
            void message.info(t('components.settings.noChanges'));
            return;
        }
        const changedItems = Object.fromEntries(
            changes.map((c) => [c.key, items[c.key]]).filter(([, s]) => Boolean(s)),
        );
        const changedValues = Object.fromEntries(changes.map((c) => [c.key, values[c.key]]));
        setSaving(true);
        updateTenantSettings(serializeSettingsValues(changedItems, changedValues))
            .then(() => {
                void message.success(t('pages.tenantSettingsManager.saveSuccess'));
                setChangesModalOpen(false);
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

            <div className="mt-8 flex justify-end space-x-3">
                <Button
                    icon={<DiffOutlined/>}
                    onClick={() => setChangesModalOpen(true)}
                    className="rounded-xl px-6 h-auto py-2"
                >
                    {`${t('components.settings.viewChanges')} (${changeCount})`}
                </Button>
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

            <SettingsChangesModal
                open={changesModalOpen}
                onClose={() => setChangesModalOpen(false)}
                changes={changesModalOpen ? computeChanges() : []}
                keyTranslationMap={tenantSettingsKeyToTranslationMap}
            />
        </Form>
    );
}
