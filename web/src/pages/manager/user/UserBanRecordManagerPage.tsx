import {Input} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {UserBanRecordManagerController} from "@/api/user/user-ban-record.api.ts";
import type {ManagerReadUserBanRecordDTO} from "@/types/user/user-ban-record.types.ts";
import {useEffect, useRef} from "react";
import {useUserBanRecordTableColumns} from "@/components/columns/UserBanRecordEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";

export default function UserBanRecordManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);

    const {filters, setFilter, syncToUrl, initialQueryValues} = useManagerQueryParams({
        schema: {
            id: 'string',
            userId: 'string',
            operatorUserId: 'string',
        }
    });

    const {t} = useTranslation();
    const columns = useUserBanRecordTableColumns();

    useEffect(() => {
        pageRef?.current?.refreshData?.({resetPage: true});
    }, [filters.id, filters.userId, filters.operatorUserId]);

    const filterableFields = [
        {field: 'id', type: 'number' as const, label: t('pages.userBanRecordManager.filter.id')},
        {field: 'user_id', type: 'number' as const, label: t('pages.userBanRecordManager.filter.userId')},
        {field: 'operator_user_id', type: 'number' as const, label: t('pages.userBanRecordManager.filter.operatorUserId')},
    ];

    return (
        <>
            <ActionBarComponent
                title={t('pages.userBanRecordManager.title')}
                subtitle={t('pages.userBanRecordManager.subtitle')}
            />
            <ManagerPageContainer
                ref={pageRef}
                entityName={t('entityNames.userBanRecord')}
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
                simpleFilters={[
                    {field: 'id', operator: 'eq', value: filters.id},
                    {field: 'user_id', urlKey: 'userId', operator: 'eq', value: filters.userId},
                    {field: 'operator_user_id', urlKey: 'operatorUserId', operator: 'eq', value: filters.operatorUserId},
                ]}
                query={async (props: ManagerReadUserBanRecordDTO) => {
                    return (await UserBanRecordManagerController.query(props)).data!;
                }}
                delete={async () => null}
                update={async () => null}
                create={async () => null}
                tableActions={[
                    {
                        label: <span>{t('pages.userBanRecordManager.filter.id')}</span>,
                        children: <Input
                            style={{width: 160}}
                            placeholder={t('pages.userBanRecordManager.filter.idPlaceholder')}
                            defaultValue={filters.id}
                            allowClear
                            onPressEnter={(e) => setFilter('id', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('id', undefined);
                            }}
                        />,
                    },
                    {
                        label: <span>{t('pages.userBanRecordManager.filter.userId')}</span>,
                        children: <Input
                            style={{width: 140}}
                            placeholder={t('pages.userBanRecordManager.filter.userIdPlaceholder')}
                            defaultValue={filters.userId}
                            allowClear
                            onPressEnter={(e) => setFilter('userId', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('userId', undefined);
                            }}
                        />,
                    },
                    {
                        label: <span>{t('pages.userBanRecordManager.filter.operatorUserId')}</span>,
                        children: <Input
                            style={{width: 140}}
                            placeholder={t('pages.userBanRecordManager.filter.operatorUserIdPlaceholder')}
                            defaultValue={filters.operatorUserId}
                            allowClear
                            onPressEnter={(e) => setFilter('operatorUserId', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('operatorUserId', undefined);
                            }}
                        />,
                    }
                ]}
            />
        </>
    );
}
