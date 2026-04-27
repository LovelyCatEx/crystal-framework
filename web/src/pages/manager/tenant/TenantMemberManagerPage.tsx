import {Button, Col, Form, Row, Select} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateTenantMemberDTO,
    type ManagerUpdateTenantMemberDTO,
    TenantMemberManagerController,
} from "@/api/tenant-member.api.ts";
import {TenantMemberStatus} from "@/types/tenant-member.types.ts";
import {getTenantMemberStatus} from "@/i18n/enum-helpers.ts";
import {useEffect, useRef, useState} from "react";
import {useTenantMemberTableColumns} from "@/components/columns/TenantMemberEntityColumns.tsx";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {TenantSelectorWithDetail} from "@/components/tenant/TenantSelectorWithDetail.tsx";
import {PlusOutlined} from "@ant-design/icons";
import {UserIdSelector} from "@/components/selector";
import {useTranslation} from "react-i18next";

export function TenantMemberManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [selectedTenantId, setSelectedTenantId] = useState<string | null>(null);
    const [filterStatus, setFilterStatus] = useState<number>();
    const {t} = useTranslation();
    const columns = useTenantMemberTableColumns();

    useEffect(() => {
        pageRef.current?.refreshData({ resetPage: true });
    }, [filterStatus, selectedTenantId]);

    const statusOptions = [
        { label: getTenantMemberStatus(TenantMemberStatus.INACTIVE), value: TenantMemberStatus.INACTIVE },
        { label: getTenantMemberStatus(TenantMemberStatus.DEPARTED), value: TenantMemberStatus.DEPARTED },
        { label: getTenantMemberStatus(TenantMemberStatus.RESIGNED), value: TenantMemberStatus.RESIGNED },
        { label: getTenantMemberStatus(TenantMemberStatus.REVIEWING), value: TenantMemberStatus.REVIEWING },
        { label: getTenantMemberStatus(TenantMemberStatus.ACTIVE), value: TenantMemberStatus.ACTIVE }
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
                title={t('pages.tenantMemberManager.title')}
                subtitle={t('pages.tenantMemberManager.subtitle')}
                titleActions={
                    selectedTenantId ? (
                        <Button
                            type="primary"
                            icon={<PlusOutlined/>}
                            size="large"
                            className="rounded-xl h-12 shadow-lg"
                            onClick={handleOpenAddModal}
                        >
                            {t('pages.tenantMemberManager.action.addNew')}
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
                    entityName={t('entityNames.tenantMember')}
                    title=""
                    subtitle=""
                    showActionBar={false}
                    columns={columns}
                    editModalFormChildren={
                        <>
                            <Row gutter={24}>
                                <Col span={12}>
                                    <Form.Item name="tenantId" hidden>
                                        <input type="hidden" value={selectedTenantId || ''} />
                                    </Form.Item>
                                    <Form.Item
                                        name="memberUserId"
                                        label={t('pages.tenantMemberManager.modal.memberUserId.label')}
                                        rules={[{ required: true, message: t('pages.tenantMemberManager.modal.memberUserId.required') }]}
                                    >
                                        <UserIdSelector />
                                    </Form.Item>
                                </Col>
                                <Col span={12}>
                                    <Form.Item
                                        name="status"
                                        label={t('pages.tenantMemberManager.modal.status.label')}
                                        rules={[{ required: true, message: t('pages.tenantMemberManager.modal.status.required') }]}
                                        initialValue={TenantMemberStatus.ACTIVE}
                                    >
                                        <Select
                                            className="w-full rounded-lg h-10 flex items-center"
                                            placeholder={t('pages.tenantMemberManager.modal.status.placeholder')}
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
                            label: <span>{t('pages.tenantMemberManager.filter.status')}</span>,
                            children: <Select
                                defaultValue="-1"
                                style={{ width: 120 }}
                                options={[
                                    { value: '-1', label: t('pages.tenantMemberManager.filter.all') },
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
