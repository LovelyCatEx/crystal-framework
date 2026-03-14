import {Col, Form, Row, Select, Spin} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateTenantMemberDTO,
    type ManagerUpdateTenantMemberDTO,
    TenantMemberManagerController,
    TenantMemberStatus
} from "@/api/tenant-member.api.ts";
import {tenantMemberStatusToTranslationMap} from "@/i18n/tenant-member.ts";
import {useEffect, useRef, useState} from "react";
import {MY_TENANT_MEMBER_TABLE_COLUMNS} from "@/components/columns/MyTenantMemberEntityColumns.tsx";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {useUserTenants} from "@/compositions/use-tenant.ts";

export function MyTenantMemberManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [filterStatus, setFilterStatus] = useState<number>();
    const { currentTenant, isJoinedTenantsLoading } = useUserTenants();

    const currentTenantId = currentTenant?.tenantId ?? null;

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

    if (isJoinedTenantsLoading) {
        return (
            <>
                <ActionBarComponent title="我的组织成员" subtitle="管理当前组织成员信息" />
                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 256 }}>
                    <Spin size="large" />
                </div>
            </>
        );
    }

    return (
        <>
            <ActionBarComponent
                title="我的组织成员"
                subtitle="管理当前组织成员信息"
            />
            {currentTenantId && (
                <ManagerPageContainer
                    ref={pageRef}
                    className="mt-4"
                    entityName="组织成员"
                    title=""
                    subtitle=""
                    showActionBar={false}
                    columns={MY_TENANT_MEMBER_TABLE_COLUMNS}
                    editModalFormChildren={
                        <>
                            <Row gutter={12}>
                                <Col span={12}>
                                    <Form.Item name="tenantId" hidden>
                                        <input type="hidden" value={currentTenantId || ''} />
                                    </Form.Item>
                                    <Form.Item
                                        name="memberUserId"
                                        hidden
                                    >
                                        <input type="hidden" />
                                    </Form.Item>
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
                            tenantId: currentTenantId
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
                            tenantId: currentTenantId
                        };
                        return (await TenantMemberManagerController.create(createProps)).data!
                    }}
                />
            )}
        </>
    )
}
