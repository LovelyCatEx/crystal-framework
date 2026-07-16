import {useEffect, useState} from "react";
import {createPortal} from "react-dom";
import {Button, theme} from "antd";
import {CloseOutlined, EyeOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import ApprovalFlowViewer from "./ApprovalFlowViewer.tsx";

interface ApprovalFlowViewerOverlayProps {
    instanceId: string;
    visible: boolean;
    onClose: () => void;
}

export function ApprovalFlowViewerOverlay({instanceId, visible, onClose}: ApprovalFlowViewerOverlayProps) {
    const {token} = theme.useToken();
    const {t} = useTranslation();
    const [mounted, setMounted] = useState(false);
    const [animating, setAnimating] = useState(false);

    useEffect(() => {
        if (visible) {
            setMounted(true);
            // Double rAF ensures the initial frame with scale(0.92) is painted before transitioning
            requestAnimationFrame(() => {
                requestAnimationFrame(() => setAnimating(true));
            });
        } else if (mounted) {
            setAnimating(false);
            const timer = setTimeout(() => setMounted(false), 250);
            return () => clearTimeout(timer);
        }
    }, [visible]);

    if (!mounted) return null;

    return createPortal(
        <div
            className="fixed inset-0 flex flex-col transition-all duration-250 ease-out"
            style={{
                zIndex: 999,
                background: token.colorBgLayout,
                color: token.colorText,
                transform: animating ? 'scale(1)' : 'scale(0.92)',
                opacity: animating ? 1 : 0,
            }}
        >
            <ApprovalFlowViewer
                instanceId={instanceId}
                headerLeft={() => (
                    <Button type="text" icon={<CloseOutlined/>} onClick={onClose}/>
                )}
                headerRight={{
                    left: () => (
                        <Button icon={<CloseOutlined/>} onClick={onClose}>
                            {t('components.approvalFlowViewer.action.close')}
                        </Button>
                    ),
                }}
            />
        </div>,
        document.body
    );
}

export function ApprovalFlowViewerButton({instanceId}: { instanceId: string }) {
    const {t} = useTranslation();
    const [visible, setVisible] = useState(false);

    return (
        <>
            <Button
                type="text"
                size="small"
                icon={<EyeOutlined/>}
                onClick={() => setVisible(true)}
            >
                {t('components.approvalFlowViewer.action.viewFlow')}
            </Button>
            <ApprovalFlowViewerOverlay
                instanceId={instanceId}
                visible={visible}
                onClose={() => setVisible(false)}
            />
        </>
    );
}
