/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {Input, Select} from "antd";
import {UserOutlined} from "@ant-design/icons";
import {useEffect, useRef, useState} from "react";
import {useTranslation} from "react-i18next";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {TenantSelectorWithDetail} from "@/components/tenant/TenantSelectorWithDetail.tsx";
import {useEconomyTransactionTableColumns} from "@/components/columns/EconomyTransactionEntityColumns.tsx";
import {EconomyTransactionManagerController, type ManagerReadEconomyTransactionDTO} from "@/api/economy/transaction.api.ts";
import {CurrencyManagerController} from "@/api/economy/currency.api.ts";
import {ResourceScope} from "@/types/BaseScopedEntity.ts";
import {EconomyReferenceType, EconomyTransactionType, type CurrencyEntity} from "@/types/economy/economy.types.ts";
import {getEconomyReferenceType, getEconomyTransactionType} from "@/i18n/enum-helpers.ts";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";
import {EntitySelectorModal} from "@/components/selector/EntitySelector.tsx";
import {useTenantMemberTableColumns} from "@/components/columns/TenantMemberEntityColumns.tsx";
import {TenantMemberManagerController} from "@/api/tenant/tenant-member.api.ts";
import type {TenantMemberVO} from "@/types/tenant/tenant-member.types.ts";

export default function TenantEconomyTransactionManagerPage() {
    const {t} = useTranslation();
    const columns = useEconomyTransactionTableColumns();
    const memberColumns = useTenantMemberTableColumns();
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [selectedTenantId, setSelectedTenantId] = useState<string | null>(null);
    const [memberSelectorOpen, setMemberSelectorOpen] = useState(false);
    const [currencies, setCurrencies] = useState<CurrencyEntity[]>([]);

    const {filters, setFilter, syncToUrl, initialQueryValues} = useManagerQueryParams({
        schema: {
            id: 'string',
            requestId: 'string',
            type: 'number',
            memberId: 'string',
            currencyId: 'string',
            referenceType: 'number',
            referenceId: 'string',
            remark: 'string',
        },
    });

    useEffect(() => {
        if (selectedTenantId) {
            pageRef.current?.refreshData({resetPage: true});
        }
    }, [selectedTenantId]);

    useEffect(() => {
        pageRef?.current?.refreshData?.({resetPage: true});
    }, [filters.id, filters.requestId, filters.type, filters.memberId, filters.currencyId, filters.referenceType, filters.referenceId, filters.remark]);

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
        { field: 'id', type: 'number' as const, label: t('pages.tenantTransactionManager.filter.id') },
        { field: 'request_id', type: 'text' as const, label: t('pages.tenantTransactionManager.filter.requestId') },
        { field: 'type', type: 'select' as const, label: t('pages.tenantTransactionManager.filter.type'), options: typeOptions },
        { field: 'owner_id', type: 'number' as const, label: t('pages.tenantTransactionManager.filter.memberId') },
        { field: 'currency_id', type: 'select' as const, label: t('pages.tenantTransactionManager.filter.currencyId'), options: currencies.map((c) => ({ value: c.id, label: `${c.code} (${c.symbol})` })) },
        { field: 'reference_type', type: 'select' as const, label: t('pages.tenantTransactionManager.filter.referenceType'), options: referenceTypeOptions },
        { field: 'reference_id', type: 'number' as const, label: t('pages.tenantTransactionManager.filter.referenceId') },
        { field: 'remark', type: 'text' as const, label: t('pages.tenantTransactionManager.filter.remark') },
    ];

    return (
        <>
            <ActionBarComponent
                title={t('pages.tenantTransactionManager.title')}
                subtitle={t('pages.tenantTransactionManager.subtitle')}
            />
            <TenantSelectorWithDetail
                value={selectedTenantId}
                onChange={setSelectedTenantId}
            />
            {selectedTenantId && (
                <>
                    <ManagerPageContainer
                        ref={pageRef}
                        className="mt-4"
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
                            { field: 'owner_id', urlKey: 'memberId', operator: 'eq', value: filters.memberId },
                            { field: 'currency_id', urlKey: 'currencyId', operator: 'eq', value: filters.currencyId },
                            { field: 'reference_type', operator: 'eq', value: filters.referenceType },
                            { field: 'reference_id', urlKey: 'referenceId', operator: 'eq', value: filters.referenceId },
                            { field: 'remark', operator: 'contains', value: filters.remark },
                        ]}
                        tableActions={[
                            {
                                label: <span>{t('pages.tenantTransactionManager.filter.id')}</span>,
                                children: <Input
                                    style={{width: 160}}
                                    placeholder={t('pages.tenantTransactionManager.filter.idPlaceholder')}
                                    defaultValue={filters.id}
                                    allowClear
                                    onPressEnter={(e) => setFilter('id', (e.target as HTMLInputElement).value || undefined)}
                                    onChange={(e) => {
                                        if (e.target.value === '') setFilter('id', undefined);
                                    }}
                                />,
                            },
                            {
                                label: <span>{t('pages.tenantTransactionManager.filter.requestId')}</span>,
                                children: <Input
                                    style={{width: 180}}
                                    placeholder={t('pages.tenantTransactionManager.filter.requestIdPlaceholder')}
                                    defaultValue={filters.requestId}
                                    allowClear
                                    onPressEnter={(e) => setFilter('requestId', (e.target as HTMLInputElement).value || undefined)}
                                    onChange={(e) => {
                                        if (e.target.value === '') setFilter('requestId', undefined);
                                    }}
                                />,
                            },
                            {
                                label: <span>{t('pages.tenantTransactionManager.filter.type')}</span>,
                                children: <Select
                                    style={{width: 120}}
                                    placeholder={t('pages.tenantTransactionManager.filter.typePlaceholder')}
                                    value={filters.type}
                                    allowClear
                                    options={typeOptions}
                                    onChange={(v) => setFilter('type', v ?? undefined)}
                                />,
                            },
                            {
                                label: <span>{t('pages.tenantTransactionManager.filter.memberId')}</span>,
                                children: <Input
                                    key={filters.memberId ?? '__empty__'}
                                    style={{width: 220}}
                                    placeholder={t('pages.tenantTransactionManager.filter.memberIdPlaceholder')}
                                    defaultValue={filters.memberId}
                                    allowClear
                                    suffix={
                                        <UserOutlined
                                            className="cursor-pointer text-gray-400 hover:text-gray-600"
                                            onClick={() => setMemberSelectorOpen(true)}
                                        />
                                    }
                                    onPressEnter={(e) => setFilter('memberId', (e.target as HTMLInputElement).value || undefined)}
                                    onChange={(e) => {
                                        if (e.target.value === '') setFilter('memberId', undefined);
                                    }}
                                />,
                            },
                            {
                                label: <span>{t('pages.tenantTransactionManager.filter.currencyId')}</span>,
                                children: <Select
                                    style={{width: 160}}
                                    placeholder={t('pages.tenantTransactionManager.filter.currencyPlaceholder')}
                                    value={filters.currencyId}
                                    allowClear
                                    options={currencies.map((c) => ({ value: c.id, label: `${c.code} (${c.symbol})` }))}
                                    onChange={(v) => setFilter('currencyId', v ?? undefined)}
                                />,
                            },
                            {
                                label: <span>{t('pages.tenantTransactionManager.filter.referenceType')}</span>,
                                children: <Select
                                    style={{width: 120}}
                                    placeholder={t('pages.tenantTransactionManager.filter.referenceTypePlaceholder')}
                                    value={filters.referenceType}
                                    allowClear
                                    options={referenceTypeOptions}
                                    onChange={(v) => setFilter('referenceType', v ?? undefined)}
                                />,
                            },
                            {
                                label: <span>{t('pages.tenantTransactionManager.filter.referenceId')}</span>,
                                children: <Input
                                    style={{width: 160}}
                                    placeholder={t('pages.tenantTransactionManager.filter.referenceIdPlaceholder')}
                                    defaultValue={filters.referenceId}
                                    allowClear
                                    onPressEnter={(e) => setFilter('referenceId', (e.target as HTMLInputElement).value || undefined)}
                                    onChange={(e) => {
                                        if (e.target.value === '') setFilter('referenceId', undefined);
                                    }}
                                />,
                            },
                            {
                                label: <span>{t('pages.tenantTransactionManager.filter.remark')}</span>,
                                children: <Input
                                    style={{width: 160}}
                                    placeholder={t('pages.tenantTransactionManager.filter.remarkPlaceholder')}
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
                            const dto: ManagerReadEconomyTransactionDTO = { ...props, scope: ResourceScope.TENANT, scopeId: selectedTenantId };
                            return (await EconomyTransactionManagerController.query(dto)).data!
                        }}
                        delete={async () => null}
                        update={async () => null}
                        create={async () => null}
                    />
                    <EntitySelectorModal<TenantMemberVO>
                        type="radio"
                        visible={memberSelectorOpen}
                        entityName={t('entityNames.tenantMember')}
                        columns={memberColumns}
                        query={async (props) => (await TenantMemberManagerController.query({...props, tenantId: selectedTenantId})).data!}
                        onCancel={() => setMemberSelectorOpen(false)}
                        onOk={(selected) => {
                            setFilter('memberId', selected.length > 0 ? selected[0].id : undefined);
                            setMemberSelectorOpen(false);
                        }}
                    />
                </>
            )}
        </>
    );
}
