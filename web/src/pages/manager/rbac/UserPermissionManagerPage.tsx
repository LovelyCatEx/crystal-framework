import {Col, Form, Input, Row, Select} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreatePermissionDTO,
    type ManagerReadPermissionDTO
} from "@/api/user-permission.api.ts";
import type {UserPermission} from "@/types/user-permission.types.ts";
import {PermissionType} from "@/types/user-permission.types.ts";
import {useEffect, useRef, useState} from "react";
import TextArea from "antd/es/input/TextArea";
import {useUserPermissionTableColumns} from "@/components/columns/UserPermissionEntityColumns.tsx";
import {useProtectedController} from "@/components/ProtectedControllerWarningWrapper.tsx";
import {useTranslation} from "react-i18next";
import {getPermissionType} from "@/i18n/enum-helpers.ts";

export function UserPermissionManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [filterPermissionType, setFilterPermissionType] = useState<number>()
    const { controller } = useProtectedController<UserPermission, ManagerCreatePermissionDTO, ManagerReadPermissionDTO>();
    const {t} = useTranslation();
    const columns = useUserPermissionTableColumns();

    useEffect(() => {
        pageRef?.current?.refreshData?.()
    }, [filterPermissionType]);

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName={t('entityNames.userPermission')}
            title={t('pages.userPermissionManager.title')}
            subtitle={t('pages.userPermissionManager.subtitle')}
            columns={columns}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={8}>
                            <Form.Item name="name" label={t('pages.userPermissionManager.modal.name.label')} rules={[{ required: true, message: t('pages.userPermissionManager.modal.name.required') }, { max: 256, message: t('pages.userPermissionManager.modal.name.maxLength') }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" maxLength={256} showCount />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="type" label={t('pages.userPermissionManager.modal.type.label')} rules={[{ required: true, message: t('pages.userPermissionManager.modal.type.required') }]}>
                                <Select
                                    className="w-full rounded-lg h-10 flex items-center"
                                    placeholder={t('pages.userPermissionManager.modal.type.placeholder')}
                                    options={[
                                        {
                                            label: getPermissionType(PermissionType.ACTION),
                                            value: PermissionType.ACTION,
                                        },
                                        {
                                            label: getPermissionType(PermissionType.MENU),
                                            value: PermissionType.MENU,
                                        },
                                        {
                                            label: getPermissionType(PermissionType.COMPONENT),
                                            value: PermissionType.COMPONENT,
                                        }
                                    ]}
                                />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="path" label={t('pages.userPermissionManager.modal.path.label')} rules={[{ max: 256, message: t('pages.userPermissionManager.modal.path.maxLength') }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" maxLength={256} showCount />
                            </Form.Item>
                        </Col>
                    </Row>

                    <Form.Item name="description" label={t('pages.userPermissionManager.modal.description.label')} rules={[{ max: 512, message: t('pages.userPermissionManager.modal.description.maxLength') }]}>
                        <TextArea rows={2} placeholder={t('pages.userPermissionManager.modal.description.placeholder')} className="rounded-lg" maxLength={512} showCount />
                    </Form.Item>
                </>
            }
            query={async (props: ManagerReadPermissionDTO) => {
                return (await controller.query(props)).data!
            }}
            delete={async (props) => {
                return (await controller.delete(props)).data!
            }}
            update={async (props) => {
                return (await controller.update(props)).data!
            }}
            create={async (props) => {
                return (await controller.create(props as ManagerCreatePermissionDTO)).data!
            }}
            tableActions={[
                {
                    label: <span>{t('pages.userPermissionManager.filter.type')}</span>,
                    children: <Select
                        defaultValue="-1"
                        style={{ width: 120 }}
                        options={[
                            { value: '-1', label: t('pages.userPermissionManager.filter.all') },
                            { value: '0', label: getPermissionType(PermissionType.ACTION) },
                            { value: '1', label: getPermissionType(PermissionType.MENU) },
                            { value: '2', label: getPermissionType(PermissionType.COMPONENT) },
                        ]}
                        onChange={(value) => setFilterPermissionType(Number.parseInt(value))}
                    />,
                    queryParamsProvider() {
                        return {
                            type: filterPermissionType === -1 ? undefined : filterPermissionType
                        }
                    }
                }
            ]}
        >

        </ManagerPageContainer>
    )
}
