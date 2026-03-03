import {Col, Form, Input, Row, Select} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "../../../components/ManagerPageContainer.tsx";
import {
    type ManagerCreateFileResourceDTO,
    type ManagerReadFileResourceDTO,
    FileResourceManagerController
} from "../../../api/file-resource.api.ts";
import {useEffect, useRef, useState} from "react";
import {ResourceFileType} from "../../../types/file-resource.types.ts";
import {FILE_RESOURCE_MANAGER_TABLE_COLUMNS} from "../../../components/columns/FileResourceEntityColumns.tsx";

export function FileResourceManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [filterType, setFilterType] = useState<number>()

    useEffect(() => {
        pageRef?.current?.refreshData?.()
    }, [filterType]);

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
                                            label: '用户头像',
                                            value: ResourceFileType.USER_AVATAR,
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
        >

        </ManagerPageContainer>
    )
}
