import {Col, DatePicker, Form, Input, Row, Select} from "antd";
import dayjs from "dayjs";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    TenantManagerController,
    type ManagerCreateTenantDTO,
    type ManagerReadTenantDTO,
    type ManagerUpdateTenantDTO
} from "@/api/tenant.api.ts";
import {useEffect, useRef, useState} from "react";
import {TENANT_MANAGER_TABLE_COLUMNS} from "@/components/columns/TenantEntityColumns.tsx";
import {TenantTireTypeManagerController} from "@/api/tenant-tire-type.api.ts";
import type {TenantTireType} from "@/types/tenant.types.ts";
import {TenantStatus, TenantStatusMap} from "@/types/tenant.types.ts";
import {JsonEditor} from "@/components/JsonEditor.tsx";
import {UserIdSelector} from "@/components/selector/UserIdSelector.tsx";

export function TenantManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [tireTypes, setTireTypes] = useState<TenantTireType[]>([]);

    useEffect(() => {
        TenantTireTypeManagerController.list().then((res) => {
            setTireTypes(res.data || []);
        });
    }, []);

    const statusOptions = [
        { label: TenantStatusMap[TenantStatus.REVIEWING].label, value: TenantStatus.REVIEWING },
        { label: TenantStatusMap[TenantStatus.ACTIVE].label, value: TenantStatus.ACTIVE },
        { label: TenantStatusMap[TenantStatus.CLOSED].label, value: TenantStatus.CLOSED }
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
            entityName="租户"
            title="租户管理"
            subtitle="管理系统租户信息"
            columns={TENANT_MANAGER_TABLE_COLUMNS}
            editModalInitialValues={initialValues}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="name" label="租户名称" rules={[{ required: true }, { max: 64, message: '租户名称长度不能超过64个字符' }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder="租户名称" maxLength={64} showCount />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="ownerUserId" label="所有者用户" rules={[{ required: true }]}>
                                <UserIdSelector isRowDisabled={(row, value) => row.id === value} />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="tireTypeId" label="套餐类型" rules={[{ required: true }]}>
                                <Select
                                    className="w-full rounded-lg h-10 flex items-center"
                                    placeholder="选择套餐类型"
                                    options={tireTypes.map((type) => ({
                                        label: type.name,
                                        value: type.id,
                                    }))}
                                />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="status" label="状态">
                                <Select
                                    className="w-full rounded-lg h-10 flex items-center"
                                    placeholder="选择状态"
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
                                label="订阅时间" 
                                rules={[{ required: true }]}
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
                                    placeholder="选择订阅时间"
                                />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item 
                                name="expiresTime" 
                                label="过期时间" 
                                rules={[{ required: true }]}
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
                                    placeholder="选择过期时间"
                                />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="contactName" label="联系人姓名" rules={[{ required: true }, { max: 64, message: '联系人姓名长度不能超过64个字符' }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder="联系人姓名" maxLength={64} showCount />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="contactEmail" label="联系人邮箱" rules={[{ required: true }, { max: 256, message: '邮箱长度不能超过256个字符' }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder="联系人邮箱" maxLength={256} showCount />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="contactPhone" label="联系人电话" rules={[{ required: true }, { max: 32, message: '电话长度不能超过32个字符' }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder="联系人电话" maxLength={32} showCount />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="address" label="联系地址" rules={[{ required: true }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder="联系地址" />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item name="description" label="描述" rules={[{ max: 512, message: '描述长度不能超过512个字符' }]}>
                        <Input.TextArea
                            className="w-full rounded-lg"
                            placeholder="租户描述"
                            rows={3}
                            maxLength={512}
                            showCount
                        />
                    </Form.Item>
                    <Form.Item name="settings" label="设置(JSON格式)">
                        <JsonEditor placeholder='{"key": "value"}' />
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
        >
        </ManagerPageContainer>
    )
}
