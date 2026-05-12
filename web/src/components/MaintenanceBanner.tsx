import {useTranslation} from "react-i18next";
import {ToolOutlined} from "@ant-design/icons";
import {useMaintenanceStatus} from "@/compositions/use-maintenance.ts";
import {useEffect, useState} from "react";
import {theme} from "antd";

const {useToken} = theme;
const BANNER_HEIGHT = 40;

/**
 * A fixed bottom banner that displays when the system is in maintenance mode
 * and the current user has access privileges.
 * Slides down when the mouse enters the bottom area, never blocks clicks.
 */
export function MaintenanceBanner() {
    const {t} = useTranslation();
    const {token} = useToken();
    const {maintenanceMode, canAccess} = useMaintenanceStatus();
    const [hidden, setHidden] = useState(false);

    useEffect(() => {
        const handleMouseMove = (e: MouseEvent) => {
            const nearBottom = e.clientY >= window.innerHeight - BANNER_HEIGHT;
            setHidden(nearBottom);
        };

        window.addEventListener('mousemove', handleMouseMove);
        return () => window.removeEventListener('mousemove', handleMouseMove);
    }, []);

    if (!maintenanceMode || !canAccess) {
        return null;
    }

    return (
        <div
            className="fixed bottom-0 left-0 right-0 z-[1000] flex items-center justify-center gap-2 px-4 py-2 backdrop-blur-md pointer-events-none transition-transform duration-300 ease-in-out"
            style={{
                backgroundColor: token.colorPrimary + 'cc',
                color: '#ffffff',
                transform: hidden ? 'translateY(100%)' : 'translateY(0)',
            }}
        >
            <ToolOutlined />
            <span className="text-sm font-medium">
                {t('components.maintenanceBanner.message')}
            </span>
        </div>
    );
}
