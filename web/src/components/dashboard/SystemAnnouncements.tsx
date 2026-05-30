import {Card, Empty, Modal, Spin, theme, Typography} from "antd";
import {useTranslation} from "react-i18next";
import {useEffect, useState} from "react";
import {NotificationOutlined} from "@ant-design/icons";
import {getPublishedAnnouncements} from "@/api/system/announcement.api.ts";
import type {Announcement} from "@/types/system/announcement.types.ts";
import dayjs from "dayjs";

const { useToken } = theme;
const { Text, Paragraph } = Typography;

export function SystemAnnouncements() {
    const { token } = useToken();
    const { t } = useTranslation();
    const [announcements, setAnnouncements] = useState<Announcement[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let cancelled = false;
        setLoading(true);
        getPublishedAnnouncements()
            .then((res) => {
                if (!cancelled) setAnnouncements(res.data ?? []);
            })
            .catch(() => {
                if (!cancelled) setAnnouncements([]);
            })
            .finally(() => {
                if (!cancelled) setLoading(false);
            });
        return () => { cancelled = true; };
    }, []);

    return (
        <Card
            title={
                <div className="flex items-center gap-2">
                    <NotificationOutlined style={{ color: token.colorPrimary }} />
                    <span className="text-sm font-bold" style={{ color: token.colorTextHeading }}>
                        {t('components.dashboard.systemAnnouncements.title')}
                    </span>
                </div>
            }
            className="rounded-3xl border-none shadow-sm h-full"
            styles={{ body: { padding: 0 } }}
        >
            {loading ? (
                <div className="flex justify-center items-center py-12">
                    <Spin size="small" />
                </div>
            ) : announcements.length === 0 ? (
                <Empty
                    image={Empty.PRESENTED_IMAGE_SIMPLE}
                    description={t('components.dashboard.systemAnnouncements.noAnnouncements')}
                    className="py-8"
                />
            ) : (
                <div>
                    {announcements.map((a, index) => (
                        <div
                            key={a.id}
                            className="py-4 px-6 cursor-pointer transition-colors"
                            style={index < announcements.length - 1 ? {borderBottom: `1px solid ${token.colorBorderSecondary}`} : undefined}
                            onMouseEnter={(e) => {
                                e.currentTarget.style.backgroundColor = token.controlItemBgHover;
                            }}
                            onMouseLeave={(e) => {
                                e.currentTarget.style.backgroundColor = '';
                            }}
                            onClick={() => {
                                Modal.info({
                                    title: a.title,
                                    content: (
                                        <div style={{whiteSpace: 'pre-wrap'}}>{a.content}</div>
                                    ),
                                    width: 560,
                                });
                            }}
                        >
                            <Text strong className="text-sm block truncate">
                                {a.title}
                            </Text>
                            <Paragraph
                                ellipsis={{rows: 2}}
                                className="!mb-0 !mt-1"
                                type="secondary"
                                style={{fontSize: 12}}
                            >
                                {a.content}
                            </Paragraph>
                            <Text type="secondary" style={{fontSize: 11}}>
                                {dayjs(Number(a.createdTime)).format('YYYY-MM-DD HH:mm')}
                            </Text>
                        </div>
                    ))}
                </div>
            )}
        </Card>
    );
}
