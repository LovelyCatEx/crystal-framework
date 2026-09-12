import {Col, Form, Input, InputNumber, Row, Switch, message} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    AiUserGroupManagerController,
    type ManagerCreateAiUserGroupDTO,
    type ManagerReadAiUserGroupDTO
} from "@/api/ai/ai-user-group.api.ts";
import {
    AiUserGroupModelManagerController,
    type ManagerCreateAiUserGroupModelDTO,
    type ManagerReadAiUserGroupModelDTO
} from "@/api/ai/ai-user-group-model.api.ts";
import {
    AiUserGroupMemberManagerController,
    type ManagerCreateAiUserGroupMemberDTO,
    type ManagerReadAiUserGroupMemberDTO
} from "@/api/ai/ai-user-group-member.api.ts";
import {useEffect, useRef, useState} from "react";
import {useAiUserGroupTableColumns} from "@/components/columns/AiUserGroupEntityColumns.tsx";
import {useAiUserGroupModelTableColumns} from "@/components/columns/AiUserGroupModelEntityColumns.tsx";
import {useAiUserGroupMemberTableColumns} from "@/components/columns/AiUserGroupMemberEntityColumns.tsx";
import {useTranslation} from "react-i18next";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";
import {AiModelIdsSelector} from "@/components/selector/AiModelIdsSelector.tsx";
import {UserIdsSelector} from "@/components/selector/UserIdsSelector.tsx";


export default function AiUserGroupManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const modelPageRef = useRef<ManagerPageContainerRef | null>(null);
    const memberPageRef = useRef<ManagerPageContainerRef | null>(null);
    const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({ schema: { id: 'string' } });
    const {t} = useTranslation();
    const columns = useAiUserGroupTableColumns();
    const modelColumns = useAiUserGroupModelTableColumns();
    const memberColumns = useAiUserGroupMemberTableColumns();
    const [selectedUserGroupId, setSelectedUserGroupId] = useState<string | null>(null);

    useEffect(() => {
        pageRef.current?.refreshData?.({ resetPage: true });
    }, [filters.id]);

    useEffect(() => {
        if (selectedUserGroupId) {
            modelPageRef.current?.refreshData?.({ resetPage: true });
            memberPageRef.current?.refreshData?.({ resetPage: true });
        }
    }, [selectedUserGroupId]);

    return (
        <div className="flex flex-col gap-4">
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
                        label: <span>{t('pages.aiUserGroupManager.tableActions.idFilter.label')}</span>,
                        children: <Input
                            className="rounded-xl"
                            placeholder={t('pages.aiUserGroupManager.tableActions.idFilter.placeholder')}
                            defaultValue={filters.id}
                            allowClear
                            onPressEnter={(e) => setFilter('id', (e.target as HTMLInputElement).value || undefined)}
                            onChange={(e) => { if (e.target.value === '') setFilter('id', undefined); }}
                        />,
                    },
                ]}
                tableSelection={{
                    type: 'radio',
                    onChange: (records) => {
                        if (records.length === 1) {
                            setSelectedUserGroupId(records[0].id);
                        } else {
                            setSelectedUserGroupId(null);
                        }
                    }
                }}
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
            />

            {selectedUserGroupId && (
                <>
                    <ManagerPageContainer
                        className="mt-4"
                        ref={modelPageRef}
                        entityName={t('entityNames.aiUserGroupModel')}
                        title={t('pages.aiUserGroupModelManager.title')}
                        subtitle={t('pages.aiUserGroupModelManager.subtitle')}
                        columns={modelColumns}
                        simpleFilters={[
                            { field: 'user_group_id', operator: 'eq', value: selectedUserGroupId },
                        ]}
                        editModalFormChildren={
                            <>
                                <Form.Item
                                    name="modelIds"
                                    label={t('pages.aiUserGroupModelManager.modal.modelId.label')}
                                    rules={[{ required: true, message: t('pages.aiUserGroupModelManager.modal.modelId.required') }]}
                                >
                                    <AiModelIdsSelector />
                                </Form.Item>
                            </>
                        }
                        query={async (props: ManagerReadAiUserGroupModelDTO) => {
                            return (await AiUserGroupModelManagerController.query(props)).data!
                        }}
                        delete={async (props) => {
                            return (await AiUserGroupModelManagerController.delete(props)).data!
                        }}
                        create={async (props) => {
                            const modelIds = (props as any).modelIds as string[];
                            if (!modelIds || modelIds.length === 0) {
                                throw new Error('请选择至少一个模型');
                            }

                            let successCount = 0;
                            let failCount = 0;

                            for (const modelId of modelIds) {
                                try {
                                    await AiUserGroupModelManagerController.create({
                                        userGroupId: String(selectedUserGroupId),
                                        modelId: String(modelId)
                                    } as ManagerCreateAiUserGroupModelDTO);
                                    successCount++;
                                } catch (error) {
                                    failCount++;
                                    console.error(`Failed to create relation for model ${modelId}:`, error);
                                }
                            }

                            if (successCount > 0) {
                                message.success(`成功创建 ${successCount} 条关联`);
                            }
                            if (failCount > 0) {
                                message.warning(`${failCount} 条关联创建失败`);
                            }

                            // Refresh the table
                            modelPageRef.current?.refreshData();

                            // Return a dummy response to satisfy the interface
                            return { success: true } as any;
                        }}
                    />

                    <ManagerPageContainer
                        className="mt-4"
                        ref={memberPageRef}
                        entityName={t('entityNames.aiUserGroupMember')}
                        title={t('pages.aiUserGroupMemberManager.title')}
                        subtitle={t('pages.aiUserGroupMemberManager.subtitle')}
                        columns={memberColumns}
                        simpleFilters={[
                            { field: 'user_group_id', operator: 'eq', value: selectedUserGroupId },
                        ]}
                        editModalFormChildren={
                            <>
                                <Form.Item
                                    name="userIds"
                                    label={t('pages.aiUserGroupMemberManager.modal.userId.label')}
                                    rules={[{ required: true, message: t('pages.aiUserGroupMemberManager.modal.userId.required') }]}
                                >
                                    <UserIdsSelector />
                                </Form.Item>
                            </>
                        }
                        query={async (props: ManagerReadAiUserGroupMemberDTO) => {
                            return (await AiUserGroupMemberManagerController.query(props)).data!
                        }}
                        delete={async (props) => {
                            return (await AiUserGroupMemberManagerController.delete(props)).data!
                        }}
                        create={async (props) => {
                            const userIds = (props as any).userIds as string[];
                            if (!userIds || userIds.length === 0) {
                                throw new Error('请选择至少一个用户');
                            }

                            let successCount = 0;
                            let failCount = 0;

                            for (const userId of userIds) {
                                try {
                                    await AiUserGroupMemberManagerController.create({
                                        userGroupId: String(selectedUserGroupId),
                                        userId: String(userId)
                                    } as ManagerCreateAiUserGroupMemberDTO);
                                    successCount++;
                                } catch (error) {
                                    failCount++;
                                    console.error(`Failed to create relation for user ${userId}:`, error);
                                }
                            }

                            if (successCount > 0) {
                                message.success(`成功创建 ${successCount} 条关联`);
                            }
                            if (failCount > 0) {
                                message.warning(`${failCount} 条关联创建失败`);
                            }

                            // Refresh the table
                            memberPageRef.current?.refreshData();

                            // Return a dummy response to satisfy the interface
                            return { success: true } as any;
                        }}
                    />
                </>
            )}
        </div>
    )
}
