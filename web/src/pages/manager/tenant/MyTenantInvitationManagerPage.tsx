import {Button, Col, DatePicker, Form, InputNumber, message, Row, Spin, Switch, Tooltip} from "antd";
import dayjs from "dayjs";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    InvitationManagerController,
    type ManagerCreateInvitationDTO,
    type ManagerUpdateInvitationDTO
} from "@/api/invitation.api.ts";
import {useRef} from "react";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {useUserTenants} from "@/compositions/use-tenant.ts";
import {ApartmentOutlined, LinkOutlined, PlusOutlined} from "@ant-design/icons";
import {EntityIdSelector} from "@/components/selector";
import {TENANT_DEPARTMENT_TABLE_COLUMNS} from "@/components/columns/TenantDepartmentEntityColumns.tsx";
import {TenantDepartmentManagerController} from "@/api/tenant-department.api.ts";
import type {TenantDepartment} from "@/types/tenant-department.types.ts";
import {TENANT_INVITATION_TABLE_COLUMNS} from "@/components/columns/TenantInvitationEntityColumns.tsx";

export function MyTenantInvitationManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const { currentTenant, isJoinedTenantsLoading } = useUserTenants();
    const currentTenantId = currentTenant?.tenantId ?? null;

    const handleOpenAddModal = () => {
        pageRef.current?.openModal();
    };

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

    if (isJoinedTenantsLoading) {
        return (
            <>
                <ActionBarComponent title="我的组织邀请码" subtitle="管理当前组织的邀请码" />
                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 256 }}>
                    <Spin size="large" />
                </div>
            </>
        );
    }

    return (
        <>
            <ActionBarComponent
                title="我的组织邀请码"
                subtitle="管理当前组织的邀请码"
                titleActions={
                    currentTenantId ? (
                        <Button
                            type="primary"
                            icon={<PlusOutlined/>}
                            size="large"
                            className="rounded-xl h-12 shadow-lg"
                            onClick={handleOpenAddModal}
                        >
                            新增邀请码
                        </Button>
                    ) : null
                }
            />
            {currentTenantId && (
                <ManagerPageContainer
                    ref={pageRef}
                    className="mt-4"
                    entityName="邀请码"
                    title=""
                    subtitle=""
                    showActionBar={false}
                    columns={TENANT_INVITATION_TABLE_COLUMNS}
                    tableRowActionsRender={(row) => (
                        <>
                            <Tooltip title="复制邀请码链接">
                                <Button
                                    type="text"
                                    size="small"
                                    icon={<LinkOutlined />}
                                    onClick={async () => {
                                        const host = window.location.origin
                                        const url = `${host}/tenant/invitation?code=${row.invitationCode}`

                                        try {
                                            await navigator.clipboard.writeText(url)
                                            message.success("已将邀请链接复制到剪切板")
                                        } catch (err) {
                                            message.error("复制失败，请手动复制")
                                            console.error('复制失败:', err)
                                        }
                                    }}
                                />
                            </Tooltip>
                        </>
                    )}
                    editModalFormChildren={
                        <>
                            <Row gutter={24}>
                                <Col span={0}>
                                    <Form.Item name="tenantId" hidden>
                                        <input type="hidden" value={currentTenantId || ''} />
                                    </Form.Item>
                                    <Form.Item
                                        name="creatorMemberId"
                                        hidden
                                    >
                                        <input type="hidden" value={'0'} />
                                    </Form.Item>
                                </Col>
                                <Col span={12}>
                                    <Form.Item
                                        name="departmentId"
                                        label="部门（可选）"
                                    >
                                        <EntityIdSelector<TenantDepartment>
                                            entityName="租户部门"
                                            columns={TENANT_DEPARTMENT_TABLE_COLUMNS}
                                            controller={TenantDepartmentManagerController}
                                            displayRender={(dept) => `${dept.name}${dept.description ? ` (${dept.description})` : ''}`}
                                            placeholder="选择部门（可选）"
                                            icon={<ApartmentOutlined />}
                                            additionalQueryParams={() => ({ tenantId: currentTenantId! })}
                                        />
                                    </Form.Item>
                                </Col>
                                <Col span={12}>
                                    <Form.Item
                                        name="requiresReviewing"
                                        label="需要审核"
                                        valuePropName="checked"
                                        initialValue={false}
                                    >
                                        <Switch />
                                    </Form.Item>
                                </Col>
                            </Row>
                            <Row gutter={24}>
                                <Col span={12}>
                                    <Form.Item
                                        name="invitationCount"
                                        label="可邀请次数"
                                        rules={[{ required: true, message: '请输入可邀请次数' }]}
                                        initialValue={10}
                                    >
                                        <InputNumber
                                            className="w-full rounded-lg h-10"
                                            placeholder="输入可邀请次数"
                                            min={1}
                                            max={1000}
                                        />
                                    </Form.Item>
                                </Col>
                                <Col span={12}>
                                    <Form.Item
                                        name="expiresTime"
                                        label="过期时间（可选）"
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
                                            placeholder="选择过期时间（可选）"
                                        />
                                    </Form.Item>
                                </Col>
                            </Row>
                        </>
                    }
                    query={async (props) => {
                        return (await InvitationManagerController.query({
                            ...props,
                            tenantId: currentTenantId
                        })).data!
                    }}
                    delete={async (props) => {
                        return (await InvitationManagerController.delete(props)).data!
                    }}
                    update={async (props: ManagerUpdateInvitationDTO) => {
                        const updateProps: ManagerUpdateInvitationDTO = {
                            ...props,
                            expiresTime: convertDateToTimestamp(props.expiresTime)
                        };
                        return (await InvitationManagerController.update(updateProps)).data!
                    }}
                    create={async (props) => {
                        const values = props as unknown as ManagerCreateInvitationDTO;
                        const createProps: ManagerCreateInvitationDTO = {
                            ...values,
                            tenantId: currentTenantId,
                            expiresTime: convertDateToTimestamp(values.expiresTime)
                        };
                        return (await InvitationManagerController.create(createProps)).data!
                    }}
                />
            )}
        </>
    )
}
