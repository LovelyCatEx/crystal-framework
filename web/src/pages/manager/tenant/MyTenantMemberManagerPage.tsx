import {Col, Form, Row, Select, Spin} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateTenantMemberDTO,
    type ManagerUpdateTenantMemberDTO,
    TenantMemberManagerController,
} from "@/api/tenant-member.api.ts";
import {TenantMemberStatus} from "@/types/tenant-member.types.ts";
import {getTenantMemberStatus} from "@/i18n/enum-helpers.ts";
import {useEffect, useRef, useState} from "react";
import {useMyTenantMemberTableColumns} from "@/components/columns/MyTenantMemberEntityColumns.tsx";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {useUserTenants} from "@/compositions/use-tenant.ts";
import {useTranslation} from "react-i18next";

export function MyTenantMemberManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [filterStatus, setFilterStatus] = useState<number>();
    const { currentTenant, isJoinedTenantsLoading } = useUserTenants();
    const {t} = useTranslation();
    const columns = useMyTenantMemberTableColumns();

    const currentTenantId = currentTenant?.tenantId ?? null;

    useEffect(() => {
        pageRef?.current?.refreshData?.({ resetPage: true });
    }, [filterStatus]);

    const statusOptions = [
        { label: getTenantMemberStatus(TenantMemberStatus.INACTIVE), value: TenantMemberStatus.INACTIVE },
        { label: getTenantMemberStatus(TenantMemberStatus.DEPARTED), value: TenantMemberStatus.DEPARTED },
        { label: getTenantMemberStatus(TenantMemberStatus.RESIGNED), value: TenantMemberStatus.RESIGNED },
        { label: getTenantMemberStatus(TenantMemberStatus.REVIEWING), value: TenantMemberStatus.REVIEWING },
        { label: getTenantMemberStatus(TenantMemberStatus.ACTIVE), value: TenantMemberStatus.ACTIVE }
    ];

    if (isJoinedTenantsLoading) {
        return (
            <>
                <ActionBarComponent title={t('pages.myTenantMemberManager.title')} subtitle={t('pages.myTenantMemberManager.subtitle')} />
                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 256 }}>
                    <Spin size="large" />
                </div>
            </>
        );
    }

    return (
        <>
            <ActionBarComponent
                title={t('pages.myTenantMemberManager.title')}
                subtitle={t('pages.myTenantMemberManager.subtitle')}
            />
            {currentTenantId && (
                <ManagerPageContainer
                    ref={pageRef}
                    className="mt-4"
                    entityName={t('entityNames.tenantMember')}
                    title=""
                    subtitle=""
                    showActionBar={false}
                    columns={columns}
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
                                        label={t('pages.myTenantMemberManager.modal.status.label')}
                                        rules={[{ required: true, message: t('pages.myTenantMemberManager.modal.status.required') }]}
                                        initialValue={TenantMemberStatus.ACTIVE}
                                    >
                                        <Select
                                            className="w-full rounded-lg h-10 flex items-center"
                                            placeholder={t('pages.myTenantMemberManager.modal.status.placeholder')}
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
                            label: <span>{t('pages.myTenantMemberManager.filter.status')}</span>,
                            children: <Select
                                defaultValue="-1"
                                style={{ width: 120 }}
                                options={[
                                    { value: '-1', label: t('pages.myTenantMemberManager.filter.all') },
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
