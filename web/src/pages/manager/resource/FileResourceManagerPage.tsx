import {Button, Col, Form, Input, message, Row, Select} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    FileResourceManagerController,
    getResourceFileDownloadUrlById,
    type ManagerCreateFileResourceDTO,
    type ManagerReadFileResourceDTO
} from "@/api/resource/file-resource.api.ts";
import {useEffect, useRef} from "react";
import {type FileResource, ResourceFileType} from "@/types/resource/file-resource.types.ts";
import {getResourceFileType} from "@/i18n/enum-helpers.ts";
import {useFileResourceTableColumns} from "@/components/columns/FileResourceEntityColumns.tsx";
import {StorageProviderIdSelector, UserIdSelector} from "@/components/selector";
import {DownloadOutlined} from "@ant-design/icons";
import {downloadFile} from "@/utils/file-download.ts";
import {useTranslation} from "react-i18next";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";

export default function FileResourceManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({
        schema: { type: 'number' }
    });
    const {t} = useTranslation();
    const columns = useFileResourceTableColumns();

    useEffect(() => {
        pageRef?.current?.refreshData?.({ resetPage: true });
    }, [filters.type]);

    const handleDownloadFileEntity = async (record: FileResource) => {
        const url = (await getResourceFileDownloadUrlById(record.id)).data;

        if (url) {
            downloadFile(url);
        } else {
            await message.error(t('pages.fileResourceManager.messages.downloadFailed'));
        }
    };

    const filterableFields = [
        {
            field: 'type',
            type: 'number' as const,
            label: t('pages.fileResourceManager.filter.type'),
            renderValue: ({ value, onChange }: { value: unknown; onChange: (v: unknown) => void }) => (
                <Select
                    className="flex-1"
                    value={value !== undefined ? String(value) : undefined}
                    allowClear
                    placeholder={t('pages.fileResourceManager.filter.all')}
                    options={[
                        { value: String(ResourceFileType.USER_AVATAR), label: getResourceFileType(ResourceFileType.USER_AVATAR) },
                        { value: String(ResourceFileType.TENANT_ICON), label: getResourceFileType(ResourceFileType.TENANT_ICON) },
                    ]}
                    onChange={(v) => onChange(v !== undefined ? Number(v) : undefined)}
                />
            ),
        },
    ];

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName={t('entityNames.fileResource')}
            title={t('pages.fileResourceManager.title')}
            subtitle={t('pages.fileResourceManager.subtitle')}
            columns={columns}
            filterableFields={filterableFields}
            queryParamsSync={syncToUrl}
            initialQueryValues={initialQueryValues}
            simpleFilters={[
                { field: 'type', operator: 'eq', value: filters.type },
            ]}
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
                        defaultValue={filters.type !== undefined ? String(filters.type) : '-1'}
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
                        onChange={(value) => setFilter('type', value === '-1' ? undefined : Number.parseInt(value))}
                    />,
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
