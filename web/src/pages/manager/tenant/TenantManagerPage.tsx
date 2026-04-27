import {Col, DatePicker, Form, Input, Row, Select} from "antd";
import dayjs from "dayjs";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateTenantDTO,
    type ManagerReadTenantDTO,
    type ManagerUpdateTenantDTO,
    TenantManagerController
} from "@/api/tenant.api.ts";
import {useEffect, useRef, useState} from "react";
import {useTenantTableColumns} from "@/components/columns/TenantEntityColumns.tsx";
import {TenantTireTypeManagerController} from "@/api/tenant-tire-type.api.ts";
import type {TenantTireType} from "@/types/tenant.types.ts";
import {TenantStatus} from "@/types/tenant.types.ts";
import {JsonEditor} from "@/components/JsonEditor.tsx";
import {UserIdSelector} from "@/components/selector/UserIdSelector.tsx";
import {getTenantStatus} from "@/i18n/enum-helpers.ts";
import {useTranslation} from "react-i18next";

export function TenantManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [tireTypes, setTireTypes] = useState<TenantTireType[]>([]);
    const [filterStatus, setFilterStatus] = useState<number>();
    const {t} = useTranslation();
    const columns = useTenantTableColumns();

    useEffect(() => {
        TenantTireTypeManagerController.list().then((res) => {
            setTireTypes(res.data || []);
        });
    }, []);

    useEffect(() => {
        pageRef?.current?.refreshData?.({ resetPage: true });
    }, [filterStatus]);

    const statusOptions = [
        { label: getTenantStatus(TenantStatus.REVIEWING), value: TenantStatus.REVIEWING },
        { label: getTenantStatus(TenantStatus.ACTIVE), value: TenantStatus.ACTIVE },
        { label: getTenantStatus(TenantStatus.CLOSED), value: TenantStatus.CLOSED }
    ];

    const convertDateToTimestamp = (date: unknown): string => {
        if (date) {
            if (typeof date === 'object' && 'isValid' in date && typeof (date as any).isValid === 'function') {
                const dayjsDate = date as any;
                if (dayjsDate.isValid()) {
                    return dayjsDate.valueOf().toString();
                }
            }
            if (typeof date === 'string') {
                return date;
            }
        }
        return '';
    };

    const initialValues = {
        subscribedTime: dayjs().valueOf().toString(),
        expiresTime: dayjs().add(1, 'year').valueOf().toString()
    };

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName={t('entityNames.tenant')}
            title={t('pages.tenantManager.title')}
            subtitle={t('pages.tenantManager.subtitle')}
            columns={columns}
            editModalInitialValues={initialValues}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="name" label={t('pages.tenantManager.modal.name.label')} rules={[{ required: true, message: t('pages.tenantManager.modal.name.required') }, { max: 64, message: t('pages.tenantManager.modal.name.maxLength') }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.tenantManager.modal.name.placeholder')} maxLength={64} showCount />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="ownerUserId" label={t('pages.tenantManager.modal.ownerUserId.label')} rules={[{ required: true, message: t('pages.tenantManager.modal.ownerUserId.required') }]}>
                                <UserIdSelector isRowDisabled={(row, value) => row.id === value} />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="tireTypeId" label={t('pages.tenantManager.modal.tireTypeId.label')} rules={[{ required: true, message: t('pages.tenantManager.modal.tireTypeId.required') }]}>
                                <Select
                                    className="w-full rounded-lg h-10 flex items-center"
                                    placeholder={t('pages.tenantManager.modal.tireTypeId.placeholder')}
                                    options={tireTypes.map((type) => ({
                                        label: type.name,
                                        value: type.id,
                                    }))}
                                />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="status" label={t('pages.tenantManager.modal.status.label')}>
                                <Select
                                    className="w-full rounded-lg h-10 flex items-center"
                                    placeholder={t('pages.tenantManager.modal.status.placeholder')}
                                    options={statusOptions}
                                    allowClear
                                />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item 
                                name="subscribedTime" 
                                label={t('pages.tenantManager.modal.subscribedTime.label')} 
                                rules={[{ required: true, message: t('pages.tenantManager.modal.subscribedTime.required') }]}
                                getValueProps={(value) => {
                                    if (value && typeof value === 'string') {
                                        const timestamp = Number(value);
                                        if (!isNaN(timestamp)) {
                                            return { value: dayjs(timestamp) };
                                        }
                                    }
                                    return { value };
                                }}
                            >
                                <DatePicker
                                    className="w-full rounded-lg h-10 flex items-center"
                                    showTime
                                    format="YYYY-MM-DD HH:mm:ss"
                                    placeholder={t('pages.tenantManager.modal.subscribedTime.placeholder')}
                                />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item 
                                name="expiresTime" 
                                label={t('pages.tenantManager.modal.expiresTime.label')} 
                                rules={[{ required: true, message: t('pages.tenantManager.modal.expiresTime.required') }]}
                                getValueProps={(value) => {
                                    if (value && typeof value === 'string') {
                                        const timestamp = Number(value);
                                        if (!isNaN(timestamp)) {
                                            return { value: dayjs(timestamp) };
                                        }
                                    }
                                    return { value };
                                }}
                            >
                                <DatePicker
                                    className="w-full rounded-lg h-10 flex items-center"
                                    showTime
                                    format="YYYY-MM-DD HH:mm:ss"
                                    placeholder={t('pages.tenantManager.modal.expiresTime.placeholder')}
                                />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="contactName" label={t('pages.tenantManager.modal.contactName.label')} rules={[{ required: true, message: t('pages.tenantManager.modal.contactName.required') }, { max: 64, message: t('pages.tenantManager.modal.contactName.maxLength') }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.tenantManager.modal.contactName.placeholder')} maxLength={64} showCount />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="contactEmail" label={t('pages.tenantManager.modal.contactEmail.label')} rules={[{ required: true, message: t('pages.tenantManager.modal.contactEmail.required') }, { max: 256, message: t('pages.tenantManager.modal.contactEmail.maxLength') }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.tenantManager.modal.contactEmail.placeholder')} maxLength={256} showCount />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="contactPhone" label={t('pages.tenantManager.modal.contactPhone.label')} rules={[{ required: true, message: t('pages.tenantManager.modal.contactPhone.required') }, { max: 32, message: t('pages.tenantManager.modal.contactPhone.maxLength') }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.tenantManager.modal.contactPhone.placeholder')} maxLength={32} showCount />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="address" label={t('pages.tenantManager.modal.address.label')} rules={[{ required: true, message: t('pages.tenantManager.modal.address.required') }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.tenantManager.modal.address.placeholder')} />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item name="description" label={t('pages.tenantManager.modal.description.label')} rules={[{ max: 512, message: t('pages.tenantManager.modal.description.maxLength') }]}>
                        <Input.TextArea
                            className="w-full rounded-lg"
                            placeholder={t('pages.tenantManager.modal.description.placeholder')}
                            rows={3}
                            maxLength={512}
                            showCount
                        />
                    </Form.Item>
                    <Form.Item name="settings" label={t('pages.tenantManager.modal.config.label')}>
                        <JsonEditor placeholder={t('pages.tenantManager.modal.config.placeholder')} />
                    </Form.Item>
                </>
            }
            query={async (props: ManagerReadTenantDTO) => {
                return (await TenantManagerController.query(props)).data!
            }}
            delete={async (props) => {
                return (await TenantManagerController.delete(props)).data!
            }}
            update={async (props: ManagerUpdateTenantDTO) => {
                const updateProps: ManagerUpdateTenantDTO = {
                    ...props,
                    subscribedTime: convertDateToTimestamp(props.subscribedTime),
                    expiresTime: convertDateToTimestamp(props.expiresTime),
                };
                return (await TenantManagerController.update(updateProps)).data!
            }}
            create={async (props) => {
                const values = props as unknown as ManagerCreateTenantDTO;
                const createProps: ManagerCreateTenantDTO = {
                    ...values,
                    subscribedTime: convertDateToTimestamp(values.subscribedTime),
                    expiresTime: convertDateToTimestamp(values.expiresTime),
                };
                return (await TenantManagerController.create(createProps)).data!
            }}
            tableActions={[
                {
                    label: <span>{t('pages.tenantManager.filter.status')}</span>,
                    children: <Select
                        defaultValue="-1"
                        style={{ width: 120 }}
                        options={[
                            { value: '-1', label: t('pages.tenantManager.filter.all') },
                            ...statusOptions
                        ]}
                        onChange={(value) => setFilterStatus(value === '-1' ? undefined : Number.parseInt(value))}
                    />,
                    queryParamsProvider() {
                        return {
                            status: filterStatus
                        };
                    }
                }
            ]}
        >
        </ManagerPageContainer>
    )
}
