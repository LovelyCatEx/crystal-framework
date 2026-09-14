import {Input, Select} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {AiModelInvocationRecordManagerController, type ManagerReadAiModelInvocationRecordDTO} from "@/api/ai/ai-model-invocation-record.api.ts";
import {useEffect, useRef} from "react";
import {useAiModelInvocationRecordTableColumns} from "@/components/columns/AiModelInvocationRecordEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";
import {AiModelInvocationStatus} from "@/types/ai/ai.types.ts";
import {getAiModelInvocationStatus} from "@/i18n/enum-helpers.ts";

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
