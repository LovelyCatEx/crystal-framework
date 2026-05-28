import { Col, Form, Input, InputNumber, Row, Select, Tag } from 'antd';
import { ManagerPageContainer, type ManagerPageContainerRef } from '@/components/ManagerPageContainer.tsx';
import {
    AnnouncementManagerController,
    type ManagerCreateAnnouncementDTO,
    type ManagerReadAnnouncementDTO,
} from '@/api/system/announcement.api.ts';
import type { Announcement } from '@/types/system/announcement.types.ts';
import { ANNOUNCEMENT_STATUS_COLORS, ANNOUNCEMENT_TARGET_COLORS } from '@/types/system/announcement.types.ts';
import { useTranslation } from 'react-i18next';
import { useRef } from 'react';
import type { EntityTableColumns } from '@/components/table/entity-table.types.ts';
import { formatTimestamp } from '@/utils/datetime.utils.ts';

export default function AnnouncementManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const { t } = useTranslation();

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

    const columns: EntityTableColumns<Announcement> = [
        {
            title: t('pages.announcementManager.columns.title'),
            dataIndex: 'title',
            key: 'title',
            render: (val: unknown) => <span className="line-clamp-1">{val as string}</span>,
        },
        {
            title: t('pages.announcementManager.columns.status'),
            dataIndex: 'status',
            key: 'status',
            width: 100,
            render: (val: unknown) => (
                <Tag color={ANNOUNCEMENT_STATUS_COLORS[val as number]}>
                    {t(`enums.announcementStatus.${val}`)}
                </Tag>
            ),
        },
        {
            title: t('pages.announcementManager.columns.target'),
            dataIndex: 'target',
            key: 'target',
            width: 130,
            render: (val: unknown) => (
                <Tag color={ANNOUNCEMENT_TARGET_COLORS[val as number]}>
                    {t(`enums.announcementTarget.${val}`)}
                </Tag>
            ),
        },
        {
            title: t('pages.announcementManager.columns.priority'),
            dataIndex: 'priority',
            key: 'priority',
            width: 90,
            render: (val: unknown) => <span>{val as number}</span>,
        },
        {
            title: t('pages.announcementManager.columns.createdTime'),
            dataIndex: 'createdTime',
            key: 'createdTime',
            width: 180,
            render: (val: unknown) => formatTimestamp(val as string),
        },
    ];

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName={t('entityNames.announcement')}
            title={t('pages.announcementManager.title')}
            subtitle={t('pages.announcementManager.subtitle')}
            columns={columns}
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
