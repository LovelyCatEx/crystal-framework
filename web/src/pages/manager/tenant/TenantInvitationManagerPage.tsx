import {Button, Col, DatePicker, Form, InputNumber, message, Row, Switch, Tooltip} from "antd";
import dayjs from "dayjs";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateInvitationDTO,
    type ManagerUpdateInvitationDTO,
    InvitationManagerController
} from "@/api/invitation.api.ts";
import {useRef, useState} from "react";
import {useTenantInvitationTableColumns} from "@/components/columns/TenantInvitationEntityColumns.tsx";
import {ActionBarComponent} from "@/components/ActionBarComponent.tsx";
import {TenantSelectorWithDetail} from "@/components/tenant/TenantSelectorWithDetail.tsx";
import {PlusOutlined, LinkOutlined} from "@ant-design/icons";
import {TenantDepartmentIdSelector, TenantMemberIdSelector} from "@/components/selector";
import {useTranslation} from "react-i18next";

export function TenantInvitationManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [selectedTenantId, setSelectedTenantId] = useState<string | null>(null);
    const {t} = useTranslation();
    const columns = useTenantInvitationTableColumns();

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
                title={t('pages.tenantInvitationManager.title')}
                subtitle={t('pages.tenantInvitationManager.subtitle')}
                titleActions={
                    selectedTenantId ? (
                        <Button
                            type="primary"
                            icon={<PlusOutlined/>}
                            size="large"
                            className="rounded-xl h-12 shadow-lg"
                            onClick={handleOpenAddModal}
                        >
                            {t('pages.tenantInvitationManager.addInvitationCode')}
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
                    entityName={t('entityNames.tenantInvitation')}
                    title=""
                    subtitle=""
                    showActionBar={false}
                    columns={columns}
                    tableRowActionsRender={(row) => (
                        <>
                            <Tooltip title={t('pages.tenantInvitationManager.copyInvitationLink')}>
                                <Button
                                    type="text"
                                    size="small"
                                    icon={<LinkOutlined />}
                                    onClick={async () => {
                                        const host = window.location.origin
                                        const url = `${host}/tenant/invitation?code=${row.invitationCode}`

                                        try {
                                            await navigator.clipboard.writeText(url)
                                            void message.success(t('pages.tenantInvitationManager.copySuccess'))
                                        } catch (err) {
                                            void message.error(t('pages.tenantInvitationManager.copyFailed'))
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
                                        label={t('pages.tenantInvitationManager.modal.creatorMemberId.label')}
                                        rules={[{ required: true, message: t('pages.tenantInvitationManager.modal.creatorMemberId.required') }]}
                                    >
                                        <TenantMemberIdSelector
                                            tenantId={selectedTenantId!}
                                            placeholder={t('pages.tenantInvitationManager.modal.creatorMemberId.placeholder')}
                                        />
                                    </Form.Item>
                                </Col>
                                <Col span={12}>
                                    <Form.Item
                                        name="departmentId"
                                        label={t('pages.tenantInvitationManager.modal.departmentId.label')}
                                    >
                                        <TenantDepartmentIdSelector
                                            tenantId={selectedTenantId!}
                                            placeholder={t('pages.tenantInvitationManager.modal.departmentId.placeholder')}
                                        />
                                    </Form.Item>
                                </Col>
                            </Row>
                            <Row gutter={24}>
                                <Col span={12}>
                                    <Form.Item
                                        name="invitationCount"
                                        label={t('pages.tenantInvitationManager.modal.invitationCount.label')}
                                        rules={[{ required: true, message: t('pages.tenantInvitationManager.modal.invitationCount.required') }]}
                                        initialValue={10}
                                    >
                                        <InputNumber
                                            className="w-full rounded-lg h-10"
                                            placeholder={t('pages.tenantInvitationManager.modal.invitationCount.placeholder')}
                                            min={1}
                                            max={9999}
                                        />
                                    </Form.Item>
                                </Col>
                                <Col span={12}>
                                    <Form.Item
                                        name="expiresTime"
                                        label={t('pages.tenantInvitationManager.modal.expiresTime.label')}
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
                                            placeholder={t('pages.tenantInvitationManager.modal.expiresTime.placeholder')}
                                        />
                                    </Form.Item>
                                </Col>
                            </Row>
                            <Row gutter={24}>
                                <Col span={12}>
                                    <Form.Item
                                        name="requiresReviewing"
                                        label={t('pages.tenantInvitationManager.modal.requiresReviewing.label')}
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
