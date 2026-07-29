import {Alert, Col, Form, Input, message, Modal, Row, Select, Switch} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateMessageChannelDTO,
    type ManagerReadMessageChannelDTO,
    type ManagerUpdateMessageChannelDTO,
    MessageChannelManagerController
} from "@/api/message-channel/message-channel.api.ts";
import React, {forwardRef, useEffect, useImperativeHandle, useRef} from "react";
import {ChannelType, type MessageChannel} from "@/types/message-channel/message-channel.types.ts";
import {MessageChannelConfigEditor} from "@/components/editor/MessageChannelConfigEditor.tsx";
import {useMessageChannelTableColumns} from "@/components/columns/MessageChannelEntityColumns.tsx";
import {
    getDefaultPreset,
    isEmptyConfig,
    sanitizeMessageChannelConfig,
    serializePreset
} from "@/utils/message-channel.utils.ts";
import {useTranslation} from "react-i18next";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";
import type {BaseManagerReadDTO} from "@/types/api.types.ts";
import {getChannelType} from "@/i18n/enum-helpers.ts";

export interface MessageChannelManagerPanelProps {
    /** Scope typeId (e.g. ResourceScope.SYSTEM=0 / TENANT=1). */
    scope: number;
    /** ID within that scope (0 for SYSTEM, tenantId for TENANT). */
    scopeId: string;
    /** i18n page namespace, e.g. `pages.tenantMessageChannelManager` or `pages.myTenantMessageChannelManager`. */
    i18nPrefix: string;
}

export interface MessageChannelManagerPanelRef {
    openCreateModal: () => void;
}

interface EditFormBodyProps {
    editingItem: MessageChannel | null;
    i18nPrefix: string;
    channelTypeOptions: { label: string; value: ChannelType }[];
}

function EditFormBody({editingItem, i18nPrefix, channelTypeOptions}: EditFormBodyProps) {
    const {t} = useTranslation();
    const form = Form.useFormInstance();
    const channelType = Form.useWatch('channelType', form) as ChannelType | undefined;
    const previousTypeRef = useRef<ChannelType | undefined>(channelType);

    useEffect(() => {
        if (editingItem) {
            previousTypeRef.current = channelType;
        }
    }, [editingItem, channelType]);

    const writePresetForType = (type: ChannelType) => {
        const preset = getDefaultPreset(type);
        if (!preset) return;
        form.setFieldValue('config', serializePreset(preset));
    };

    const handleChannelTypeChange = (next: ChannelType) => {
        const prev = previousTypeRef.current;
        const currentConfig = form.getFieldValue('config') as string | undefined;

        if (prev === undefined || isEmptyConfig(currentConfig)) {
            writePresetForType(next);
            previousTypeRef.current = next;
            return;
        }

        Modal.confirm({
            title: t(`${i18nPrefix}.modal.channelType.switchConfirmTitle`),
            content: t(`${i18nPrefix}.modal.channelType.switchConfirmContent`),
            onOk: () => {
                writePresetForType(next);
                previousTypeRef.current = next;
            },
            onCancel: () => {
                form.setFieldValue('channelType', prev);
            }
        });
    };

    return (
        <>
            <Row gutter={24}>
                <Col span={12}>
                    <Form.Item
                        name="channelType"
                        label={t(`${i18nPrefix}.modal.channelType.label`)}
                        rules={[{required: true, message: t(`${i18nPrefix}.modal.channelType.required`)}]}
                    >
                        <Select
                            className="w-full rounded-lg h-10 flex items-center"
                            placeholder={t(`${i18nPrefix}.modal.channelType.placeholder`)}
                            disabled={!!editingItem}
                            options={channelTypeOptions}
                            onChange={handleChannelTypeChange}
                        />
                    </Form.Item>
                </Col>
                <Col span={12}>
                    <Form.Item
                        name="name"
                        label={t(`${i18nPrefix}.modal.name.label`)}
                        rules={[
                            {required: true, message: t(`${i18nPrefix}.modal.name.required`)},
                            {max: 64, message: t(`${i18nPrefix}.modal.name.maxLength`)}
                        ]}
                    >
                        <Input
                            className="w-full rounded-lg h-10 flex items-center"
                            placeholder={t(`${i18nPrefix}.modal.name.placeholder`)}
                            maxLength={64}
                            showCount
                        />
                    </Form.Item>
                </Col>
            </Row>
            <Form.Item
                name="enabled"
                label={t(`${i18nPrefix}.modal.enabled.label`)}
                valuePropName="checked"
                initialValue={true}
            >
                <Switch/>
            </Form.Item>
            {!!editingItem && (
                <Alert
                    type="info"
                    showIcon
                    className="mb-4"
                    message={t(`${i18nPrefix}.modal.config.encryptedHint`)}
                />
            )}
            <Form.Item
                name="config"
                label={t(`${i18nPrefix}.modal.config.label`)}
                getValueProps={(value) => ({
                    value: typeof value === 'string' ? sanitizeMessageChannelConfig(value) : value
                })}
                rules={[{required: true, message: t(`${i18nPrefix}.modal.config.required`)}]}
            >
                <MessageChannelConfigEditor
                    placeholder={t(`${i18nPrefix}.modal.config.placeholder`)}
                    channelType={channelType}
                />
            </Form.Item>
        </>
    );
}

export const MessageChannelManagerPanel = forwardRef<MessageChannelManagerPanelRef, MessageChannelManagerPanelProps>(
    function MessageChannelManagerPanel({scope, scopeId, i18nPrefix}, ref) {
    const {t} = useTranslation();
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const baseColumns = useMessageChannelTableColumns();
    const {filters, setFilter, syncToUrl, initialQueryValues} = useManagerQueryParams({
        schema: {type: 'number', id: 'string'}
    });

    useImperativeHandle(ref, () => ({
        openCreateModal: () => pageRef.current?.openModal(),
    }));

    useEffect(() => {
        pageRef?.current?.refreshData?.({resetPage: true});
    }, [filters.type, filters.id]);

    const handleEnabledChange = (enabled: boolean, row: MessageChannel) => {
        MessageChannelManagerController
            .update({id: row.id, enabled})
            .then(() => {
                void message.success(t(`${i18nPrefix}.messages.statusUpdateSuccess`));
                pageRef.current?.refreshData();
            })
            .catch(() => {
                void message.error(t(`${i18nPrefix}.messages.statusUpdateFailed`));
            });
    };

    const columns = [
        ...baseColumns,
        {
            title: t('components.columns.messageChannel.enabled'),
            dataIndex: "enabled",
            key: "enabled",
            width: 100,
            render: function (_: unknown, row: MessageChannel): React.ReactNode {
                return <Switch value={row.enabled} onChange={(enabled) => handleEnabledChange(enabled, row)}/>;
            }
        }
    ];

    const channelTypeOptions = [
        {label: getChannelType(ChannelType.EMAIL), value: ChannelType.EMAIL},
        {label: getChannelType(ChannelType.LARK), value: ChannelType.LARK}
    ];

    return (
        <ManagerPageContainer
            ref={pageRef}
            className="mt-4"
            entityName={t('entityNames.messageChannel')}
            title=""
            subtitle=""
            showActionBar={false}
            columns={columns}
            searchKeywords={['name']}
            queryParamsSync={syncToUrl}
            initialQueryValues={initialQueryValues}
            simpleFilters={[
                {field: 'id', operator: 'eq', value: filters.id},
                {field: 'channel_type', operator: 'eq', value: filters.type},
            ]}
            tableActions={[
                {
                    label: <span>{t(`${i18nPrefix}.filter.id`)}</span>,
                    children: <Input
                        style={{width: 160}}
                        placeholder={t(`${i18nPrefix}.filter.idPlaceholder`)}
                        defaultValue={filters.id}
                        allowClear
                        onPressEnter={(e: React.KeyboardEvent<HTMLInputElement>) => setFilter('id', (e.target as HTMLInputElement).value || undefined)}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => { if (e.target.value === '') setFilter('id', undefined); }}
                    />,
                },
                {
                    label: <span>{t(`${i18nPrefix}.filter.type`)}</span>,
                    children: <Select
                        defaultValue={filters.type !== undefined ? String(filters.type) : '-1'}
                        style={{width: 120}}
                        options={[
                            {value: '-1', label: t(`${i18nPrefix}.filter.all`)},
                            {label: getChannelType(ChannelType.EMAIL), value: String(ChannelType.EMAIL)},
                            {label: getChannelType(ChannelType.LARK), value: String(ChannelType.LARK)},
                        ]}
                        onChange={(value) => setFilter('type', value === '-1' ? undefined : Number.parseInt(value))}
                    />,
                }
            ]}
            editModalFormChildren={(editingItem) => (
                <EditFormBody
                    editingItem={editingItem}
                    i18nPrefix={i18nPrefix}
                    channelTypeOptions={channelTypeOptions}
                />
            )}
            query={async (props: BaseManagerReadDTO) => {
                return (await MessageChannelManagerController.query({
                    ...(props as ManagerReadMessageChannelDTO),
                    scope,
                    scopeId,
                })).data!
            }}
            delete={async (props) => {
                return (await MessageChannelManagerController.delete(props)).data!
            }}
            update={async (props: ManagerUpdateMessageChannelDTO) => {
                // The form is seeded with the raw entity config (sensitive fields still `ENC:`-prefixed).
                // Sanitize on submit so what is sent matches what the operator saw (secrets blanked unless
                // re-entered), avoiding double-encryption of an untouched ciphertext on the backend.
                const updateProps: ManagerUpdateMessageChannelDTO = {
                    ...props,
                    config: typeof props.config === 'string' ? sanitizeMessageChannelConfig(props.config) : props.config
                };
                return (await MessageChannelManagerController.update(updateProps)).data!
            }}
            create={async (props) => {
                const values = props as unknown as ManagerCreateMessageChannelDTO;
                return (await MessageChannelManagerController.create({
                    ...values,
                    scope,
                    scopeId,
                })).data!
            }}
        />
    );
});
