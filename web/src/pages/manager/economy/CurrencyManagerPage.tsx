/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {Col, Form, Input, InputNumber, Row, Select, Switch} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    CurrencyManagerController,
    type ManagerCreateCurrencyDTO,
    type ManagerReadCurrencyDTO
} from "@/api/economy/currency.api.ts";
import {useRef} from "react";
import {useCurrencyTableColumns} from "@/components/columns/CurrencyEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {CurrencySymbolPosition} from "@/types/economy/economy.types.ts";
import {getCurrencySymbolPosition} from "@/i18n/enum-helpers.ts";

export default function CurrencyManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const {t} = useTranslation();
    const columns = useCurrencyTableColumns();
    const symbolPositionOptions = [
        { value: CurrencySymbolPosition.PREFIX, label: getCurrencySymbolPosition(CurrencySymbolPosition.PREFIX) },
        { value: CurrencySymbolPosition.SUFFIX, label: getCurrencySymbolPosition(CurrencySymbolPosition.SUFFIX) },
        { value: CurrencySymbolPosition.REPLACE_DECIMAL, label: getCurrencySymbolPosition(CurrencySymbolPosition.REPLACE_DECIMAL) },
    ];

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName={t('entityNames.currency')}
            title={t('pages.currencyManager.title')}
            subtitle={t('pages.currencyManager.subtitle')}
            columns={columns}
            searchKeywords={['code', 'name', 'symbol', 'description']}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item
                                name="code"
                                label={t('pages.currencyManager.modal.code.label')}
                                rules={[{ required: true, message: t('pages.currencyManager.modal.code.required') }]}
                            >
                                <Input placeholder={t('pages.currencyManager.modal.code.placeholder')} />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item
                                name="name"
                                label={t('pages.currencyManager.modal.name.label')}
                                rules={[{ required: true, message: t('pages.currencyManager.modal.name.required') }]}
                            >
                                <Input placeholder={t('pages.currencyManager.modal.name.placeholder')} />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item
                                name="symbol"
                                label={t('pages.currencyManager.modal.symbol.label')}
                                rules={[{ required: true, message: t('pages.currencyManager.modal.symbol.required') }]}
                            >
                                <Input placeholder={t('pages.currencyManager.modal.symbol.placeholder')} />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item
                                name="precision"
                                label={t('pages.currencyManager.modal.precision.label')}
                                initialValue={2}
                            >
                                <InputNumber
                                    className="w-full"
                                    min={0}
                                    max={8}
                                    placeholder={t('pages.currencyManager.modal.precision.placeholder')}
                                />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={8}>
                            <Form.Item
                                name="symbolPosition"
                                label={t('pages.currencyManager.modal.symbolPosition.label')}
                                initialValue={CurrencySymbolPosition.PREFIX}
                            >
                                <Select options={symbolPositionOptions} />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item
                                name="decimalSeparator"
                                label={t('pages.currencyManager.modal.decimalSeparator.label')}
                                initialValue="."
                            >
                                <Input placeholder={t('pages.currencyManager.modal.decimalSeparator.placeholder')} />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item
                                name="thousandsSeparator"
                                label={t('pages.currencyManager.modal.thousandsSeparator.label')}
                                initialValue=","
                            >
                                <Input placeholder={t('pages.currencyManager.modal.thousandsSeparator.placeholder')} />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item name="description" label={t('pages.currencyManager.modal.description.label')}>
                        <Input.TextArea rows={2} placeholder={t('pages.currencyManager.modal.description.placeholder')} />
                    </Form.Item>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item
                                name="enabled"
                                label={t('pages.currencyManager.modal.enabled.label')}
                                valuePropName="checked"
                                initialValue={true}
                            >
                                <Switch />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item
                                name="sort"
                                label={t('pages.currencyManager.modal.sort.label')}
                                initialValue={0}
                            >
                                <InputNumber className="w-full" placeholder={t('pages.currencyManager.modal.sort.placeholder')} />
                            </Form.Item>
                        </Col>
                    </Row>
                </>
            }
            query={async (props: ManagerReadCurrencyDTO) => {
                return (await CurrencyManagerController.query(props)).data!
            }}
            delete={async (props) => {
                return (await CurrencyManagerController.delete(props)).data!
            }}
            update={async (props) => {
                return (await CurrencyManagerController.update(props)).data!
            }}
            create={async (props) => {
                return (await CurrencyManagerController.create(props as ManagerCreateCurrencyDTO)).data!
            }}
        />
    );
}
