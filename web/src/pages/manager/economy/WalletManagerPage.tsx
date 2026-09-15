/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {Button, Form, Input, InputNumber, message, Modal, Select} from "antd";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {adjustWallet, WalletManagerController, type ManagerReadWalletDTO} from "@/api/economy/wallet.api.ts";
import {CurrencyManagerController} from "@/api/economy/currency.api.ts";
import {useEffect, useRef, useState} from "react";
import {useWalletTableColumns} from "@/components/columns/WalletEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {EconomyTransactionType, type CurrencyEntity} from "@/types/economy/economy.types.ts";
import {ResourceScope} from "@/types/BaseScopedEntity.ts";
import {getEconomyTransactionType} from "@/i18n/enum-helpers.ts";
import {UserIdSelector} from "@/components/selector/UserIdSelector.tsx";

export default function WalletManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const {t} = useTranslation();
    const columns = useWalletTableColumns();
    const [adjustOpen, setAdjustOpen] = useState(false);
    const [currencies, setCurrencies] = useState<CurrencyEntity[]>([]);
    const [form] = Form.useForm();

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
        </>
    );
}
