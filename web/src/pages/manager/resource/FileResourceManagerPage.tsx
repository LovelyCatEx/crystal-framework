import {Button, Col, Form, Input, message, Row, Select} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    FileResourceManagerController,
    type ManagerCreateFileResourceDTO,
    managerGetFileDownloadUrl,
    type ManagerReadFileResourceDTO
} from "@/api/file-resource.api.ts";
import {useEffect, useRef, useState} from "react";
import {type FileResource, ResourceFileType} from "@/types/file-resource.types.ts";
import {FILE_RESOURCE_MANAGER_TABLE_COLUMNS} from "@/components/columns/FileResourceEntityColumns.tsx";
import {StorageProviderIdSelector, UserIdSelector} from "@/components/selector";
import {DownloadOutlined} from "@ant-design/icons";
import {downloadFile} from "@/utils/file-download.ts";

export function FileResourceManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [filterType, setFilterType] = useState<number>()

    useEffect(() => {
        pageRef?.current?.refreshData?.();
    }, [filterType]);

    const handleDownloadFileEntity = async (record: FileResource) => {
        const url = (await managerGetFileDownloadUrl(record.id)).data;

        if (url) {
            downloadFile(url);
        } else {
            await message.error("无法获取文件下载链接");
        }
    };

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName="文件资源"
            title="文件资源管理"
            subtitle="管理系统文件资源列表"
            columns={FILE_RESOURCE_MANAGER_TABLE_COLUMNS}
            editModalFormChildren={
                <>
                <Row gutter={24}>
                        <Col span={24}>
                            <Form.Item name="userId" label="所属用户" rules={[{ required: true }]}>
                                <UserIdSelector />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={8}>
                            <Form.Item name="type" label="文件类型" rules={[{ required: true }]}>
                                <Select
                                    className="w-full rounded-lg h-10 flex items-center"
                                    placeholder="选择文件类型"
                                    options={[
                                        {
                                            label: '用户头像',
                                            value: ResourceFileType.USER_AVATAR,
                                        }
                                    ]}
                                />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={24}>
                            <Form.Item name="storageProviderId" label="存储提供商" rules={[{ required: true }]}>
                                <StorageProviderIdSelector />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="fileName" label="文件名" rules={[{ required: true }, { max: 256, message: '文件名长度不能超过256个字符' }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder="文件名" maxLength={256} showCount />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="fileExtension" label="扩展名" rules={[{ required: true }, { max: 64, message: '扩展名长度不能超过64个字符' }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder="文件扩展名" maxLength={64} showCount />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="md5" label="MD5" rules={[{ required: true }, { max: 32, message: 'MD5长度不能超过32个字符' }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder="文件MD5值" maxLength={32} showCount />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="fileSize" label="文件大小 (Bytes)" rules={[{ required: true }]}>
                                <Input type="number" className="w-full rounded-lg h-10 flex items-center" placeholder="文件大小(字节)" />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item name="objectKey" label="对象键" rules={[{ required: true }, { max: 256, message: '对象键长度不能超过256个字符' }]}>
                        <Input className="w-full rounded-lg h-10 flex items-center" placeholder="存储对象键" maxLength={256} showCount />
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
                        className="min-w-32"
                        defaultValue="-1"
                        style={{ width: 120 }}
                        options={[
                            { value: '-1', label: '全部' },
                            {
                                label: '用户头像',
                                value: ResourceFileType.USER_AVATAR,
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
            tableRowActionsRender={(record) => (
                <>
                    <Button type="text" size="small" icon={<DownloadOutlined />} onClick={() => handleDownloadFileEntity(record)} />
                </>
            )}
        >

        </ManagerPageContainer>
    )
}
