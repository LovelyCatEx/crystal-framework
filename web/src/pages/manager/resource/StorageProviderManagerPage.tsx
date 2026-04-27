import {Col, Form, Input, message, Row, Select, Switch} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    type ManagerCreateStorageProviderDTO,
    type ManagerReadStorageProviderDTO,
    StorageProviderManagerController
} from "@/api/storage-provider.api.ts";
import React, {useEffect, useRef, useState} from "react";
import {StorageProviderType, type StorageProvider} from "@/types/storage-provider.types.ts";
import {StorageProviderConfigEditor} from "@/components/StorageProviderConfigEditor.tsx";
import {useStorageProviderTableColumns} from "@/components/columns/StorageProviderEntityColumns.tsx";
import {useTranslation} from "react-i18next";

export function StorageProviderManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [filterType, setFilterType] = useState<number>()
    const {t} = useTranslation();
    const baseColumns = useStorageProviderTableColumns();

    useEffect(() => {
        pageRef?.current?.refreshData?.({ resetPage: true })
    }, [filterType]);

    const handleStorageProviderActiveChange = (active: boolean, row: StorageProvider) => {
        StorageProviderManagerController
            .update({ id: row.id, active: active })
            .then(() => {
                void message.success(t('pages.storageProviderManager.messages.statusUpdateSuccess'));
                pageRef.current?.refreshData();
            })
            .catch(() => {
                void message.error(t('pages.storageProviderManager.messages.statusUpdateFailed'));
            })
    }

    const columnsWithActive = [...baseColumns];
    columnsWithActive.push({
        title: t('pages.storageProviderManager.columns.active'),
        dataIndex: "active",
        key: "active",
        width: 100,
        render: function (_: unknown, row: StorageProvider): React.ReactNode {
            return <Switch value={row.active} onChange={(active) => handleStorageProviderActiveChange(active, row)} />
        }
    });

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName={t('entityNames.storageProvider')}
            title={t('pages.storageProviderManager.title')}
            subtitle={t('pages.storageProviderManager.subtitle')}
            columns={columnsWithActive}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="name" label={t('pages.storageProviderManager.modal.name.label')} rules={[{ required: true, message: t('pages.storageProviderManager.modal.name.required') }, { max: 64, message: t('pages.storageProviderManager.modal.name.maxLength') }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.storageProviderManager.modal.name.placeholder')} maxLength={64} showCount />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="type" label={t('pages.storageProviderManager.modal.type.label')} rules={[{ required: true, message: t('pages.storageProviderManager.modal.type.required') }]}>
                                <Select
                                    className="w-full rounded-lg h-10 flex items-center"
                                    placeholder={t('pages.storageProviderManager.modal.type.placeholder')}
                                    options={[
                                        {
                                            label: t('pages.storageProviderManager.modal.type.localFileSystem'),
                                            value: StorageProviderType.LOCAL_FILE_SYSTEM,
                                        },
                                        {
                                            label: t('pages.storageProviderManager.modal.type.aliyunOss'),
                                            value: StorageProviderType.ALIYUN_OSS,
                                        },
                                        {
                                            label: t('pages.storageProviderManager.modal.type.tencentCos'),
                                            value: StorageProviderType.TENCENT_COS,
                                        }
                                    ]}
                                />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item name="description" label={t('pages.storageProviderManager.modal.description.label')} rules={[{ max: 512, message: t('pages.storageProviderManager.modal.description.maxLength') }]}>
                        <Input className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.storageProviderManager.modal.description.placeholder')} maxLength={512} showCount />
                    </Form.Item>
                    <Form.Item name="baseUrl" label={t('pages.storageProviderManager.modal.baseUrl.label')} rules={[{ required: true, message: t('pages.storageProviderManager.modal.baseUrl.required') }, { max: 256, message: t('pages.storageProviderManager.modal.baseUrl.maxLength') }]}>
                        <Input className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.storageProviderManager.modal.baseUrl.placeholder')} maxLength={256} showCount />
                    </Form.Item>
                    <Form.Item name="properties" label={t('pages.storageProviderManager.modal.properties.label')} rules={[{ required: true, message: t('pages.storageProviderManager.modal.properties.required') }]}>
                        <StorageProviderConfigEditor placeholder={t('pages.storageProviderManager.modal.properties.placeholder')} />
                    </Form.Item>
                </>
            }
            query={async (props: ManagerReadStorageProviderDTO) => {
                return (await StorageProviderManagerController.query(props)).data!
            }}
            delete={async (props) => {
                return (await StorageProviderManagerController.delete(props)).data!
            }}
            update={async (props) => {
                return (await StorageProviderManagerController.update(props)).data!
            }}
            create={async (props) => {
                return (await StorageProviderManagerController.create(props as ManagerCreateStorageProviderDTO)).data!
            }}
            tableActions={[
                {
                    label: <span>{t('pages.storageProviderManager.filter.type')}</span>,
                    children: <Select
                        defaultValue="-1"
                        style={{ width: 120 }}
                        options={[
                            { value: '-1', label: t('pages.storageProviderManager.filter.all') },
                            {
                                label: t('pages.storageProviderManager.modal.type.localFileSystem'),
                                value: StorageProviderType.LOCAL_FILE_SYSTEM,
                            },
                            {
                                label: t('pages.storageProviderManager.modal.type.aliyunOss'),
                                value: StorageProviderType.ALIYUN_OSS,
                            },
                            {
                                label: t('pages.storageProviderManager.modal.type.tencentCos'),
                                value: StorageProviderType.TENCENT_COS,
                            }
                        ]}
                        onChange={(value) => setFilterType(Number.parseInt(value))}
                    />,
                    queryParamsProvider() {
                        return {
                            type: filterType === -1 ? undefined : filterType
                        }
                    }
                }
            ]}
        >

        </ManagerPageContainer>
    )
}
