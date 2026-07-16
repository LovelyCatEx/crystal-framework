import {Col, Form, Input, Row, Select, Switch} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {type ManagerCreatePermissionDTO, type ManagerReadPermissionDTO} from "@/api/user/rbac/user-permission.api.ts";
import type {UserPermission} from "@/types/user/rbac/user-permission.types.ts";
import {PermissionType} from "@/types/user/rbac/user-permission.types.ts";
import {useEffect, useRef, useState} from "react";
import TextArea from "antd/es/input/TextArea";
import {useUserPermissionTableColumns} from "@/components/columns/UserPermissionEntityColumns.tsx";
import {useProtectedController} from "@/components/base/ProtectedControllerWarningWrapper.tsx";
import {useTranslation} from "react-i18next";
import {getPermissionType} from "@/i18n/enum-helpers.ts";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";
import {usePermissionTranslator} from "@/i18n/permission-translations.tsx";

export default function UserPermissionManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({
        schema: { type: 'number', id: 'string' }
    });
    const { controller } = useProtectedController<UserPermission, ManagerCreatePermissionDTO, ManagerReadPermissionDTO>();
    const {t} = useTranslation();
    const [useI18nDescription, setUseI18nDescription] = useState(true);
    const translatePermission = usePermissionTranslator();
    const columns = useUserPermissionTableColumns({
        descriptionRender: (row) => {
            if (!useI18nDescription) return row.description;
            return translatePermission(row.name) ?? row.description;
        },
    });

    useEffect(() => {
        pageRef?.current?.refreshData?.({ resetPage: true })
    }, [filters.type, filters.id]);

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
            searchKeywords={['name', 'description', 'path']}
            filterableFields={[
                { field: 'id', type: 'number' as const, label: t('pages.userPermissionManager.filter.id') },
                {
                    field: 'type',
                    type: 'number' as const,
                    label: t('pages.userPermissionManager.filter.type'),
                    renderValue: ({ value, onChange }) => (
                        <Select
                            className="flex-1"
                            value={value !== undefined ? String(value) : undefined}
                            allowClear
                            placeholder={t('pages.userPermissionManager.filter.all')}
                            options={[
                                { value: String(PermissionType.ACTION), label: getPermissionType(PermissionType.ACTION) },
                                { value: String(PermissionType.MENU), label: getPermissionType(PermissionType.MENU) },
                                { value: String(PermissionType.COMPONENT), label: getPermissionType(PermissionType.COMPONENT) },
                            ]}
                            onChange={(v) => onChange(v !== undefined ? Number(v) : undefined)}
                        />
                    ),
                },
                            ]}
            queryParamsSync={syncToUrl}
            initialQueryValues={initialQueryValues}
            simpleFilters={[
                { field: 'id', operator: 'eq', value: filters.id },
                { field: 'type', operator: 'eq', value: filters.type },
            ]}
            tableActions={[
                {
                    label: <span>{t('pages.userPermissionManager.filter.id')}</span>,
                    children: <Input
                        placeholder={t('pages.userPermissionManager.filter.idPlaceholder')}
                        defaultValue={filters.id}
                        allowClear
                        className="rounded-xl"
                        onPressEnter={(e) => setFilter('id', (e.target as HTMLInputElement).value || undefined)}
                        onChange={(e) => { if (e.target.value === '') setFilter('id', undefined); }}
                    />,
                },
                {
                    label: <span>{t('pages.userPermissionManager.filter.type')}</span>,
                    children: <Select
                        defaultValue={filters.type !== undefined ? String(filters.type) : '-1'}
                        style={{ width: 120 }}
                        options={[
                            { value: '-1', label: t('pages.userPermissionManager.filter.all') },
                            { value: '0', label: getPermissionType(PermissionType.ACTION) },
                            { value: '1', label: getPermissionType(PermissionType.MENU) },
                            { value: '2', label: getPermissionType(PermissionType.COMPONENT) },
                        ]}
                        onChange={(value) => setFilter('type', value === '-1' ? undefined : Number.parseInt(value))}
                    />,
                },
                {
                    label: <span>{t('pages.permissionCatalog.source.label')}</span>,
                    children: <Switch
                        checked={useI18nDescription}
                        checkedChildren={t('pages.permissionCatalog.source.i18n')}
                        unCheckedChildren={t('pages.permissionCatalog.source.db')}
                        onChange={setUseI18nDescription}
                    />,
                },
            ]}
        >

        </ManagerPageContainer>
    )
}
