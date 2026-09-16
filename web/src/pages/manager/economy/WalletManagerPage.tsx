/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {Button, Form, Input, InputNumber, message, Modal, Select} from "antd";
import {UserOutlined} from "@ant-design/icons";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {adjustWallet, WalletManagerController, type ManagerReadWalletDTO} from "@/api/economy/wallet.api.ts";
import {CurrencyManagerController} from "@/api/economy/currency.api.ts";
import {useEffect, useRef, useState} from "react";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";
import {useWalletTableColumns} from "@/components/columns/WalletEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {EconomyTransactionType, type CurrencyEntity} from "@/types/economy/economy.types.ts";
import {ResourceScope} from "@/types/BaseScopedEntity.ts";
import {getEconomyTransactionType} from "@/i18n/enum-helpers.ts";
import {UserIdSelector} from "@/components/selector/UserIdSelector.tsx";
import {EntitySelectorModal} from "@/components/selector/EntitySelector.tsx";
import {useUserTableColumns} from "@/components/columns/UserEntityColumns.tsx";
import {UserManagerController} from "@/api/user/user.api.ts";
import type {User} from "@/types/user/user.types.ts";

export default function WalletManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const {t} = useTranslation();
    const columns = useWalletTableColumns();
    const userColumns = useUserTableColumns();
    const [adjustOpen, setAdjustOpen] = useState(false);
    const [userSelectorOpen, setUserSelectorOpen] = useState(false);
    const [currencies, setCurrencies] = useState<CurrencyEntity[]>([]);
    const [form] = Form.useForm();

    const {filters, setFilter, syncToUrl, initialQueryValues} = useManagerQueryParams({
        schema: {id: 'string', userId: 'string', currencyId: 'string'},
    });

    useEffect(() => {
        pageRef?.current?.refreshData?.({resetPage: true});
    }, [filters.id, filters.userId, filters.currencyId]);

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

    const filterableFields = [
        { field: 'id', type: 'number' as const, label: t('pages.walletManager.filter.id') },
        { field: 'owner_id', type: 'number' as const, label: t('pages.walletManager.filter.userId') },
        { field: 'currency_id', type: 'select' as const, label: t('pages.walletManager.filter.currencyId'), options: currencies.map((c) => ({ value: c.id, label: `${c.code} (${c.symbol})` })) },
        { field: 'balance', type: 'number' as const, label: t('pages.walletManager.filter.balance') },
    ];

    const handleAdjust = async () => {
        const values = await form.validateFields();
        await adjustWallet({
            scope: ResourceScope.SYSTEM,
            scopeId: "0",
            ownerId: String(values.userId),
            currencyId: String(values.currencyId),
            amount: values.amount,
            type: values.type,
            remark: values.remark,
        });
        message.success(t('pages.walletManager.messages.adjustSuccess'));
        setAdjustOpen(false);
        form.resetFields();
        pageRef.current?.refreshData?.({ resetPage: true });
    };

    return (
        <>
            <ActionBarComponent
                title={t('pages.walletManager.title')}
                subtitle={t('pages.walletManager.subtitle')}
                titleActions={
                    <Button type="primary" onClick={() => setAdjustOpen(true)}>
                        {t('pages.walletManager.action.adjust')}
                    </Button>
                }
            />
            <ManagerPageContainer
                ref={pageRef}
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
                    { field: 'owner_id', urlKey: 'userId', operator: 'eq', value: filters.userId },
                    { field: 'currency_id', urlKey: 'currencyId', operator: 'eq', value: filters.currencyId },
                ]}
                tableActions={[
                    {
                        label: <span>{t('pages.walletManager.filter.id')}</span>,
                        children: <Input
                            style={{width: 180}}
                            placeholder={t('pages.walletManager.filter.idPlaceholder')}
                            defaultValue={filters.id}
                            allowClear
                            onPressEnter={(e) => setFilter('id', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => {
                                if (e.target.value === '') setFilter('id', undefined);
                            }}
                        />,
                    },
                    {
                        label: <span>{t('pages.walletManager.filter.userId')}</span>,
                        children: <Input
                            key={filters.userId ?? '__empty__'}
                            style={{width: 240}}
                            placeholder={t('pages.walletManager.filter.userIdPlaceholder')}
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
                        label: <span>{t('pages.walletManager.filter.currencyId')}</span>,
                        children: <Select
                            style={{width: 180}}
                            placeholder={t('pages.walletManager.adjust.currencyPlaceholder')}
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
                    const dto: ManagerReadWalletDTO = { ...props, scope: ResourceScope.SYSTEM };
                    return (await WalletManagerController.query(dto)).data!
                }}
                delete={async () => null}
                update={async () => null}
                create={async () => null}
            />
            <Modal
                title={t('pages.walletManager.action.adjust')}
                open={adjustOpen}
                onOk={handleAdjust}
                onCancel={() => setAdjustOpen(false)}
                destroyOnClose
            >
                <Form form={form} layout="vertical">
                    <Form.Item
                        name="userId"
                        label={t('pages.walletManager.adjust.userId')}
                        rules={[{ required: true, message: t('pages.walletManager.adjust.userIdRequired') }]}
                    >
                        <UserIdSelector />
                    </Form.Item>
                    <Form.Item
                        name="currencyId"
                        label={t('pages.walletManager.adjust.currency')}
                        rules={[{ required: true, message: t('pages.walletManager.adjust.currencyRequired') }]}
                    >
                        <Select
                            placeholder={t('pages.walletManager.adjust.currencyPlaceholder')}
                            options={currencies.map((c) => ({ value: c.id, label: `${c.code} (${c.symbol})` }))}
                        />
                    </Form.Item>
                    <Form.Item
                        name="amount"
                        label={t('pages.walletManager.adjust.amount')}
                        rules={[{ required: true, message: t('pages.walletManager.adjust.amountRequired') }]}
                    >
                        <InputNumber className="w-full" placeholder={t('pages.walletManager.adjust.amountPlaceholder')} />
                    </Form.Item>
                    <Form.Item
                        name="type"
                        label={t('pages.walletManager.adjust.type')}
                        rules={[{ required: true, message: t('pages.walletManager.adjust.typeRequired') }]}
                        initialValue={EconomyTransactionType.RECHARGE}
                    >
                        <Select options={typeOptions} />
                    </Form.Item>
                    <Form.Item name="remark" label={t('pages.walletManager.adjust.remark')}>
                        <Input placeholder={t('pages.walletManager.adjust.remarkPlaceholder')} />
                    </Form.Item>
                </Form>
            </Modal>
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
