import {Modal, Tag, Typography} from "antd";
import i18n from "@/i18n";
import {formatTimestamp} from "@/utils/datetime.utils.ts";
import type {BanContext} from "@/types/common/forbidden.types.ts";

const {Text} = Typography;

function renderBanModalContent(context: BanContext) {
    const {reason, bannedAt, banUntil} = context;
    return (
        <div style={{marginTop: 8}}>
            <div style={{marginBottom: 12}}>
                <Text type="secondary">{i18n.t('api.banModal.reasonLabel')}</Text>
                <div style={{marginTop: 4}}>
                    <Text strong>{reason || i18n.t('api.banModal.noReason')}</Text>
                </div>
            </div>

            <div style={{marginBottom: 12}}>
                <Text type="secondary">{i18n.t('api.banModal.bannedAtLabel')}</Text>
                <div style={{marginTop: 4}}>
                    <Text>{formatTimestamp(bannedAt)}</Text>
                </div>
            </div>

            <div>
                <Text type="secondary">{i18n.t('api.banModal.banUntilLabel')}</Text>
                <div style={{marginTop: 4}}>
                    {banUntil ? (
                        <Text>{formatTimestamp(banUntil)}</Text>
                    ) : (
                        <Tag color="volcano">{i18n.t('api.banModal.permanent')}</Tag>
                    )}
                </div>
            </div>
        </div>
    );
}

export function showBanModal(context: BanContext) {
    Modal.warning({
        title: i18n.t('api.banModal.title'),
        content: renderBanModalContent(context),
        width: 520,
    });
}
