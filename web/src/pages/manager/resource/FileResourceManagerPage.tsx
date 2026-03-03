import {Col, Form, Input, Row, Select, Space, Tag} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "../../../components/ManagerPageContainer.tsx";
import {
    type ManagerCreateFileResourceDTO,
    type ManagerReadFileResourceDTO,
    FileResourceManagerController
} from "../../../api/file-resource.api.ts";
import React, {type JSX, useEffect, useRef, useState} from "react";
import {ResourceFileType, type FileResource} from "../../../types/file-resource.types.ts";
import {CopyableToolTip} from "../../../components/CopyableToolTip.tsx";

export function FileResourceManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [filterType, setFilterType] = useState<number>()

    useEffect(() => {
        pageRef?.current?.refreshData?.()
    }, [filterType]);

    const formatFileSize = (bytes: number): string => {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    };

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName="文件资源"
            title="文件资源管理"
            subtitle="管理系统文件资源列表"
            columns={[
                {
                    title: "文件信息",
                    dataIndex: "fileName",
                    key: "fileName",
                    render: function (_: unknown, row: FileResource): React.ReactNode | JSX.Element {
                        return <Space orientation='vertical' size={0}>
                            <CopyableToolTip title={row.fileName}>
                                <span className="text-xs font-mono">{row.fileName}</span>
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
                    render: function (_: unknown, row: FileResource): React.ReactNode | JSX.Element {
                        return <Space orientation='vertical' size={0}>
                            <CopyableToolTip title={ResourceFileType[row.type]}>
                                <Tag color="orange" className="text-xs font-mono">{ResourceFileType[row.type]}</Tag>
                            </CopyableToolTip>
                        </Space>
                    }
                },
                {
                    title: "文件属性",
                    dataIndex: "fileExtension",
                    key: "fileExtension",
                    render: function (_: unknown, row: FileResource): React.ReactNode | JSX.Element {
                        return <Space orientation='vertical' size={0}>
                            <span className="text-xs font-mono">扩展名: {row.fileExtension}</span>
                            <span className="text-xs font-mono">大小: {formatFileSize(row.fileSize)}</span>
                        </Space>
                    }
                },
                {
                    title: "MD5",
                    dataIndex: "md5",
                    key: "md5",
                    render: function (_: unknown, row: FileResource): React.ReactNode | JSX.Element {
                        return <CopyableToolTip title={row.md5}>
                            <span className="text-xs font-mono">{row.md5.substring(0, 16)}...</span>
                        </CopyableToolTip>
                    }
                },
                {
                    title: "存储信息",
                    dataIndex: "storageProviderId",
                    key: "storageProviderId",
                    render: function (_: unknown, row: FileResource): React.ReactNode | JSX.Element {
                        return <Space orientation='vertical' size={0}>
                            <span className="text-xs font-mono">提供商ID: {row.storageProviderId}</span>
                            <CopyableToolTip title={row.objectKey}>
                                <span className="text-xs font-mono text-gray-500">{row.objectKey.substring(0, 30)}...</span>
                            </CopyableToolTip>
                        </Space>
                    }
                }
            ]}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={8}>
                            <Form.Item name="userId" label="用户ID" rules={[{ required: true }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder="所属用户ID" />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="type" label="文件类型" rules={[{ required: true }]}>
                                <Select
                                    className="w-full rounded-lg h-10 flex items-center"
                                    placeholder="选择文件类型"
                                    options={[
                                        {
                                            label: 'USER_AVATAR',
                                            value: 0,
                                        }
                                    ]}
                                />
                            </Form.Item>
                        </Col>
                        <Col span={8}>
                            <Form.Item name="storageProviderId" label="存储提供商ID" rules={[{ required: true }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder="存储提供商ID" />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="fileName" label="文件名" rules={[{ required: true }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder="文件名" />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="fileExtension" label="扩展名" rules={[{ required: true }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder="文件扩展名" />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="md5" label="MD5" rules={[{ required: true }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder="文件MD5值" />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="fileSize" label="文件大小" rules={[{ required: true }]}>
                                <Input type="number" className="w-full rounded-lg h-10 flex items-center" placeholder="文件大小(字节)" />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item name="objectKey" label="对象键" rules={[{ required: true }]}>
                        <Input className="w-full rounded-lg h-10 flex items-center" placeholder="存储对象键" />
                    </Form.Item>
                </>
            }
            query={async (props: ManagerReadFileResourceDTO) => {
                return (await FileResourceManagerController.query(props)).data!
            }}
            delete={async (props) => {
                return (await FileResourceManagerController.delete(props)).data!
            }}
            update={async (props) => {
                return (await FileResourceManagerController.update(props)).data!
            }}
            create={async (props) => {
                return (await FileResourceManagerController.create(props as ManagerCreateFileResourceDTO)).data!
            }}
            tableActions={[
                {
                    label: <span>类型</span>,
                    children: <Select
                        defaultValue="-1"
                        style={{ width: 120 }}
                        options={[
                            { value: '-1', label: '全部' },
                            { value: '0', label: 'USER_AVATAR' },
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
