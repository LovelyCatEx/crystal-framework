/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {Descriptions, Input, Select} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {AiModelInvocationRecordManagerController, type ManagerReadAiModelInvocationRecordDTO} from "@/api/ai/ai-model-invocation-record.api.ts";
import {useEffect, useRef} from "react";
import type {JSX} from "react";
import {useAiModelInvocationRecordTableColumns} from "@/components/columns/AiModelInvocationRecordEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";
import {AiModelInvocationStatus, type AiModelInvocationRecordEntity} from "@/types/ai/ai.types.ts";
import {getAiModelInvocationStatus} from "@/i18n/enum-helpers.ts";
import {CopyableToolTip} from "@/components/CopyableToolTip.tsx";
import {CurrencyAmountCodeDisplay} from "@/components/economy/CurrencyAmountCodeDisplay.tsx";
import {AiModelChip} from "@/components/chip/AiModelChip.tsx";
import {AiProviderChip} from "@/components/chip/AiProviderChip.tsx";
import {TenantDisplay} from "@/components/tenant/TenantDisplay.tsx";

function formatBytes(value: string | null | undefined): string {
    if (!value) return '—';
    const bytes = Number(value);
    if (Number.isNaN(bytes)) return '—';
    if (bytes < 1024) return `${bytes} B (${bytes} Bytes)`;
    const units = ['KB', 'MB', 'GB', 'TB'];
    let v = bytes;
    let i = -1;
    do {
        v /= 1024;
        i++;
    } while (v >= 1024 && i < units.length - 1);
    return `${v.toFixed(1).replace(/\.0$/, '')} ${units[i]} (${bytes} Bytes)`;
}

function InvocationRecordDetail({record}: { record: AiModelInvocationRecordEntity }): JSX.Element {
    const {t} = useTranslation();

    const show = (value: string | null | undefined): string => (value ? value : '—');
    const showNum = (value: number | null | undefined): string => (value !== null && value !== undefined ? String(value) : '—');

    const tokenSegments: JSX.Element[] = [
        <>{record.promptTokens} × <CurrencyAmountCodeDisplay currencyId={record.currencyId} amount={record.promptUnitPrice} />/M</>,
        <>{record.completionTokens} × <CurrencyAmountCodeDisplay currencyId={record.currencyId} amount={record.completionUnitPrice} />/M</>,
    ];
    if (record.cachedPromptTokens > 0) {
        tokenSegments.push(<>{record.cachedPromptTokens} × <CurrencyAmountCodeDisplay currencyId={record.currencyId} amount={record.cacheReadUnitPrice} />/M</>);
    }
    if (record.cacheCreationTokens > 0) {
        tokenSegments.push(<>{record.cacheCreationTokens} × <CurrencyAmountCodeDisplay currencyId={record.currencyId} amount={record.cacheWriteUnitPrice} />/M</>);
    }
    const substitution = (
        <>
            ({tokenSegments.map((segment, index) => (
                <span key={index}>{index > 0 ? ' + ' : ''}{segment}</span>
            ))})
            {' '}× {record.groupMultiplier}
        </>
    );

    return (
        <div className="px-6 py-2">
            <Descriptions column={2} size="small" className="text-xs">
                <Descriptions.Item label={t('pages.aiModelInvocationRecordManager.detail.requestId')} span={2}>
                    <CopyableToolTip title={record.requestId}>
                        <span className="font-mono">{record.requestId}</span>
                    </CopyableToolTip>
                </Descriptions.Item>
                <Descriptions.Item label={t('pages.aiModelInvocationRecordManager.detail.sessionId')}>
                    {show(record.sessionId)}
                </Descriptions.Item>
                <Descriptions.Item label={t('pages.aiModelInvocationRecordManager.detail.tenantId')}>
                    <TenantDisplay tenantId={record.tenantId} variant="icon-text" />
                </Descriptions.Item>
                <Descriptions.Item label={t('pages.aiModelInvocationRecordManager.detail.providerId')}>
                    <AiProviderChip providerId={record.providerId} variant="text" />
                </Descriptions.Item>
                <Descriptions.Item label={t('pages.aiModelInvocationRecordManager.detail.modelId')}>
                    <AiModelChip modelId={record.modelId} variant="text" />
                </Descriptions.Item>
                <Descriptions.Item label={t('pages.aiModelInvocationRecordManager.detail.stopReason')}>
                    {show(record.stopReason)}
                </Descriptions.Item>
                <Descriptions.Item label={t('pages.aiModelInvocationRecordManager.detail.errorCode')}>
                    {show(record.errorCode)}
                </Descriptions.Item>
                {record.errorMessage && (
                    <Descriptions.Item label={t('pages.aiModelInvocationRecordManager.detail.errorMessage')} span={2}>
                        <span className="text-red-500">{record.errorMessage}</span>
                    </Descriptions.Item>
                )}
                <Descriptions.Item label={t('pages.aiModelInvocationRecordManager.detail.clientIp')} span={2}>
                    {show(record.clientIp)}
                </Descriptions.Item>
                <Descriptions.Item label={t('pages.aiModelInvocationRecordManager.detail.userAgent')} span={2}>
                    {show(record.userAgent)}
                </Descriptions.Item>
                <Descriptions.Item label={t('pages.aiModelInvocationRecordManager.detail.requestSize')}>
                    {formatBytes(record.requestSizeBytes)}
                </Descriptions.Item>
                <Descriptions.Item label={t('pages.aiModelInvocationRecordManager.detail.responseSize')}>
                    {formatBytes(record.responseSizeBytes)}
                </Descriptions.Item>
                <Descriptions.Item label={t('pages.aiModelInvocationRecordManager.detail.temperature')}>
                    {showNum(record.temperature)}
                </Descriptions.Item>
                <Descriptions.Item label={t('pages.aiModelInvocationRecordManager.detail.topP')}>
                    {showNum(record.topP)}
                </Descriptions.Item>
                <Descriptions.Item label={t('pages.aiModelInvocationRecordManager.detail.maxTokens')}>
                    {showNum(record.maxTokens)}
                </Descriptions.Item>
                <Descriptions.Item label={t('pages.aiModelInvocationRecordManager.detail.toolCalls')}>
                    {record.toolCallsCount}
                </Descriptions.Item>
                <Descriptions.Item label={t('pages.aiModelInvocationRecordManager.detail.messageCount')}>
                    {record.messageCount}
                </Descriptions.Item>
                <Descriptions.Item label={t('pages.aiModelInvocationRecordManager.detail.queueWait')}>
                    {show(record.queueWaitMs)}
                </Descriptions.Item>
                <Descriptions.Item label={t('pages.aiModelInvocationRecordManager.detail.tokensPerSecond')} span={2}>
                    {showNum(record.tokensPerSecond)}
                </Descriptions.Item>
                <Descriptions.Item label={t('pages.aiModelInvocationRecordManager.detail.billing')} span={2}>
                    <div className="font-mono text-xs grid gap-y-1 gap-x-2" style={{gridTemplateColumns: 'auto 1fr'}}>
                        <span>{t('pages.aiModelInvocationRecordManager.detail.actualCost')}</span>
                        <span>= ({t('pages.aiModelInvocationRecordManager.detail.input')} × {t('pages.aiModelInvocationRecordManager.detail.inputPrice')} + {t('pages.aiModelInvocationRecordManager.detail.output')} × {t('pages.aiModelInvocationRecordManager.detail.outputPrice')} + {t('pages.aiModelInvocationRecordManager.detail.cacheRead')} × {t('pages.aiModelInvocationRecordManager.detail.cacheReadPrice')} + {t('pages.aiModelInvocationRecordManager.detail.cacheWrite')} × {t('pages.aiModelInvocationRecordManager.detail.cacheWritePrice')}) × {t('pages.aiModelInvocationRecordManager.detail.groupMultiplier')}</span>
                        <span />
                        <span>= {substitution}</span>
                        <span />
                        <span>= <CurrencyAmountCodeDisplay currencyId={record.currencyId} amount={record.finalCost} /></span>
                    </div>
                </Descriptions.Item>
            </Descriptions>
        </div>
    );
}

export default function AiModelInvocationRecordManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({
        schema: {
            requestId: 'string',
            userId: 'string',
            modelId: 'string',
            status: 'number',
            id: 'string',
        }
    });
    const {t} = useTranslation();
    const columns = useAiModelInvocationRecordTableColumns();

    useEffect(() => {
        pageRef?.current?.refreshData?.({resetPage: true});
    }, [filters.requestId, filters.userId, filters.modelId, filters.status, filters.id]);

    const filterableFields = [
        { field: 'id', type: 'number' as const, label: t('pages.aiModelInvocationRecordManager.filter.id') },
        { field: 'request_id', type: 'text' as const, label: t('pages.aiModelInvocationRecordManager.filter.requestId') },
        { field: 'user_id', type: 'number' as const, label: t('pages.aiModelInvocationRecordManager.filter.userId') },
        { field: 'model_id', type: 'number' as const, label: t('pages.aiModelInvocationRecordManager.filter.modelId') },
        {
            field: 'status',
            type: 'number' as const,
            label: t('pages.aiModelInvocationRecordManager.filter.status'),
            renderValue: ({ value, onChange }: { value: unknown; onChange: (v: unknown) => void }) => (
                <Select
                    className="flex-1"
                    value={value !== undefined ? String(value) : undefined}
                    allowClear
                    placeholder={t('pages.aiModelInvocationRecordManager.filter.all')}
                    options={[
                        { value: String(AiModelInvocationStatus.FAILED), label: getAiModelInvocationStatus(AiModelInvocationStatus.FAILED) },
                        { value: String(AiModelInvocationStatus.SUCCESS), label: getAiModelInvocationStatus(AiModelInvocationStatus.SUCCESS) },
                    ]}
                    onChange={(v) => onChange(v !== undefined ? Number(v) : undefined)}
                />
            ),
        },
    ];

    return (
        <>
            <ActionBarComponent
                title={t('pages.aiModelInvocationRecordManager.title')}
                subtitle={t('pages.aiModelInvocationRecordManager.subtitle')}
            />
            <ManagerPageContainer
                ref={pageRef}
                entityName={t('entityNames.aiModelInvocationRecord')}
                title=""
                subtitle=""
                showActionBar={false}
                readonlyMode={true}
                showRowActions={false}
                hideRecordTimeColumn={true}
                columns={columns}
                expandedRowRender={(record) => <InvocationRecordDetail record={record} />}
                editModalFormChildren={<></>}
                filterableFields={filterableFields}
                queryParamsSync={syncToUrl}
                initialQueryValues={initialQueryValues}
                searchKeywords={['request_id']}
                simpleFilters={[
                    { field: 'id', operator: 'eq', value: filters.id },
                    { field: 'request_id', urlKey: 'requestId', operator: 'contains', value: filters.requestId },
                    { field: 'user_id', urlKey: 'userId', operator: 'eq', value: filters.userId },
                    { field: 'model_id', urlKey: 'modelId', operator: 'eq', value: filters.modelId },
                    { field: 'status', operator: 'eq', value: filters.status },
                ]}
                query={async (props: ManagerReadAiModelInvocationRecordDTO) => {
                    return (await AiModelInvocationRecordManagerController.query(props)).data!;
                }}
                delete={async () => null}
                update={async () => null}
                create={async () => null}
                tableActions={[
                    {
                        label: <span>{t('pages.aiModelInvocationRecordManager.filter.id')}</span>,
                        children: <Input
                            style={{width: 180}}
                            placeholder={t('pages.aiModelInvocationRecordManager.filter.idPlaceholder')}
                            defaultValue={filters.id}
                            allowClear
                            onPressEnter={(e) => setFilter('id', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('id', undefined);
                            }}
                        />,
                    },
                    {
                        label: <span>{t('pages.aiModelInvocationRecordManager.filter.requestId')}</span>,
                        children: <Input
                            style={{width: 180}}
                            placeholder={t('pages.aiModelInvocationRecordManager.filter.requestIdPlaceholder')}
                            defaultValue={filters.requestId}
                            allowClear
                            onPressEnter={(e) => setFilter('requestId', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('requestId', undefined);
                            }}
                        />,
                    },
                    {
                        label: <span>{t('pages.aiModelInvocationRecordManager.filter.userId')}</span>,
                        children: <Input
                            style={{width: 140}}
                            placeholder={t('pages.aiModelInvocationRecordManager.filter.userIdPlaceholder')}
                            defaultValue={filters.userId}
                            allowClear
                            onPressEnter={(e) => setFilter('userId', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('userId', undefined);
                            }}
                        />,
                    },
                    {
                        label: <span>{t('pages.aiModelInvocationRecordManager.filter.modelId')}</span>,
                        children: <Input
                            style={{width: 140}}
                            placeholder={t('pages.aiModelInvocationRecordManager.filter.modelIdPlaceholder')}
                            defaultValue={filters.modelId}
                            allowClear
                            onPressEnter={(e) => setFilter('modelId', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('modelId', undefined);
                            }}
                        />,
                    },
                    {
                        label: <span>{t('pages.aiModelInvocationRecordManager.filter.status')}</span>,
                        children: <Select
                            defaultValue={filters.status !== undefined ? String(filters.status) : '-1'}
                            style={{width: 120}}
                            options={[
                                {value: '-1', label: t('pages.aiModelInvocationRecordManager.filter.all')},
                                {value: String(AiModelInvocationStatus.FAILED), label: getAiModelInvocationStatus(AiModelInvocationStatus.FAILED)},
                                {value: String(AiModelInvocationStatus.SUCCESS), label: getAiModelInvocationStatus(AiModelInvocationStatus.SUCCESS)},
                            ]}
                            onChange={(value) => setFilter('status', value === '-1' ? undefined : Number.parseInt(value))}
                        />,
                    },
                ]}
            />
        </>
    );
}
