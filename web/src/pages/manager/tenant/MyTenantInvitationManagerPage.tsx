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
import {LinkOutlined, PlusOutlined} from "@ant-design/icons";
import {TenantDepartmentIdSelector} from "@/components/selector";
import {useTenantInvitationTableColumns} from "@/components/columns/TenantInvitationEntityColumns.tsx";
import {useTranslation} from "react-i18next";

export function MyTenantInvitationManagerPage() {
    const { t } = useTranslation();
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const { currentTenant, isJoinedTenantsLoading } = useUserTenants();
    const currentTenantId = currentTenant?.tenantId ?? null;
    const columns = useTenantInvitationTableColumns();

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
                <ActionBarComponent title={t('pages.myTenantInvitationManager.title')} subtitle={t('pages.myTenantInvitationManager.subtitle')} />
                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 256 }}>
                    <Spin size="large" />
                </div>
            </>
        );
    }

    return (
        <>
            <ActionBarComponent
                title={t('pages.myTenantInvitationManager.title')}
                subtitle={t('pages.myTenantInvitationManager.subtitle')}
                titleActions={
                    currentTenantId ? (
                        <Button
                            type="primary"
                            icon={<PlusOutlined/>}
                            size="large"
                            className="rounded-xl h-12 shadow-lg"
                            onClick={handleOpenAddModal}
                        >
                            {t('pages.myTenantInvitationManager.action.addNew')}
                        </Button>
                    ) : null
                }
            />
            {currentTenantId && (
                <ManagerPageContainer
                    ref={pageRef}
                    className="mt-4"
                    entityName={t('entityNames.tenantInvitation')}
                    title=""
                    subtitle=""
                    showActionBar={false}
                    columns={columns}
                    tableRowActionsRender={(row) => (
                        <>
                            <Tooltip title={t('pages.myTenantInvitationManager.action.copyLinkTooltip')}>
                                <Button
                                    type="text"
                                    size="small"
                                    icon={<LinkOutlined />}
                                    onClick={async () => {
                                        const host = window.location.origin
                                        const url = `${host}/tenant/invitation?code=${row.invitationCode}`

                                        try {
                                            await navigator.clipboard.writeText(url)
                                            message.success(t('pages.myTenantInvitationManager.messages.copySuccess'))
                                        } catch (err) {
                                            message.error(t('pages.myTenantInvitationManager.messages.copyFailed'))
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
                                        label={t('pages.myTenantInvitationManager.modal.departmentId.label')}
                                    >
                                        <TenantDepartmentIdSelector
                                            tenantId={currentTenantId}
                                            placeholder={t('pages.myTenantInvitationManager.modal.departmentId.placeholder')}
                                        />
                                    </Form.Item>
                                </Col>
                                <Col span={12}>
                                    <Form.Item
                                        name="requiresReviewing"
                                        label={t('pages.myTenantInvitationManager.modal.requiresReviewing.label')}
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
                                        label={t('pages.myTenantInvitationManager.modal.invitationCount.label')}
                                        rules={[{ required: true, message: t('pages.myTenantInvitationManager.modal.invitationCount.required') }]}
                                        initialValue={10}
                                    >
                                        <InputNumber
                                            className="w-full rounded-lg h-10"
                                            placeholder={t('pages.myTenantInvitationManager.modal.invitationCount.placeholder')}
                                            min={1}
                                            max={1000}
                                        />
                                    </Form.Item>
                                </Col>
                                <Col span={12}>
                                    <Form.Item
                                        name="expiresTime"
                                        label={t('pages.myTenantInvitationManager.modal.expiresTime.label')}
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
                                            placeholder={t('pages.myTenantInvitationManager.modal.expiresTime.placeholder')}
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
