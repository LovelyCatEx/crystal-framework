import {Col, Form, Input, InputNumber, Row, Select} from 'antd';
import { ManagerPageContainer, type ManagerPageContainerRef } from '@/components/ManagerPageContainer.tsx';
import {
    AnnouncementManagerController,
    type ManagerCreateAnnouncementDTO,
    type ManagerReadAnnouncementDTO,
} from '@/api/system/announcement.api.ts';
import type { Announcement } from '@/types/system/announcement.types.ts';
import { useTranslation } from 'react-i18next';
import { useEffect, useRef } from 'react';
import {useAnnouncementTableColumns} from "@/components/columns/AnnouncementEntityColumns.tsx";
import {useManagerQueryParams} from "@/compositions/use-manager-query-params.ts";

export default function AnnouncementManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const { t } = useTranslation();

    const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({
        schema: { id: 'string', status: 'number', target: 'number' } as const,
    });

    useEffect(() => {
        pageRef.current?.refreshData({ resetPage: true });
    }, [filters.id, filters.status, filters.target]);

    const statusOptions = [
        { value: 0, label: t('enums.announcementStatus.0') },
        { value: 1, label: t('enums.announcementStatus.1') },
        { value: 2, label: t('enums.announcementStatus.2') },
    ];

    const targetOptions = [
        { value: 0, label: t('enums.announcementTarget.0') },
        { value: 1, label: t('enums.announcementTarget.1') },
        { value: 2, label: t('enums.announcementTarget.2') },
    ];

    const columns = useAnnouncementTableColumns({
        onRefresh: () => pageRef.current?.refreshData(),
    });

    const filterableFields = [
        { field: 'id', type: 'number' as const, label: t('pages.announcementManager.filter.id') },
        { field: 'priority', type: 'number' as const, label: t('pages.announcementManager.filter.priority') },
        {
            field: 'status',
            type: 'number' as const,
            label: t('pages.announcementManager.filter.status'),
            renderValue: ({ value, onChange }: { value: unknown; onChange: (v: unknown) => void }) => (
                <Select
                    className="flex-1"
                    value={value !== undefined ? String(value) : undefined}
                    allowClear
                    placeholder={t('pages.announcementManager.filter.all')}
                    options={statusOptions.map(o => ({ label: o.label, value: String(o.value) }))}
                    onChange={(v) => onChange(v !== undefined ? Number(v) : undefined)}
                />
            ),
        },
        {
            field: 'target',
            type: 'number' as const,
            label: t('pages.announcementManager.filter.target'),
            renderValue: ({ value, onChange }: { value: unknown; onChange: (v: unknown) => void }) => (
                <Select
                    className="flex-1"
                    value={value !== undefined ? String(value) : undefined}
                    allowClear
                    placeholder={t('pages.announcementManager.filter.all')}
                    options={targetOptions.map(o => ({ label: o.label, value: String(o.value) }))}
                    onChange={(v) => onChange(v !== undefined ? Number(v) : undefined)}
                />
            ),
        },
    ];

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName={t('entityNames.announcement')}
            title={t('pages.announcementManager.title')}
            subtitle={t('pages.announcementManager.subtitle')}
            columns={columns}
            searchKeywords={['title', 'content']}
            filterableFields={filterableFields}
            queryParamsSync={syncToUrl}
            initialQueryValues={initialQueryValues}
            simpleFilters={[
                { field: 'id', operator: 'eq', value: filters.id },
                { field: 'status', operator: 'eq', value: filters.status },
                { field: 'target', operator: 'eq', value: filters.target },
            ]}
            tableActions={[
                {
                    label: <span>{t('pages.announcementManager.filter.id')}</span>,
                    children: <Input
                        style={{ width: 160 }}
                        placeholder={t('pages.announcementManager.filter.idPlaceholder')}
                        defaultValue={filters.id}
                        allowClear
                        onPressEnter={(e) => setFilter('id', (e.target as HTMLInputElement).value || undefined)}
                        onChange={(e) => { if (e.target.value === '') setFilter('id', undefined); }}
                    />,
                },
                {
                    label: <span>{t('pages.announcementManager.filter.status')}</span>,
                    children: <Select
                        defaultValue={filters.status !== undefined ? String(filters.status) : '-1'}
                        style={{ width: 120 }}
                        options={[
                            { value: '-1', label: t('pages.announcementManager.filter.all') },
                            ...statusOptions,
                        ]}
                        onChange={(value) => setFilter('status', value === '-1' ? undefined : Number.parseInt(value))}
                    />,
                },
                {
                    label: <span>{t('pages.announcementManager.filter.target')}</span>,
                    children: <Select
                        defaultValue={filters.target !== undefined ? String(filters.target) : '-1'}
                        style={{ width: 120 }}
                        options={[
                            { value: '-1', label: t('pages.announcementManager.filter.all') },
                            ...targetOptions,
                        ]}
                        onChange={(value) => setFilter('target', value === '-1' ? undefined : Number.parseInt(value))}
                    />,
                },
            ]}
            editModalFormChildren={(editingItem) => {
                const item = editingItem as Announcement | null;
                return (
                    <>
                        <Row gutter={24}>
                            <Col span={24}>
                                <Form.Item
                                    name="title"
                                    label={t('pages.announcementManager.modal.title.label')}
                                    rules={[
                                        { required: true, message: t('pages.announcementManager.modal.title.required') },
                                        { max: 256, message: t('pages.announcementManager.modal.title.maxLength') },
                                    ]}
                                >
                                    <Input className="rounded-lg h-10" maxLength={256} showCount />
                                </Form.Item>
                            </Col>
                        </Row>
                        <Row gutter={24}>
                            <Col span={24}>
                                <Form.Item
                                    name="content"
                                    label={t('pages.announcementManager.modal.content.label')}
                                    rules={[
                                        { required: true, message: t('pages.announcementManager.modal.content.required') },
                                    ]}
                                >
                                    <Input.TextArea
                                        className="rounded-lg"
                                        rows={5}
                                        maxLength={10000}
                                        showCount
                                    />
                                </Form.Item>
                            </Col>
                        </Row>
                        <Row gutter={24}>
                            <Col span={8}>
                                <Form.Item
                                    name="status"
                                    label={t('pages.announcementManager.modal.status.label')}
                                    initialValue={item?.status ?? 0}
                                >
                                    <Select options={statusOptions} className="w-full" />
                                </Form.Item>
                            </Col>
                            <Col span={8}>
                                <Form.Item
                                    name="target"
                                    label={t('pages.announcementManager.modal.target.label')}
                                    initialValue={item?.target ?? 2}
                                >
                                    <Select options={targetOptions} className="w-full" />
                                </Form.Item>
                            </Col>
                            <Col span={8}>
                                <Form.Item
                                    name="priority"
                                    label={t('pages.announcementManager.modal.priority.label')}
                                    initialValue={item?.priority ?? 0}
                                >
                                    <InputNumber className="w-full" min={0} max={9999} />
                                </Form.Item>
                            </Col>
                        </Row>
                    </>
                );
            }}
            query={async (props: ManagerReadAnnouncementDTO) => {
                return (await AnnouncementManagerController.query(props)).data!;
            }}
            delete={async (props) => {
                return (await AnnouncementManagerController.delete(props)).data!;
            }}
            update={async (props) => {
                return (await AnnouncementManagerController.update(props)).data!;
            }}
            create={async (props) => {
                return (await AnnouncementManagerController.create(props as ManagerCreateAnnouncementDTO)).data!;
            }}
        />
    );
}
