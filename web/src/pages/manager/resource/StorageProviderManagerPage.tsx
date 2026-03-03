import {Col, Form, Input, message, Row, Select, Space, Switch, Tag} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "../../../components/ManagerPageContainer.tsx";
import {
    type ManagerCreateStorageProviderDTO,
    type ManagerReadStorageProviderDTO,
    StorageProviderManagerController
} from "../../../api/storage-provider.api.ts";
import React, {type JSX, useEffect, useRef, useState} from "react";
import {StorageProviderType, type StorageProvider} from "../../../types/storage-provider.types.ts";
import {CopyableToolTip} from "../../../components/CopyableToolTip.tsx";
import {StorageProviderConfigEditor} from "../../../components/StorageProviderConfigEditor.tsx";

export function StorageProviderManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [filterType, setFilterType] = useState<number>()

    useEffect(() => {
        pageRef?.current?.refreshData?.()
    }, [filterType]);

    const handleStorageProviderActiveChange = (active: boolean, row: StorageProvider) => {
        StorageProviderManagerController
            .update({ id: row.id, active: active })
            .then(() => {
                void message.success("状态更新成功");
                pageRef.current?.refreshData();
            })
            .catch(() => {
                void message.error("状态更新失败");
            })
    }

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName="存储提供商"
            title="存储提供商管理"
            subtitle="管理系统存储提供商配置"
            columns={[
                {
                    title: "名称",
                    dataIndex: "name",
                    key: "name",
                    render: function (_: unknown, row: StorageProvider): React.ReactNode | JSX.Element {
                        return <Space orientation='vertical' size={0}>
                            <CopyableToolTip title={row.name}>
                                <span className="text-xs font-mono font-bold">{row.name}</span>
                            </CopyableToolTip>
                            <CopyableToolTip title={row.id}>
                                <Tag color="blue" className="m-0 text-[10px] leading-4 h-4 px-1 rounded">ID: {row.id}</Tag>
                            </CopyableToolTip>
                        </Space>
                    }
                },
                {
                    title: "类型",
                    dataIndex: "type",
                    key: "type",
                    render: function (_: unknown, row: StorageProvider): React.ReactNode | JSX.Element {
                        return <Space orientation='vertical' size={0}>
                            <CopyableToolTip title={StorageProviderType[row.type]}>
                                <Tag color="orange" className="text-xs font-mono">{StorageProviderType[row.type]}</Tag>
                            </CopyableToolTip>
                        </Space>
                    }
                },
                {
                    title: "描述",
                    dataIndex: "description",
                    key: "description",
                    render: function (_: unknown, row: StorageProvider): React.ReactNode | JSX.Element {
                        return <Space orientation='vertical' size={0}>
                            <CopyableToolTip title={row.description ?? '无描述'}>
                                <span className="text-xs font-mono">{row.description ?? '-'}</span>
                            </CopyableToolTip>
                        </Space>
                    }
                },
                {
                    title: "基础URL",
                    dataIndex: "baseUrl",
                    key: "baseUrl",
                    render: function (_: unknown, row: StorageProvider): React.ReactNode | JSX.Element {
                        return <Space orientation='vertical' size={0}>
                            <CopyableToolTip title={row.baseUrl}>
                                <span className="text-xs font-mono text-blue-600">{row.baseUrl}</span>
                            </CopyableToolTip>
                        </Space>
                    }
                },
                {
                    title: "配置",
                    dataIndex: "properties",
                    key: "properties",
                    render: function (_: unknown, row: StorageProvider): React.ReactNode | JSX.Element {
                        return <CopyableToolTip title={row.properties}>
                            <span className="text-xs font-mono text-gray-500">{row.properties.substring(0, 32)}...</span>
                        </CopyableToolTip>
                    }
                },
                {
                    title: "启用状态",
                    dataIndex: "active",
                    key: "active",
                    width: 100,
                    render: function (_: unknown, row: StorageProvider): React.ReactNode | JSX.Element {
                        return <Switch value={row.active} onChange={(active) => handleStorageProviderActiveChange(active, row)} />
                    }
                }
            ]}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="name" label="名称" rules={[{ required: true }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder="存储提供商名称" />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="type" label="类型" rules={[{ required: true }]}>
                                <Select
                                    className="w-full rounded-lg h-10 flex items-center"
                                    placeholder="选择存储类型"
                                    options={[
                                        {
                                            label: '本地文件系统',
                                            value: StorageProviderType.LOCAL_FILE_SYSTEM,
                                        },
                                        {
                                            label: '阿里云 OSS',
                                            value: StorageProviderType.ALIYUN_OSS,
                                        },
                                        {
                                            label: '腾讯 OSS',
                                            value: StorageProviderType.TENCENT_COS,
                                        }
                                    ]}
                                />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item name="description" label="描述">
                        <Input className="w-full rounded-lg h-10 flex items-center" placeholder="存储提供商描述" />
                    </Form.Item>
                    <Form.Item name="baseUrl" label="基础URL" rules={[{ required: true }]}>
                        <Input className="w-full rounded-lg h-10 flex items-center" placeholder="访问基础URL" />
                    </Form.Item>
                    <Form.Item name="properties" label="配置属性(JSON)" rules={[{ required: true }]}>
                        <StorageProviderConfigEditor placeholder="输入JSON格式的配置属性..." />
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
                    label: <span>类型</span>,
                    children: <Select
                        defaultValue="-1"
                        style={{ width: 120 }}
                        options={[
                            { value: '-1', label: '全部' },
                            {
                                label: '本地文件系统',
                                value: StorageProviderType.LOCAL_FILE_SYSTEM,
                            },
                            {
                                label: '阿里云 OSS',
                                value: StorageProviderType.ALIYUN_OSS,
                            },
                            {
                                label: '腾讯 OSS',
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
