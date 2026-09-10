import {Col, Form, Input, InputNumber, Row, Switch} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    AiUserGroupManagerController,
    type ManagerCreateAiUserGroupDTO,
    type ManagerReadAiUserGroupDTO
} from "@/api/ai/ai-user-group.api.ts";
import {useEffect, useRef} from "react";
import {useAiUserGroupTableColumns} from "@/components/columns/AiUserGroupEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";

export default function AiUserGroupManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({ schema: { id: 'string' } });
    const {t} = useTranslation();
    const columns = useAiUserGroupTableColumns();

    useEffect(() => {
        pageRef.current?.refreshData?.({ resetPage: true });
    }, [filters.id]);

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName={t('entityNames.aiUserGroup')}
            title={t('pages.aiUserGroupManager.title')}
            subtitle={t('pages.aiUserGroupManager.subtitle')}
            columns={columns}
            searchKeywords={['name', 'key', 'description']}
            queryParamsSync={syncToUrl}
            initialQueryValues={initialQueryValues}
            simpleFilters={[
                { field: 'id', operator: 'eq', value: filters.id },
            ]}
            tableActions={[
                {
                    label: <span>ID</span>,
                    children: <Input
                        className="rounded-xl"
                        placeholder="Enter user group ID"
                        defaultValue={filters.id}
                        allowClear
                        onPressEnter={(e) => setFilter('id', (e.target as HTMLInputElement).value || undefined)}
                        onChange={(e) => { if (e.target.value === '') setFilter('id', undefined); }}
                    />,
                },
            ]}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item
                                name="name"
                                label={t('pages.aiUserGroupManager.modal.name.label')}
                                rules={[{ required: true, message: t('pages.aiUserGroupManager.modal.name.required') }]}
                            >
                                <Input placeholder={t('pages.aiUserGroupManager.modal.name.placeholder')} />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item
                                name="key"
                                label={t('pages.aiUserGroupManager.modal.key.label')}
                                rules={[{ required: true, message: t('pages.aiUserGroupManager.modal.key.required') }]}
                            >
                                <Input placeholder={t('pages.aiUserGroupManager.modal.key.placeholder')} />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item name="description" label={t('pages.aiUserGroupManager.modal.description.label')}>
                        <Input.TextArea rows={2} placeholder={t('pages.aiUserGroupManager.modal.description.placeholder')} />
                    </Form.Item>
                    <Form.Item
                        name="billingMultiplier"
                        label={t('pages.aiUserGroupManager.modal.billingMultiplier.label')}
                        rules={[{ required: true, message: t('pages.aiUserGroupManager.modal.billingMultiplier.required') }]}
                        initialValue="1.0"
                    >
                        <Input placeholder={t('pages.aiUserGroupManager.modal.billingMultiplier.placeholder')} />
                    </Form.Item>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item
                                name="enabled"
                                label={t('pages.aiUserGroupManager.modal.enabled.label')}
                                valuePropName="checked"
                                initialValue={true}
                            >
                                <Switch />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item
                                name="isDefault"
                                label={t('pages.aiUserGroupManager.modal.isDefault.label')}
                                valuePropName="checked"
                                initialValue={false}
                            >
                                <Switch />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item
                        name="sort"
                        label={t('pages.aiUserGroupManager.modal.sort.label')}
                        initialValue={0}
                    >
                        <InputNumber className="w-full" placeholder={t('pages.aiUserGroupManager.modal.sort.placeholder')} />
                    </Form.Item>
                </>
            }
            query={async (props: ManagerReadAiUserGroupDTO) => {
                return (await AiUserGroupManagerController.query(props)).data!
            }}
            delete={async (props) => {
                return (await AiUserGroupManagerController.delete(props)).data!
            }}
            update={async (props) => {
                return (await AiUserGroupManagerController.update(props)).data!
            }}
            create={async (props) => {
                return (await AiUserGroupManagerController.create(props as ManagerCreateAiUserGroupDTO)).data!
            }}
        >
        </ManagerPageContainer>
    )
}
