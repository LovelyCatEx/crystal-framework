import {Button, Col, Form, Input, message, Row, Select} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    FileResourceManagerController,
    type ManagerCreateFileResourceDTO,
    getResourceFileDownloadUrlById,
    type ManagerReadFileResourceDTO
} from "@/api/file-resource.api.ts";
import {useEffect, useRef, useState} from "react";
import {type FileResource, ResourceFileType} from "@/types/file-resource.types.ts";
import {getResourceFileType} from "@/i18n/enum-helpers.ts";
import {useFileResourceTableColumns} from "@/components/columns/FileResourceEntityColumns.tsx";
import {StorageProviderIdSelector, UserIdSelector} from "@/components/selector";
import {DownloadOutlined} from "@ant-design/icons";
import {downloadFile} from "@/utils/file-download.ts";
import {useTranslation} from "react-i18next";

export function FileResourceManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const [filterType, setFilterType] = useState<number>()
    const {t} = useTranslation();
    const columns = useFileResourceTableColumns();

    useEffect(() => {
        pageRef?.current?.refreshData?.();
    }, [filterType]);

    const handleDownloadFileEntity = async (record: FileResource) => {
        const url = (await getResourceFileDownloadUrlById(record.id)).data;

        if (url) {
            downloadFile(url);
        } else {
            await message.error(t('pages.fileResourceManager.messages.downloadFailed'));
        }
    };

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName={t('entityNames.fileResource')}
            title={t('pages.fileResourceManager.title')}
            subtitle={t('pages.fileResourceManager.subtitle')}
            columns={columns}
            editModalFormChildren={
                <>
                <Row gutter={24}>
                        <Col span={24}>
                            <Form.Item name="userId" label={t('pages.fileResourceManager.modal.userId.label')} rules={[{ required: true, message: t('pages.fileResourceManager.modal.userId.required') }]}>
                                <UserIdSelector />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={8}>
                            <Form.Item name="type" label={t('pages.fileResourceManager.modal.type.label')} rules={[{ required: true, message: t('pages.fileResourceManager.modal.type.required') }]}>
                                <Select
                                    className="w-full rounded-lg h-10 flex items-center"
                                    placeholder={t('pages.fileResourceManager.modal.type.placeholder')}
                                    options={[
                                        {
                                            label: getResourceFileType(ResourceFileType.USER_AVATAR),
                                            value: ResourceFileType.USER_AVATAR,
                                        },
                                        {
                                            label: getResourceFileType(ResourceFileType.TENANT_ICON),
                                            value: ResourceFileType.TENANT_ICON,
                                        }
                                    ]}
                                />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={24}>
                            <Form.Item name="storageProviderId" label={t('pages.fileResourceManager.modal.storageProviderId.label')} rules={[{ required: true, message: t('pages.fileResourceManager.modal.storageProviderId.required') }]}>
                                <StorageProviderIdSelector />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="fileName" label={t('pages.fileResourceManager.modal.fileName.label')} rules={[{ required: true, message: t('pages.fileResourceManager.modal.fileName.required') }, { max: 256, message: t('pages.fileResourceManager.modal.fileName.maxLength') }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.fileResourceManager.modal.fileName.placeholder')} maxLength={256} showCount />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="fileExtension" label={t('pages.fileResourceManager.modal.fileExtension.label')} rules={[{ required: true, message: t('pages.fileResourceManager.modal.fileExtension.required') }, { max: 64, message: t('pages.fileResourceManager.modal.fileExtension.maxLength') }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.fileResourceManager.modal.fileExtension.placeholder')} maxLength={64} showCount />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item name="md5" label={t('pages.fileResourceManager.modal.md5.label')} rules={[{ required: true, message: t('pages.fileResourceManager.modal.md5.required') }, { max: 32, message: t('pages.fileResourceManager.modal.md5.maxLength') }]}>
                                <Input className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.fileResourceManager.modal.md5.placeholder')} maxLength={32} showCount />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item name="fileSize" label={t('pages.fileResourceManager.modal.fileSize.label')} rules={[{ required: true, message: t('pages.fileResourceManager.modal.fileSize.required') }]}>
                                <Input type="number" className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.fileResourceManager.modal.fileSize.placeholder')} />
                            </Form.Item>
                        </Col>
                    </Row>
                    <Form.Item name="objectKey" label={t('pages.fileResourceManager.modal.objectKey.label')} rules={[{ required: true, message: t('pages.fileResourceManager.modal.objectKey.required') }, { max: 256, message: t('pages.fileResourceManager.modal.objectKey.maxLength') }]}>
                        <Input className="w-full rounded-lg h-10 flex items-center" placeholder={t('pages.fileResourceManager.modal.objectKey.placeholder')} maxLength={256} showCount />
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
                    label: <span>{t('pages.fileResourceManager.filter.type')}</span>,
                    children: <Select
                        className="min-w-32"
                        defaultValue="-1"
                        style={{ width: 120 }}
                        options={[
                            { value: '-1', label: t('pages.fileResourceManager.filter.all') },
                            {
                                label: getResourceFileType(ResourceFileType.USER_AVATAR),
                                value: ResourceFileType.USER_AVATAR,
                            },
                            {
                                label: getResourceFileType(ResourceFileType.TENANT_ICON),
                                value: ResourceFileType.TENANT_ICON,
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
