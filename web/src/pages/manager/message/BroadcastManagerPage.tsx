import {Col, DatePicker, Form, Input, Row, Select} from "antd";
import dayjs from "dayjs";
import {useRef} from "react";
import {useTranslation} from "react-i18next";
import {ManagerPageContainer, type ManagerPageContainerRef} from "@/components/ManagerPageContainer.tsx";
import {
    BroadcastManagerController,
    type ManagerCreateBroadcastDTO,
    type ManagerUpdateBroadcastDTO,
} from "@/api/message/broadcast.api.ts";
import {AudienceType, BroadcastCategory, ScopeType} from "@/types/message/broadcast.types.ts";
import {getAudienceType, getBroadcastCategory} from "@/i18n/enum-helpers.ts";
import {useBroadcastTableColumns} from "@/components/columns/BroadcastEntityColumns.tsx";

const SYSTEM_SCOPE_ID = "0";

// Form fields carry a dayjs (freshly picked) or a string (loaded from the entity, kept as-is by
// getValueProps). Normalize both to an epoch-millis string; empty stays undefined so the backend
// applies its defaults (publishTime = now, expireTime = never).
function toTimestamp(value: unknown): string | undefined {
    if (!value) return undefined;
    if (typeof value === 'string') return value;
    if (typeof value === 'object' && 'isValid' in value && typeof (value as {isValid: () => boolean}).isValid === 'function') {
        const d = value as dayjs.Dayjs;
        return d.isValid() ? String(d.valueOf()) : undefined;
    }
    return undefined;
}

export default function BroadcastManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);
    const {t} = useTranslation();
    const columns = useBroadcastTableColumns();

    const categoryOptions = [
        {value: BroadcastCategory.ANNOUNCEMENT, label: getBroadcastCategory(BroadcastCategory.ANNOUNCEMENT)},
    ];

    const audienceOptions = [
        {value: AudienceType.ALL_USERS, label: getAudienceType(AudienceType.ALL_USERS)},
    ];

    return (
        <ManagerPageContainer
            ref={pageRef}
            entityName={t('entityNames.broadcast')}
            title={t('pages.broadcastManager.title')}
            subtitle={t('pages.broadcastManager.subtitle')}
            columns={columns}
            searchKeywords={['title', 'content']}
            editModalFormChildren={
                <>
                    <Row gutter={24}>
                        <Col span={24}>
                            <Form.Item
                                name="title"
                                label={t('pages.broadcastManager.modal.title.label')}
                                rules={[
                                    {required: true, message: t('pages.broadcastManager.modal.title.required')},
                                    {max: 256, message: t('pages.broadcastManager.modal.title.maxLength')},
                                ]}
                            >
                                <Input className="rounded-lg h-10" maxLength={256} showCount/>
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={24}>
                            <Form.Item
                                name="content"
                                label={t('pages.broadcastManager.modal.content.label')}
                                rules={[{required: true, message: t('pages.broadcastManager.modal.content.required')}]}
                            >
                                <Input.TextArea className="rounded-lg" rows={5} showCount/>
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item
                                name="category"
                                label={t('pages.broadcastManager.modal.category.label')}
                                initialValue={BroadcastCategory.ANNOUNCEMENT}
                            >
                                <Select options={categoryOptions} className="w-full"/>
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item
                                name="audienceType"
                                label={t('pages.broadcastManager.modal.audienceType.label')}
                                initialValue={AudienceType.ALL_USERS}
                                rules={[{required: true, message: t('pages.broadcastManager.modal.audienceType.required')}]}
                            >
                                <Select options={audienceOptions} className="w-full"/>
                            </Form.Item>
                        </Col>
                    </Row>
                    <Row gutter={24}>
                        <Col span={12}>
                            <Form.Item
                                name="publishTime"
                                label={t('pages.broadcastManager.modal.publishTime.label')}
                                help={t('pages.broadcastManager.modal.publishTime.help')}
                                getValueProps={(value) => {
                                    if (value && typeof value === 'string') {
                                        const ts = Number(value);
                                        if (!isNaN(ts)) return {value: dayjs(ts)};
                                    }
                                    return {value};
                                }}
                            >
                                <DatePicker
                                    className="w-full rounded-lg h-10 flex items-center"
                                    showTime
                                    format="YYYY-MM-DD HH:mm:ss"
                                    placeholder={t('pages.broadcastManager.modal.publishTime.placeholder')}
                                />
                            </Form.Item>
                        </Col>
                        <Col span={12}>
                            <Form.Item
                                name="expireTime"
                                label={t('pages.broadcastManager.modal.expireTime.label')}
                                help={t('pages.broadcastManager.modal.expireTime.help')}
                                getValueProps={(value) => {
                                    if (value && typeof value === 'string') {
                                        const ts = Number(value);
                                        if (!isNaN(ts)) return {value: dayjs(ts)};
                                    }
                                    return {value};
                                }}
                            >
                                <DatePicker
                                    className="w-full rounded-lg h-10 flex items-center"
                                    showTime
                                    format="YYYY-MM-DD HH:mm:ss"
                                    placeholder={t('pages.broadcastManager.modal.expireTime.placeholder')}
                                />
                            </Form.Item>
                        </Col>
                    </Row>
                </>
            }
            query={async (props) => {
                return (await BroadcastManagerController.query({
                    ...props,
                    scope: ScopeType.SYSTEM,
                    scopeId: SYSTEM_SCOPE_ID,
                })).data!;
            }}
            create={async (props) => {
                const dto = props as ManagerCreateBroadcastDTO;
                await BroadcastManagerController.create({
                    ...dto,
                    scope: ScopeType.SYSTEM,
                    scopeId: SYSTEM_SCOPE_ID,
                    publishTime: toTimestamp(dto.publishTime),
                    expireTime: toTimestamp(dto.expireTime),
                });
            }}
            update={async (props) => {
                const dto = props as ManagerUpdateBroadcastDTO;
                await BroadcastManagerController.update({
                    ...dto,
                    publishTime: toTimestamp(dto.publishTime),
                    expireTime: toTimestamp(dto.expireTime),
                });
            }}
            delete={async (props) => {
                await BroadcastManagerController.delete(props);
            }}
        />
    );
}
