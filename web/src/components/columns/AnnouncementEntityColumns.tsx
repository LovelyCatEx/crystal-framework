import {message, Switch, Tag} from "antd";
import type {EntityTableColumns} from "@/components/table/entity-table.types.ts";
import type {Announcement} from "@/types/system/announcement.types.ts";
import {ANNOUNCEMENT_TARGET_COLORS} from "@/types/system/announcement.types.ts";
import {AnnouncementManagerController} from "@/api/system/announcement.api.ts";
import {useTranslation} from "react-i18next";

interface UseAnnouncementTableColumnsOptions {
    onRefresh?: () => void;
}

export function useAnnouncementTableColumns(options?: UseAnnouncementTableColumnsOptions): EntityTableColumns<Announcement> {
    const { t } = useTranslation();
    const { onRefresh } = options ?? {};

    const handleStatusChange = (checked: boolean, row: Announcement) => {
        const newStatus = checked ? 1 : 0;
        AnnouncementManagerController
            .update({ id: row.id, status: newStatus })
            .then(() => {
                void message.success(t('pages.announcementManager.messages.statusUpdateSuccess'));
                onRefresh?.();
            })
            .catch(() => {
                void message.error(t('pages.announcementManager.messages.statusUpdateFailed'));
            });
    };

    return [
        {
            title: t('pages.announcementManager.columns.title'),
            dataIndex: 'title',
            key: 'title',
            width: 400,
            render: function (_: unknown, row: Announcement) {
                return (
                    <div className="flex flex-col gap-1">
                        <span className="line-clamp-1">{row.title}</span>
                        <Tag color="blue" className="!m-0 !text-[10px] !leading-4 !h-4 !px-1 !rounded w-fit">
                            ID: {row.id}
                        </Tag>
                    </div>
                );
            },
        },
        {
            title: t('pages.announcementManager.columns.content'),
            dataIndex: 'content',
            key: 'content',
            render: (val: unknown) => {
                const text = val as string ?? '';
                return <span className="line-clamp-2">{text.length > 128 ? `${text.slice(0, 128)}...` : text}</span>;
            },
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
            title: t('pages.announcementManager.columns.status'),
            dataIndex: 'status',
            key: 'status',
            width: 100,
            render: function (_: unknown, row: Announcement) {
                return (
                    <Switch
                        checked={row.status === 1}
                        disabled={row.status === 2}
                        onChange={(checked) => handleStatusChange(checked, row)}
                    />
                );
            },
        },
    ];
}
