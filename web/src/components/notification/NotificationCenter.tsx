import {useState} from "react";
import {useTranslation} from "react-i18next";
import {Badge, Empty, List, Spin, theme, Typography} from "antd";
import {NotificationOutlined} from "@ant-design/icons";
import dayjs from "dayjs";
import {useBroadcastInbox} from "@/compositions/use-broadcast-inbox.ts";
import type {Broadcast} from "@/types/message/broadcast.types.ts";

const {useToken} = theme;
const {Text, Paragraph, Title} = Typography;

/** The single built-in conversation kind currently wired: read-diffusion system broadcasts. */
export enum NotificationConversationKind {
    SYSTEM_BROADCAST = 'system-broadcast',
}

const {SYSTEM_BROADCAST} = NotificationConversationKind;

/**
 * Embeddable notification center: a left conversation list + right content panel. Designed to work
 * both inside a Modal (header bell) and inline in a page. Today only the read-diffusion
 * "System Announcements" conversation exists; write-diffusion conversations (P2P / tenant) will slot
 * into the same left list later without changing the shell. Opening a broadcast marks it read.
 */
export function NotificationCenter() {
    const {t} = useTranslation();
    const {token} = useToken();
    const {unreadBroadcasts, unreadCount, isLoading, markRead} = useBroadcastInbox();
    const [activeKind, setActiveKind] = useState<NotificationConversationKind>(SYSTEM_BROADCAST);

    return (
        <div className="flex" style={{height: 480}}>
            {/* Left: conversation list */}
            <div
                className="flex flex-col"
                style={{width: 220, borderRight: `1px solid ${token.colorBorderSecondary}`}}
            >
                <List
                    dataSource={[SYSTEM_BROADCAST]}
                    renderItem={(kind) => (
                        <List.Item
                            className="cursor-pointer"
                            style={{
                                paddingInline: 16,
                                background: kind === activeKind ? token.colorFillTertiary : undefined,
                            }}
                            onClick={() => setActiveKind(kind)}
                        >
                            <List.Item.Meta
                                avatar={<NotificationOutlined style={{color: token.colorPrimary}}/>}
                                title={t('components.notification.conversations.systemBroadcast')}
                            />
                            <Badge count={unreadCount} size="small"/>
                        </List.Item>
                    )}
                />
            </div>

            {/* Right: content panel */}
            <div className="flex-1 overflow-auto" style={{padding: 16}}>
                {activeKind === SYSTEM_BROADCAST && (
                    <SystemBroadcastPanel
                        broadcasts={unreadBroadcasts}
                        loading={isLoading}
                        onOpen={markRead}
                    />
                )}
            </div>
        </div>
    );
}

function SystemBroadcastPanel(props: {
    broadcasts: Broadcast[];
    loading: boolean;
    onOpen: (broadcastId: string) => void;
}) {
    const {t} = useTranslation();
    const {token} = useToken();

    if (props.loading) {
        return <div className="flex justify-center py-8"><Spin/></div>;
    }
    if (props.broadcasts.length === 0) {
        return <Empty description={t('components.notification.empty')}/>;
    }

    return (
        <List
            dataSource={props.broadcasts}
            renderItem={(item) => (
                <List.Item onClick={() => props.onOpen(item.id)} className="cursor-pointer">
                    <div className="w-full">
                        <div className="flex items-center justify-between">
                            <Title level={5} style={{margin: 0}}>{item.title}</Title>
                            <Text type="secondary" className="text-xs">
                                {dayjs(Number(item.publishTime)).format('YYYY-MM-DD HH:mm')}
                            </Text>
                        </div>
                        <Paragraph style={{marginBottom: 0, color: token.colorTextSecondary}}>
                            {item.content}
                        </Paragraph>
                    </div>
                </List.Item>
            )}
        />
    );
}
