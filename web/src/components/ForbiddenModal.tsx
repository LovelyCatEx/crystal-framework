import {Modal, Space, Tag, Typography} from "antd";
import i18n from "@/i18n";
import type {ForbiddenContext} from "@/types/common/forbidden.types.ts";
import {getForbiddenReason, getForbiddenScope} from "@/i18n/enum-helpers.ts";

const {Text} = Typography;

function renderForbiddenModalContent(context: ForbiddenContext, serverMessage?: string) {
    const {reason, scope, requiredPermissions} = context;
    return (
        <div style={{marginTop: 8}}>
            <div style={{marginBottom: 12}}>
                <Text type="secondary">{i18n.t('api.forbiddenModal.reasonLabel')}</Text>
                <div style={{marginTop: 4}}>
                    <Text strong>{getForbiddenReason(reason)}</Text>
                </div>
            </div>

            <div style={{marginBottom: 12}}>
                <Text type="secondary">{i18n.t('api.forbiddenModal.scopeLabel')}</Text>
                <div style={{marginTop: 4}}>
                    <Tag color="blue">{getForbiddenScope(scope)}</Tag>
                </div>
            </div>

            <div style={{marginBottom: 12}}>
                <Text type="secondary">{i18n.t('api.forbiddenModal.requiredPermissionsLabel')}</Text>
                <div style={{marginTop: 6}}>
                    {requiredPermissions.length > 0 ? (
                        <Space size={[6, 6]} wrap>
                            {requiredPermissions.map((perm) => (
                                <Tag key={perm} color="magenta">
                                    <code style={{fontSize: 12}}>{perm}</code>
                                </Tag>
                            ))}
                        </Space>
                    ) : (
                        <Text type="secondary" italic>
                            {i18n.t('api.forbiddenModal.noPermissionsRequired')}
                        </Text>
                    )}
                </div>
            </div>

            {serverMessage && (
                <div>
                    <Text type="secondary">{i18n.t('api.forbiddenModal.messageLabel')}</Text>
                    <div style={{marginTop: 4}}>
                        <Text style={{whiteSpace: 'pre-wrap'}}>{serverMessage}</Text>
                    </div>
                </div>
            )}
        </div>
    );
}

export function showForbiddenModal(context: ForbiddenContext, serverMessage?: string) {
    Modal.warning({
        title: i18n.t('api.forbiddenModal.title'),
        content: renderForbiddenModalContent(context, serverMessage),
        width: 520,
    });
}
