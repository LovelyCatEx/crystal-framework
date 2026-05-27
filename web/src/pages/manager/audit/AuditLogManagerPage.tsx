import {Input, Select} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {AuditLogManagerController, type ManagerReadAuditLogDTO,} from "@/api/audit/audit-log.api.ts";
import {useEffect, useRef} from "react";
import {useAuditLogTableColumns} from "@/components/columns/AuditLogEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";

export default function AuditLogManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({
        schema: {
            action: 'number',
            userId: 'string',
            username: 'string',
            path: 'string',
            remoteIp: 'string',
            id: 'string',
        }
    });
    const {t} = useTranslation();
    const columns = useAuditLogTableColumns();

    useEffect(() => {
        pageRef?.current?.refreshData?.({ resetPage: true });
    }, [filters.action, filters.userId, filters.username, filters.path, filters.remoteIp, filters.id]);

    const filterableFields = [
        { field: 'id',            type: 'number' as const, label: t('pages.auditLogManager.filter.id') },
        { field: 'user_id',       type: 'number' as const, label: t('pages.auditLogManager.filter.userId') },
        { field: 'username',      type: 'text'   as const, label: t('pages.auditLogManager.filter.username') },
        {
            field: 'action',
            type: 'number' as const,
            label: t('pages.auditLogManager.filter.action'),
            renderValue: ({ value, onChange }: { value: unknown; onChange: (v: unknown) => void }) => (
                <Select
                    className="flex-1"
                    value={value !== undefined ? String(value) : undefined}
                    allowClear
                    placeholder={t('pages.auditLogManager.filter.all')}
                    options={[
                        { value: '1', label: t('pages.auditLogManager.actionType.create') },
                        { value: '2', label: t('pages.auditLogManager.actionType.read') },
                        { value: '3', label: t('pages.auditLogManager.actionType.update') },
                        { value: '4', label: t('pages.auditLogManager.actionType.delete') },
                    ]}
                    onChange={(v) => onChange(v !== undefined ? Number(v) : undefined)}
                />
            ),
        },
        { field: 'path',          type: 'text'   as const, label: t('pages.auditLogManager.filter.path') },
        { field: 'remote_ip',     type: 'text'   as const, label: t('pages.auditLogManager.filter.remoteIp') },
    ];

    return (
        <>
            <ActionBarComponent
                title={t('pages.auditLogManager.title')}
                subtitle={t('pages.auditLogManager.subtitle')}
            />
            <ManagerPageContainer
                ref={pageRef}
                entityName={t('entityNames.auditLog')}
                title=""
                subtitle=""
                showActionBar={false}
                readonlyMode={true}
                showRowActions={false}
                columns={columns}
                editModalFormChildren={<></>}
                filterableFields={filterableFields}
                queryParamsSync={syncToUrl}
                initialQueryValues={initialQueryValues}
                searchKeywords={['username', 'path', 'remote_ip']}
                simpleFilters={[
                    { field: 'id', operator: 'eq', value: filters.id },
                    { field: 'user_id', urlKey: 'userId', operator: 'eq', value: filters.userId },
                    { field: 'username', operator: 'contains', value: filters.username },
                    { field: 'action', operator: 'eq', value: filters.action },
                    { field: 'path', operator: 'contains', value: filters.path },
                    { field: 'remote_ip', urlKey: 'remoteIp', operator: 'contains', value: filters.remoteIp },
                ]}
                query={async (props: ManagerReadAuditLogDTO) => {
                    return (await AuditLogManagerController.query(props)).data!;
                }}
                delete={async () => { return null; }}
                update={async () => { return null; }}
                create={async () => { return null; }}
                tableActions={[
                    {
                        label: <span>{t('pages.auditLogManager.filter.id')}</span>,
                        children: <Input
                            style={{ width: 160 }}
                            placeholder={t('pages.auditLogManager.filter.idPlaceholder')}
                            defaultValue={filters.id}
                            allowClear
                            onPressEnter={(e) => setFilter('id', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('id', undefined);
                            }}
                        />,
                    },
                    {
                        label: <span>{t('pages.auditLogManager.filter.action')}</span>,
                        children: <Select
                            defaultValue={filters.action !== undefined ? String(filters.action) : '-1'}
                            style={{ width: 120 }}
                            options={[
                                { value: '-1', label: t('pages.auditLogManager.filter.all') },
                                { value: '1', label: t('pages.auditLogManager.actionType.create') },
                                { value: '2', label: t('pages.auditLogManager.actionType.read') },
                                { value: '3', label: t('pages.auditLogManager.actionType.update') },
                                { value: '4', label: t('pages.auditLogManager.actionType.delete') },
                            ]}
                            onChange={(value) => setFilter('action', value === '-1' ? undefined : Number.parseInt(value))}
                        />,
                    },
                    {
                        label: <span>{t('pages.auditLogManager.filter.userId')}</span>,
                        children: <Input
                            style={{ width: 140 }}
                            placeholder={t('pages.auditLogManager.filter.userIdPlaceholder')}
                            defaultValue={filters.userId}
                            allowClear
                            onPressEnter={(e) => setFilter('userId', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('userId', undefined);
                            }}
                        />,
                    },
                    {
                        label: <span>{t('pages.auditLogManager.filter.username')}</span>,
                        children: <Input
                            style={{ width: 140 }}
                            placeholder={t('pages.auditLogManager.filter.usernamePlaceholder')}
                            defaultValue={filters.username}
                            allowClear
                            onPressEnter={(e) => setFilter('username', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('username', undefined);
                            }}
                        />,
                    },
                    {
                        label: <span>{t('pages.auditLogManager.filter.path')}</span>,
                        children: <Input
                            style={{ width: 160 }}
                            placeholder={t('pages.auditLogManager.filter.pathPlaceholder')}
                            defaultValue={filters.path}
                            allowClear
                            onPressEnter={(e) => setFilter('path', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('path', undefined);
                            }}
                        />,
                    },
                    {
                        label: <span>{t('pages.auditLogManager.filter.remoteIp')}</span>,
                        children: <Input
                            style={{ width: 140 }}
                            placeholder={t('pages.auditLogManager.filter.remoteIpPlaceholder')}
                            defaultValue={filters.remoteIp}
                            allowClear
                            onPressEnter={(e) => setFilter('remoteIp', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('remoteIp', undefined);
                            }}
                        />,
                    }
                ]}
            >
            </ManagerPageContainer>
        </>
    );
}
