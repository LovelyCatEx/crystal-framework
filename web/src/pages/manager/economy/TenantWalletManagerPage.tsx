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
import {useWalletTableColumns} from "@/components/columns/WalletEntityColumns.tsx";
import {WalletManagerController, type ManagerReadWalletDTO} from "@/api/economy/wallet.api.ts";
import {CurrencyManagerController} from "@/api/economy/currency.api.ts";
import {ResourceScope} from "@/types/BaseScopedEntity.ts";
import type {CurrencyEntity} from "@/types/economy/economy.types.ts";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";
import {EntitySelectorModal} from "@/components/selector/EntitySelector.tsx";
import {useTenantMemberTableColumns} from "@/components/columns/TenantMemberEntityColumns.tsx";
import {TenantMemberManagerController} from "@/api/tenant/tenant-member.api.ts";
import type {TenantMemberVO} from "@/types/tenant/tenant-member.types.ts";

export default function TenantWalletManagerPage() {
    const {t} = useTranslation();
    const columns = useWalletTableColumns();
    const memberColumns = useTenantMemberTableColumns();
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [selectedTenantId, setSelectedTenantId] = useState<string | null>(null);
    const [memberSelectorOpen, setMemberSelectorOpen] = useState(false);
    const [currencies, setCurrencies] = useState<CurrencyEntity[]>([]);

    const {filters, setFilter, syncToUrl, initialQueryValues} = useManagerQueryParams({
        schema: {id: 'string', memberId: 'string', currencyId: 'string'},
    });

    useEffect(() => {
        if (selectedTenantId) {
            pageRef.current?.refreshData({resetPage: true});
        }
    }, [selectedTenantId]);

    useEffect(() => {
        pageRef?.current?.refreshData?.({resetPage: true});
    }, [filters.id, filters.memberId, filters.currencyId]);

    useEffect(() => {
        CurrencyManagerController.list().then((res) => {
            if (res.data) setCurrencies(res.data);
        }).catch(() => {
            // Silent fail
        });
    }, []);

    const filterableFields = [
        { field: 'id', type: 'number' as const, label: t('pages.tenantWalletManager.filter.id') },
        { field: 'owner_id', type: 'number' as const, label: t('pages.tenantWalletManager.filter.memberId') },
        { field: 'currency_id', type: 'select' as const, label: t('pages.tenantWalletManager.filter.currencyId'), options: currencies.map((c) => ({ value: c.id, label: `${c.code} (${c.symbol})` })) },
        { field: 'balance', type: 'number' as const, label: t('pages.tenantWalletManager.filter.balance') },
    ];

    return (
        <>
            <ActionBarComponent
                title={t('pages.tenantWalletManager.title')}
                subtitle={t('pages.tenantWalletManager.subtitle')}
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
                        entityName={t('entityNames.wallet')}
                        title=""
                        subtitle=""
                        showActionBar={false}
                        readonlyMode={true}
                        showRowActions={false}
                        columns={columns}
                        filterableFields={filterableFields}
                        simpleFilters={[
                            { field: 'id', operator: 'eq', value: filters.id },
                            { field: 'owner_id', urlKey: 'memberId', operator: 'eq', value: filters.memberId },
                            { field: 'currency_id', urlKey: 'currencyId', operator: 'eq', value: filters.currencyId },
                        ]}
                        tableActions={[
                            {
                                label: <span>{t('pages.tenantWalletManager.filter.id')}</span>,
                                children: <Input
                                    style={{width: 160}}
                                    placeholder={t('pages.tenantWalletManager.filter.idPlaceholder')}
                                    defaultValue={filters.id}
                                    allowClear
                                    onPressEnter={(e) => setFilter('id', (e.target as HTMLInputElement).value || undefined)}
                                    onChange={(e) => {
                                        if (e.target.value === '') setFilter('id', undefined);
                                    }}
                                />,
                            },
                            {
                                label: <span>{t('pages.tenantWalletManager.filter.memberId')}</span>,
                                children: <Input
                                    key={filters.memberId ?? '__empty__'}
                                    style={{width: 220}}
                                    placeholder={t('pages.tenantWalletManager.filter.memberIdPlaceholder')}
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
                                label: <span>{t('pages.tenantWalletManager.filter.currencyId')}</span>,
                                children: <Select
                                    style={{width: 160}}
                                    placeholder={t('pages.tenantWalletManager.filter.currencyPlaceholder')}
                                    value={filters.currencyId}
                                    allowClear
                                    options={currencies.map((c) => ({ value: c.id, label: `${c.code} (${c.symbol})` }))}
                                    onChange={(v) => setFilter('currencyId', v ?? undefined)}
                                />,
                            },
                        ]}
                        queryParamsSync={syncToUrl}
                        initialQueryValues={initialQueryValues}
                        editModalFormChildren={<></>}
                        query={async (props) => {
                            const dto: ManagerReadWalletDTO = { ...props, scope: ResourceScope.TENANT, scopeId: selectedTenantId };
                            return (await WalletManagerController.query(dto)).data!
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
