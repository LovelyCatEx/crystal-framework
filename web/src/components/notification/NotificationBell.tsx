import {useState} from "react";
import {useTranslation} from "react-i18next";
import {Badge, Button, Modal} from "antd";
import {BellOutlined} from "@ant-design/icons";
import {useBroadcastInbox} from "@/compositions/use-broadcast-inbox.ts";
import {NotificationCenter} from "@/components/notification/NotificationCenter.tsx";

/**
 * Header bell: shows the live unread-broadcast count (SWR-polled) as a badge and opens the
 * {@link NotificationCenter} in a modal on click. The center is also usable standalone inline in a
 * page — this component is only the header entry point.
 */
export function NotificationBell() {
    const {t} = useTranslation();
    const [open, setOpen] = useState(false);
    const {unreadCount} = useBroadcastInbox();

    return (
        <>
            <Badge count={unreadCount} size="small" offset={[-2, 2]}>
                <Button
                    type="text"
                    shape="circle"
                    icon={<BellOutlined style={{fontSize: 18}}/>}
                    onClick={() => setOpen(true)}
                    aria-label={t('components.notification.title')}
                />
            </Badge>
            <Modal
                title={t('components.notification.title')}
                open={open}
                onCancel={() => setOpen(false)}
                footer={null}
                width={760}
                styles={{body: {padding: 0}}}
            >
                <NotificationCenter/>
            </Modal>
        </>
    );
}
