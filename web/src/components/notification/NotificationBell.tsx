import {useState} from "react";
import {useTranslation} from "react-i18next";
import {Badge, Button, Modal, theme} from "antd";
import {BellOutlined} from "@ant-design/icons";
import {useTotalUnread} from "@/compositions/use-total-unread.ts";
import {NotificationCenter} from "@/components/notification/NotificationCenter.tsx";

const {useToken} = theme;

/**
 * Header bell: shows the live unread-broadcast count (SWR-polled) as a badge and opens the
 * {@link NotificationCenter} in a modal on click. The center is also usable standalone inline in a
 * page — this component is only the header entry point.
 */
export function NotificationBell() {
    const {t} = useTranslation();
    const {token} = useToken();
    const [open, setOpen] = useState(false);
    const {unreadCount} = useTotalUnread();

    return (
        <>
            <Badge count={unreadCount} size="small" offset={[-2, 2]}>
                <Button
                    type="text"
                    shape="circle"
                    icon={<BellOutlined style={{fontSize: 16}}/>}
                    onClick={() => setOpen(true)}
                    aria-label={t('components.notification.title')}
                />
            </Badge>
            <Modal
                title={t('components.notification.title')}
                open={open}
                onCancel={() => setOpen(false)}
                maskClosable
                footer={null}
                width={760}
                styles={{body: {padding: 0}}}
            >
                {/* The center is frameless (to embed cleanly in pages); the modal supplies the frame here. */}
                <NotificationCenter
                    style={{
                        background: token.colorBgContainer,
                        border: `1px solid ${token.colorBorderSecondary}`,
                        borderRadius: token.borderRadiusLG,
                    }}
                />
            </Modal>
        </>
    );
}
