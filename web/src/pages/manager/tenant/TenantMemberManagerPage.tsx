import {Button, Col, Form, Row, Select} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateTenantMemberDTO,
    type ManagerUpdateTenantMemberDTO,
    TenantMemberManagerController,
    TenantMemberStatus
} from "@/api/tenant-member.api.ts";
import {tenantMemberStatusToTranslationMap} from "@/i18n/tenant-member.ts";
import {useEffect, useRef, useState} from "react";
import {TENANT_MEMBER_TABLE_COLUMNS} from "@/components/columns/TenantMemberEntityColumns.tsx";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {TenantSelectorWithDetail} from "@/components/tenant/TenantSelectorWithDetail.tsx";
import {PlusOutlined} from "@ant-design/icons";
import {UserIdSelector} from "@/components/selector";

export function TenantMemberManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [selectedTenantId, setSelectedTenantId] = useState<string | null>(null);
    const [filterStatus, setFilterStatus] = useState<number>();

    useEffect(() => {
        pageRef?.current?.refreshData?.();
    }, [filterStatus]);

    const statusOptions = [
        { label: tenantMemberStatusToTranslationMap.get(TenantMemberStatus.INACTIVE), value: TenantMemberStatus.INACTIVE },
        { label: tenantMemberStatusToTranslationMap.get(TenantMemberStatus.DEPARTED), value: TenantMemberStatus.DEPARTED },
        { label: tenantMemberStatusToTranslationMap.get(TenantMemberStatus.RESIGNED), value: TenantMemberStatus.RESIGNED },
        { label: tenantMemberStatusToTranslationMap.get(TenantMemberStatus.REVIEWING), value: TenantMemberStatus.REVIEWING },
        { label: tenantMemberStatusToTranslationMap.get(TenantMemberStatus.ACTIVE), value: TenantMemberStatus.ACTIVE }
    ];

    const handleTenantChange = (tenantId: string | null) => {
        setSelectedTenantId(tenantId);
    };

    const handleOpenAddModal = () => {
        pageRef.current?.openModal();
    };

    return (
        <>
            <ActionBarComponent
                title="租户成员管理"
                subtitle="管理租户成员信息"
                titleActions={
                    selectedTenantId ? (
                        <Button
                            type="primary"
                            icon={<PlusOutlined/>}
                            size="large"
                            className="rounded-xl h-12 shadow-lg"
                            onClick={handleOpenAddModal}
                        >
                            新增租户成员
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
                    entityName="租户成员"
                    title=""
                    subtitle=""
                    showActionBar={false}
                    columns={TENANT_MEMBER_TABLE_COLUMNS}
                    editModalFormChildren={
                        <>
                            <Row gutter={24}>
                                <Col span={12}>
                                    <Form.Item name="tenantId" hidden>
                                        <input type="hidden" value={selectedTenantId || ''} />
                                    </Form.Item>
                                    <Form.Item
                                        name="memberUserId"
                                        label="成员用户（仅创建时有效）"
                                        rules={[{ required: true, message: '请选择成员用户' }]}
                                    >
                                        <UserIdSelector />
                                    </Form.Item>
                                </Col>
                                <Col span={12}>
                                    <Form.Item
                                        name="status"
                                        label="状态"
                                        rules={[{ required: true, message: '请选择状态' }]}
                                        initialValue={TenantMemberStatus.ACTIVE}
                                    >
                                        <Select
                                            className="w-full rounded-lg h-10 flex items-center"
                                            placeholder="选择状态"
                                            options={statusOptions}
                                        />
                                    </Form.Item>
                                </Col>
                            </Row>
                        </>
                    }
                    query={async (props) => {
                        return (await TenantMemberManagerController.query({
                            ...props,
                            tenantId: selectedTenantId
                        })).data!
                    }}
                    tableActions={[
                        {
                            label: <span>状态</span>,
                            children: <Select
                                defaultValue="-1"
                                style={{ width: 120 }}
                                options={[
                                    { value: '-1', label: '全部' },
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
                    delete={async (props) => {
                        return (await TenantMemberManagerController.delete(props)).data!
                    }}
                    update={async (props: ManagerUpdateTenantMemberDTO) => {
                        return (await TenantMemberManagerController.update(props)).data!
                    }}
                    create={async (props) => {
                        const createProps: ManagerCreateTenantMemberDTO = {
                            ...(props as ManagerCreateTenantMemberDTO),
                            tenantId: selectedTenantId
                        };
                        return (await TenantMemberManagerController.create(createProps)).data!
                    }}
                />
            )}
        </>
    )
}
