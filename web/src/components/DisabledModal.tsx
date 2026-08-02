import {Modal, Typography} from "antd";
import i18n from "@/i18n";

const {Text} = Typography;

/**
 * Login-time dialog shown when the server rejects authentication because the
 * account has been disabled by an administrator (403 + DisabledContext).
 * Carries no dynamic data, so it only surfaces the server message plus a
 * static explanation.
 */
export function showDisabledModal(serverMessage?: string) {
    Modal.warning({
        title: i18n.t('api.disabledModal.title'),
        content: (
            <div style={{marginTop: 8}}>
                <Text>{i18n.t('api.disabledModal.description')}</Text>
                {serverMessage && (
                    <div style={{marginTop: 8}}>
                        <Text type="secondary" style={{whiteSpace: 'pre-wrap'}}>{serverMessage}</Text>
                    </div>
                )}
            </div>
        ),
        width: 460,
    });
}
