import {Button, Col, Dropdown, Form, Input, message, Row, Select} from "antd";
import type {MenuProps} from "antd";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    FileResourceManagerController,
    getResourceFileDownloadUrlById,
    type ManagerCreateFileResourceDTO
} from "@/api/resource/file-resource.api.ts";
import {useEffect, useRef} from "react";
import {type FileResource} from "@/types/resource/file-resource.types.ts";
import {ResourceScope} from "@/types/BaseScopedEntity.ts";
import {useResourceFileTypes} from "@/compositions/use-resource-file-types.ts";
import {useFileResourceTableColumns} from "@/components/columns/FileResourceEntityColumns.tsx";
import {StorageProviderIdSelector, UserIdSelector} from "@/components/selector";
import {CopyOutlined, DownloadOutlined} from "@ant-design/icons";
import {downloadFile} from "@/utils/file-download.ts";
import {useTranslation} from "react-i18next";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";

export default function FileResourceManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({
        schema: { type: 'number', id: 'string' }
    });
    const {t} = useTranslation();
    const columns = useFileResourceTableColumns();
    const {types: resourceFileTypes} = useResourceFileTypes();
    const fileTypeOptions = resourceFileTypes.map((d) => ({value: d.typeId, label: d.displayName}));
    const fileTypeOptionsAsString = resourceFileTypes.map((d) => ({value: String(d.typeId), label: d.displayName}));

    useEffect(() => {
        pageRef?.current?.refreshData?.({ resetPage: true });
    }, [filters.type, filters.id]);

    const handleDownloadFileEntity = async (record: FileResource) => {
        const url = (await getResourceFileDownloadUrlById(record.id)).data;

        if (url) {
            downloadFile(url);
        } else {
            await message.error(t('pages.fileResourceManager.messages.downloadFailed'));
        }
    };

    const handleCopyFileEntityLink = async (record: FileResource) => {
        const url = (await getResourceFileDownloadUrlById(record.id)).data;

        if (!url) {
            await message.error(t('pages.fileResourceManager.messages.downloadFailed'));
            return;
        }

        try {
            await navigator.clipboard.writeText(url);
            await message.success(t('pages.fileResourceManager.messages.copyLinkSuccess'));
        } catch {
            await message.error(t('pages.fileResourceManager.messages.copyLinkFailed'));
        }
    };

    const buildRowActionMenu = (record: FileResource): MenuProps['items'] => [
        {
            key: 'download',
            icon: <DownloadOutlined />,
            label: t('pages.fileResourceManager.actions.download'),
            onClick: () => handleDownloadFileEntity(record),
        },
        {
            key: 'copyLink',
            icon: <CopyOutlined />,
            label: t('pages.fileResourceManager.actions.copyLink'),
            onClick: () => handleCopyFileEntityLink(record),
        },
    ];

    const filterableFields = [
        { field: 'id', type: 'number' as const, label: t('pages.fileResourceManager.filter.id') },
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
                    options={fileTypeOptionsAsString}
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
            searchKeywords={['file_name', 'md5', 'file_extension']}
            filterableFields={filterableFields}
            queryParamsSync={syncToUrl}
            initialQueryValues={initialQueryValues}
            simpleFilters={[
                { field: 'id', operator: 'eq', value: filters.id },
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
                                    options={fileTypeOptions}
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
            query={async (props) => {
                return (await FileResourceManagerController.query({
                    ...props,
                    scope: ResourceScope.SYSTEM,
                    scopeId: '0',
                })).data!
            }}
            delete={async (props) => {
                return (await FileResourceManagerController.delete(props)).data!
            }}
            update={async (props) => {
                return (await FileResourceManagerController.update(props)).data!
            }}
            create={async (props) => {
                return (await FileResourceManagerController.create({
                    ...props,
                    scope: ResourceScope.SYSTEM,
                    scopeId: '0',
                } as unknown as ManagerCreateFileResourceDTO)).data!
            }}
            tableActions={[
                {
                    label: <span>{t('pages.fileResourceManager.filter.id')}</span>,
                    children: <Input
                        style={{ width: 160 }}
                        placeholder={t('pages.fileResourceManager.filter.idPlaceholder')}
                        defaultValue={filters.id}
                        allowClear
                        onPressEnter={(e) => setFilter('id', (e.target as HTMLInputElement).value || undefined)}
                        onChange={(e) => { if (e.target.value === '') setFilter('id', undefined); }}
                    />,
                },
                {
                    label: <span>{t('pages.fileResourceManager.filter.type')}</span>,
                    children: <Select
                        className="min-w-32"
                        defaultValue={filters.type !== undefined ? String(filters.type) : '-1'}
                        style={{ width: 120 }}
                        options={[
                            { value: '-1', label: t('pages.fileResourceManager.filter.all') },
                            ...fileTypeOptions,
                        ]}
                        onChange={(value) => setFilter('type', value === '-1' ? undefined : Number.parseInt(value))}
                    />,
                }
            ]}
            tableRowActionsRender={(record) => (
                <Dropdown menu={{ items: buildRowActionMenu(record) }} trigger={['hover']}>
                    <Button type="text" size="small" icon={<DownloadOutlined />} />
                </Dropdown>
            )}
        >

        </ManagerPageContainer>
    )
}
