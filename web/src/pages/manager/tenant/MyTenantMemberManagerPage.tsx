import {Col, Form, Input, Row, Select, Spin} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateTenantMemberDTO,
    type ManagerUpdateTenantMemberDTO,
    TenantMemberManagerController,
} from "@/api/tenant/tenant-member.api.ts";
import {TenantMemberStatus} from "@/types/tenant/tenant-member.types.ts";
import {getTenantMemberStatus} from "@/i18n/enum-helpers.ts";
import {useEffect, useRef} from "react";
import {useMyTenantMemberTableColumns} from "@/components/columns/MyTenantMemberEntityColumns.tsx";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {useUserTenants} from "@/compositions/use-tenant.ts";
import {useTranslation} from "react-i18next";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";

export default function MyTenantMemberManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({ schema: { status: 'number', id: 'string', memberUserId: 'string' } });
    const { currentTenant, isJoinedTenantsLoading } = useUserTenants();
    const {t} = useTranslation();
    const columns = useMyTenantMemberTableColumns();

    const currentTenantId = currentTenant?.tenantId ?? null;

    useEffect(() => {
        pageRef?.current?.refreshData?.({ resetPage: true });
    }, [filters.status, filters.id, filters.memberUserId]);

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
                    filterableFields={[
                        { field: 'id', type: 'number' as const, label: t('pages.myTenantMemberManager.filter.id') },
                        {
                            field: 'status',
                            type: 'number' as const,
                            label: t('pages.myTenantMemberManager.filter.status'),
                            renderValue: ({ value, onChange }) => (
                                <Select
                                    className="flex-1"
                                    value={value !== undefined ? String(value) : undefined}
                                    allowClear
                                    placeholder={t('pages.myTenantMemberManager.filter.all')}
                                    options={statusOptions.map(o => ({ label: o.label, value: String(o.value) }))}
                                    onChange={(v) => onChange(v !== undefined ? Number(v) : undefined)}
                                />
                            ),
                        },
                                                    ]}
                    queryParamsSync={syncToUrl}
                    initialQueryValues={initialQueryValues}
                    simpleFilters={[
                        { field: 'status', operator: 'eq', value: filters.status },
                        { field: 'id', operator: 'eq', value: filters.id },
                        { field: 'member_user_id', urlKey: 'memberUserId', operator: 'eq', value: filters.memberUserId },
                    ]}
                    tableActions={[
                        {
                            label: <span>{t('pages.myTenantMemberManager.filter.id')}</span>,
                            children: <Input
                                style={{ width: 160 }}
                                placeholder={t('pages.myTenantMemberManager.filter.idPlaceholder')}
                                defaultValue={filters.id}
                                allowClear
                                onPressEnter={(e) => setFilter('id', (e.target as HTMLInputElement).value || undefined)}
                                onChange={(e) => { if (e.target.value === '') setFilter('id', undefined); }}
                            />,
                        },
                        {
                            label: <span>{t('pages.myTenantMemberManager.filter.memberUserId')}</span>,
                            children: <Input
                                style={{ width: 160 }}
                                placeholder={t('pages.myTenantMemberManager.filter.memberUserIdPlaceholder')}
                                defaultValue={filters.memberUserId}
                                allowClear
                                onPressEnter={(e) => setFilter('memberUserId', (e.target as HTMLInputElement).value || undefined)}
                                onChange={(e) => { if (e.target.value === '') setFilter('memberUserId', undefined); }}
                            />,
                        },
                        {
                            label: <span>{t('pages.myTenantMemberManager.filter.status')}</span>,
                            children: <Select
                                defaultValue={filters.status !== undefined ? String(filters.status) : '-1'}
                                style={{ width: 120 }}
                                options={[
                                    { value: '-1', label: t('pages.myTenantMemberManager.filter.all') },
                                    ...statusOptions
                                ]}
                                onChange={(value) => setFilter('status', value === '-1' ? undefined : Number.parseInt(value))}
                            />,
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
