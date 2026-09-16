/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {Input, Select} from "antd";
import {UserOutlined} from "@ant-design/icons";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    EconomyTransactionManagerController,
    type ManagerReadEconomyTransactionDTO
} from "@/api/economy/transaction.api.ts";
import {CurrencyManagerController} from "@/api/economy/currency.api.ts";
import {useEffect, useRef, useState} from "react";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";
import {useEconomyTransactionTableColumns} from "@/components/columns/EconomyTransactionEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {ResourceScope} from "@/types/BaseScopedEntity.ts";
import {EconomyReferenceType, EconomyTransactionType, type CurrencyEntity} from "@/types/economy/economy.types.ts";
import {getEconomyReferenceType, getEconomyTransactionType} from "@/i18n/enum-helpers.ts";
import {EntitySelectorModal} from "@/components/selector/EntitySelector.tsx";
import {useUserTableColumns} from "@/components/columns/UserEntityColumns.tsx";
import {UserManagerController} from "@/api/user/user.api.ts";
import type {User} from "@/types/user/user.types.ts";

export default function EconomyTransactionManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const {t} = useTranslation();
    const columns = useEconomyTransactionTableColumns();
    const userColumns = useUserTableColumns();
    const [userSelectorOpen, setUserSelectorOpen] = useState(false);
    const [currencies, setCurrencies] = useState<CurrencyEntity[]>([]);

    const {filters, setFilter, syncToUrl, initialQueryValues} = useManagerQueryParams({
        schema: {
            id: 'string',
            requestId: 'string',
            type: 'number',
            userId: 'string',
            currencyId: 'string',
            referenceType: 'number',
            referenceId: 'string',
            remark: 'string',
        },
    });

    useEffect(() => {
        pageRef?.current?.refreshData?.({resetPage: true});
    }, [filters.id, filters.requestId, filters.type, filters.userId, filters.currencyId, filters.referenceType, filters.referenceId, filters.remark]);

    useEffect(() => {
        CurrencyManagerController.list().then((res) => {
            if (res.data) setCurrencies(res.data);
        }).catch(() => {
            // Silent fail
        });
    }, []);

    const typeOptions = [
        { value: EconomyTransactionType.RECHARGE, label: getEconomyTransactionType(EconomyTransactionType.RECHARGE) },
        { value: EconomyTransactionType.DEDUCT, label: getEconomyTransactionType(EconomyTransactionType.DEDUCT) },
    ];
    const referenceTypeOptions = [
        { value: EconomyReferenceType.NONE, label: getEconomyReferenceType(EconomyReferenceType.NONE) },
        { value: EconomyReferenceType.AI_INVOCATION, label: getEconomyReferenceType(EconomyReferenceType.AI_INVOCATION) },
    ];

    const filterableFields = [
        { field: 'id', type: 'number' as const, label: t('pages.transactionManager.filter.id') },
        { field: 'request_id', type: 'text' as const, label: t('pages.transactionManager.filter.requestId') },
        { field: 'type', type: 'select' as const, label: t('pages.transactionManager.filter.type'), options: typeOptions },
        { field: 'owner_id', type: 'number' as const, label: t('pages.transactionManager.filter.ownerId') },
        { field: 'currency_id', type: 'select' as const, label: t('pages.transactionManager.filter.currencyId'), options: currencies.map((c) => ({ value: c.id, label: `${c.code} (${c.symbol})` })) },
        { field: 'reference_type', type: 'select' as const, label: t('pages.transactionManager.filter.referenceType'), options: referenceTypeOptions },
        { field: 'reference_id', type: 'number' as const, label: t('pages.transactionManager.filter.referenceId') },
        { field: 'remark', type: 'text' as const, label: t('pages.transactionManager.filter.remark') },
    ];

    return (
        <>
            <ActionBarComponent
                title={t('pages.transactionManager.title')}
                subtitle={t('pages.transactionManager.subtitle')}
            />
            <ManagerPageContainer
                ref={pageRef}
                entityName={t('entityNames.transaction')}
                title=""
                subtitle=""
                showActionBar={false}
                readonlyMode={true}
                showRowActions={false}
                columns={columns}
                filterableFields={filterableFields}
                simpleFilters={[
                    { field: 'id', operator: 'eq', value: filters.id },
                    { field: 'request_id', urlKey: 'requestId', operator: 'contains', value: filters.requestId },
                    { field: 'type', operator: 'eq', value: filters.type },
                    { field: 'owner_id', urlKey: 'userId', operator: 'eq', value: filters.userId },
                    { field: 'currency_id', urlKey: 'currencyId', operator: 'eq', value: filters.currencyId },
                    { field: 'reference_type', operator: 'eq', value: filters.referenceType },
                    { field: 'reference_id', urlKey: 'referenceId', operator: 'eq', value: filters.referenceId },
                    { field: 'remark', operator: 'contains', value: filters.remark },
                ]}
                tableActions={[
                    {
                        label: <span>{t('pages.transactionManager.filter.id')}</span>,
                        children: <Input
                            style={{width: 160}}
                            placeholder={t('pages.transactionManager.filter.idPlaceholder')}
                            defaultValue={filters.id}
                            allowClear
                            onPressEnter={(e) => setFilter('id', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('id', undefined);
                            }}
                        />,
                    },
                    {
                        label: <span>{t('pages.transactionManager.filter.requestId')}</span>,
                        children: <Input
                            style={{width: 180}}
                            placeholder={t('pages.transactionManager.filter.requestIdPlaceholder')}
                            defaultValue={filters.requestId}
                            allowClear
                            onPressEnter={(e) => setFilter('requestId', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('requestId', undefined);
                            }}
                        />,
                    },
                    {
                        label: <span>{t('pages.transactionManager.filter.type')}</span>,
                        children: <Select
                            style={{width: 120}}
                            placeholder={t('pages.transactionManager.filter.typePlaceholder')}
                            value={filters.type}
                            allowClear
                            options={typeOptions}
                            onChange={(v) => setFilter('type', v ?? undefined)}
                        />,
                    },
                    {
                        label: <span>{t('pages.transactionManager.filter.ownerId')}</span>,
                        children: <Input
                            key={filters.userId ?? '__empty__'}
                            style={{width: 200}}
                            placeholder={t('pages.transactionManager.filter.ownerIdPlaceholder')}
                            defaultValue={filters.userId}
                            allowClear
                            suffix={
                                <UserOutlined
                                    className="cursor-pointer text-gray-400 hover:text-gray-600"
                                    onClick={() => setUserSelectorOpen(true)}
                                />
                            }
                            onPressEnter={(e) => setFilter('userId', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('userId', undefined);
                            }}
                        />,
                    },
                    {
                        label: <span>{t('pages.transactionManager.filter.currencyId')}</span>,
                        children: <Select
                            style={{width: 160}}
                            placeholder={t('pages.transactionManager.filter.currencyPlaceholder')}
                            value={filters.currencyId}
                            allowClear
                            options={currencies.map((c) => ({ value: c.id, label: `${c.code} (${c.symbol})` }))}
                            onChange={(v) => setFilter('currencyId', v ?? undefined)}
                        />,
                    },
                    {
                        label: <span>{t('pages.transactionManager.filter.referenceType')}</span>,
                        children: <Select
                            style={{width: 120}}
                            placeholder={t('pages.transactionManager.filter.referenceTypePlaceholder')}
                            value={filters.referenceType}
                            allowClear
                            options={referenceTypeOptions}
                            onChange={(v) => setFilter('referenceType', v ?? undefined)}
                        />,
                    },
                    {
                        label: <span>{t('pages.transactionManager.filter.referenceId')}</span>,
                        children: <Input
                            style={{width: 160}}
                            placeholder={t('pages.transactionManager.filter.referenceIdPlaceholder')}
                            defaultValue={filters.referenceId}
                            allowClear
                            onPressEnter={(e) => setFilter('referenceId', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('referenceId', undefined);
                            }}
                        />,
                    },
                    {
                        label: <span>{t('pages.transactionManager.filter.remark')}</span>,
                        children: <Input
                            style={{width: 160}}
                            placeholder={t('pages.transactionManager.filter.remarkPlaceholder')}
                            defaultValue={filters.remark}
                            allowClear
                            onPressEnter={(e) => setFilter('remark', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('remark', undefined);
                            }}
                        />,
                    },
                ]}
                queryParamsSync={syncToUrl}
                initialQueryValues={initialQueryValues}
                editModalFormChildren={<></>}
                query={async (props) => {
                    const dto: ManagerReadEconomyTransactionDTO = { ...props, scope: ResourceScope.SYSTEM };
                    return (await EconomyTransactionManagerController.query(dto)).data!
                }}
                delete={async () => null}
                update={async () => null}
                create={async () => null}
            />
            <EntitySelectorModal<User>
                type="radio"
                visible={userSelectorOpen}
                entityName={t('entityNames.user')}
                columns={userColumns}
                query={async (props) => (await UserManagerController.query({...props})).data!}
                onCancel={() => setUserSelectorOpen(false)}
                onOk={(selected) => {
                    setFilter('userId', selected.length > 0 ? selected[0].id : undefined);
                    setUserSelectorOpen(false);
                }}
            />
        </>
    );
}
