import {Input, Select} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {UserLoginLogManagerController} from "@/api/user/user-login-log.api.ts";
import type {ManagerReadUserLoginLogDTO} from "@/types/user/user-login-log.types.ts";
import {useEffect, useRef} from "react";
import {useUserLoginLogTableColumns} from "@/components/columns/UserLoginLogEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";

export default function UserLoginLogManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);

    const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({
        schema: {
            userId: 'string',
            username: 'string',
            loginMethod: 'number',
            success: 'string',
            remoteIp: 'string',
            id: 'string',
        }
    });

    const {t} = useTranslation();
    const columns = useUserLoginLogTableColumns();

    useEffect(() => {
        pageRef?.current?.refreshData?.({resetPage: true});
    }, [filters.userId, filters.username, filters.loginMethod, filters.success, filters.remoteIp, filters.id]);

    const filterableFields = [
        { field: 'id',            type: 'number' as const, label: t('pages.userLoginLogManager.filter.id') },
        { field: 'user_id',       type: 'number' as const, label: t('pages.userLoginLogManager.filter.userId') },
        { field: 'username',      type: 'text'   as const, label: t('pages.userLoginLogManager.filter.username') },
        {
            field: 'login_method',
            type: 'number' as const,
            label: t('pages.userLoginLogManager.filter.loginMethod'),
            renderValue: ({ value, onChange }: { value: unknown; onChange: (v: unknown) => void }) => (
                <Select
                    className="flex-1"
                    value={value !== undefined ? String(value) : undefined}
                    allowClear
                    placeholder={t('pages.userLoginLogManager.filter.all')}
                    options={[
                        { value: '0', label: t('pages.userLoginLogManager.loginMethod.password') },
                        { value: '1', label: t('pages.userLoginLogManager.loginMethod.oauth2') },
                    ]}
                    onChange={(v) => onChange(v !== undefined ? Number(v) : undefined)}
                />
            ),
        },
        { field: 'success',       type: 'text'   as const, label: t('pages.userLoginLogManager.filter.status') },
        { field: 'remote_ip',     type: 'text'   as const, label: t('pages.userLoginLogManager.filter.remoteIp') },
    ];

    return (
        <>
            <ActionBarComponent
                title={t('pages.userLoginLogManager.title')}
                subtitle={t('pages.userLoginLogManager.subtitle')}
            />
            <ManagerPageContainer
                ref={pageRef}
                entityName={t('entityNames.userLoginLog')}
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
                searchKeywords={['username', 'remote_ip']}
                simpleFilters={[
                    { field: 'id', operator: 'eq', value: filters.id },
                    { field: 'user_id', urlKey: 'userId', operator: 'eq', value: filters.userId },
                    { field: 'username', operator: 'contains', value: filters.username },
                    { field: 'login_method', urlKey: 'loginMethod', operator: 'eq', value: filters.loginMethod },
                    { field: 'success', operator: 'eq', value: filters.success },
                    { field: 'remote_ip', urlKey: 'remoteIp', operator: 'contains', value: filters.remoteIp },
                ]}
                query={async (props: ManagerReadUserLoginLogDTO) => {
                    return (await UserLoginLogManagerController.query(props)).data!;
                }}
                delete={async () => null}
                update={async () => null}
                create={async () => null}
                tableActions={[
                    {
                        label: <span>{t('pages.userLoginLogManager.filter.id')}</span>,
                        children: <Input
                            style={{width: 160}}
                            placeholder={t('pages.userLoginLogManager.filter.idPlaceholder')}
                            defaultValue={filters.id}
                            allowClear
                            onPressEnter={(e) => setFilter('id', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('id', undefined);
                            }}
                        />,
                    },
                    {
                        label: <span>{t('pages.userLoginLogManager.filter.userId')}</span>,
                        children: <Input
                            style={{width: 140}}
                            placeholder={t('pages.userLoginLogManager.filter.userIdPlaceholder')}
                            defaultValue={filters.userId}
                            allowClear
                            onPressEnter={(e) => setFilter('userId', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('userId', undefined);
                            }}
                        />,
                    },
                    {
                        label: <span>{t('pages.userLoginLogManager.filter.username')}</span>,
                        children: <Input
                            style={{width: 140}}
                            placeholder={t('pages.userLoginLogManager.filter.usernamePlaceholder')}
                            defaultValue={filters.username}
                            allowClear
                            onPressEnter={(e) => setFilter('username', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('username', undefined);
                            }}
                        />,
                    },
                    {
                        label: <span>{t('pages.userLoginLogManager.filter.loginMethod')}</span>,
                        children: <Select
                            defaultValue={filters.loginMethod !== undefined ? String(filters.loginMethod) : '-1'}
                            style={{width: 120}}
                            options={[
                                {value: '-1', label: t('pages.userLoginLogManager.filter.all')},
                                {value: '0', label: t('pages.userLoginLogManager.loginMethod.password')},
                                {value: '1', label: t('pages.userLoginLogManager.loginMethod.oauth2')},
                            ]}
                            onChange={(value) => setFilter('loginMethod', value === '-1' ? undefined : Number.parseInt(value))}
                        />,
                    },
                    {
                        label: <span>{t('pages.userLoginLogManager.filter.status')}</span>,
                        children: <Select
                            defaultValue={filters.success ?? 'all'}
                            style={{width: 100}}
                            options={[
                                {value: 'all', label: t('pages.userLoginLogManager.filter.all')},
                                {value: 'true', label: t('pages.userLoginLogManager.filter.success')},
                                {value: 'false', label: t('pages.userLoginLogManager.filter.failed')},
                            ]}
                            onChange={(value) => setFilter('success', value === 'all' ? undefined : value)}
                        />,
                    },
                    {
                        label: <span>{t('pages.userLoginLogManager.filter.remoteIp')}</span>,
                        children: <Input
                            style={{width: 140}}
                            placeholder={t('pages.userLoginLogManager.filter.remoteIpPlaceholder')}
                            defaultValue={filters.remoteIp}
                            allowClear
                            onPressEnter={(e) => setFilter('remoteIp', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('remoteIp', undefined);
                            }}
                        />,
                    }
                ]}
            />
        </>
    );
}
