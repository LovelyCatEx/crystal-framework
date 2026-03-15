import {Button, Col, DatePicker, Form, InputNumber, message, Row, Switch, Tooltip} from "antd";
import dayjs from "dayjs";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateInvitationDTO,
    type ManagerUpdateInvitationDTO,
    InvitationManagerController
} from "@/api/invitation.api.ts";
import {useRef, useState} from "react";
import {TENANT_INVITATION_TABLE_COLUMNS} from "@/components/columns/TenantInvitationEntityColumns.tsx";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {TenantSelectorWithDetail} from "@/components/tenant/TenantSelectorWithDetail.tsx";
import {PlusOutlined, LinkOutlined} from "@ant-design/icons";
import {TenantDepartmentIdSelector, TenantMemberIdSelector} from "@/components/selector";

export function TenantInvitationManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [selectedTenantId, setSelectedTenantId] = useState<string | null>(null);

    const handleTenantChange = (tenantId: string | null) => {
        setSelectedTenantId(tenantId);
    };

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

    return (
        <>
            <ActionBarComponent
                title="邀请码管理"
                subtitle="管理租户邀请码"
                titleActions={
                    selectedTenantId ? (
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
            <TenantSelectorWithDetail
                value={selectedTenantId}
                onChange={handleTenantChange}
            />
            {selectedTenantId && (
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
                                <Col span={12}>
                                    <Form.Item name="tenantId" hidden>
                                        <input type="hidden" value={selectedTenantId || ''} />
                                    </Form.Item>
                                    <Form.Item
                                        name="creatorMemberId"
                                        label="创建者成员"
                                        rules={[{ required: true, message: '请选择创建者成员' }]}
                                    >
                                        <TenantMemberIdSelector
                                            tenantId={selectedTenantId!}
                                            placeholder="选择创建者成员"
                                        />
                                    </Form.Item>
                                </Col>
                                <Col span={12}>
                                    <Form.Item
                                        name="departmentId"
                                        label="部门（可选）"
                                    >
                                        <TenantDepartmentIdSelector
                                            tenantId={selectedTenantId!}
                                            placeholder="选择部门（可选）"
                                        />
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
                                            max={9999}
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
                            <Row gutter={24}>
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
                        </>
                    }
                    query={async (props) => {
                        return (await InvitationManagerController.query({
                            ...props,
                            tenantId: selectedTenantId
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
                            tenantId: selectedTenantId,
                            expiresTime: convertDateToTimestamp(values.expiresTime)
                        };
                        return (await InvitationManagerController.create(createProps)).data!
                    }}
                />
            )}
        </>
    )
}
