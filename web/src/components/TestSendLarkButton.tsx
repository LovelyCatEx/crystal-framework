import {Button, Form, Input, message, Modal, Select} from "antd";
import {SendOutlined} from "@ant-design/icons";
import {useState} from "react";
import {useTranslation} from "react-i18next";
import {ChannelType, testSendMessage} from "@/api/system/system-settings.api.ts";
import {MessageChainEditor} from "@/components/message-chain-editor";

export type LarkRecipientIdType = 'openId' | 'userId' | 'unionId' | 'email' | 'chatId';

interface TestSendLarkFormValues {
    idType: LarkRecipientIdType;
    idValue: string;
    content?: string;
}

const ID_TYPE_OPTIONS: LarkRecipientIdType[] = ['openId', 'userId', 'unionId', 'email', 'chatId'];

export function TestSendLarkButton() {
    const {t} = useTranslation();
    const [open, setOpen] = useState(false);
    const [sending, setSending] = useState(false);
    const [form] = Form.useForm<TestSendLarkFormValues>();

    const handleOk = () => {
        form.validateFields()
            .then((values) => {
                setSending(true);
                return testSendMessage({
                    channelType: ChannelType.LARK,
                    recipient: { [values.idType]: values.idValue },
                    content: values.content?.trim() || undefined,
                })
                    .then(() => {
                        void message.success(t('pages.systemSettingsManager.testSendLark.sendSuccess'));
                    })
                    .catch(() => {
                        // handleApiResponse already shows error message on non-200 codes
                    })
                    .finally(() => {
                        setSending(false);
                    });
            })
            .catch(() => {});
    };

    return (
        <>
            <Button
                icon={<SendOutlined/>}
                onClick={() => setOpen(true)}
            >
                {t('pages.systemSettingsManager.testSendLark.button')}
            </Button>
            <Modal
                title={t('pages.systemSettingsManager.testSendLark.modalTitle')}
                open={open}
                confirmLoading={sending}
                onOk={handleOk}
                onCancel={() => {
                    if (!sending) {
                        setOpen(false);
                        form.resetFields();
                    }
                }}
                okText={t('pages.systemSettingsManager.testSendLark.confirm')}
                cancelText={t('pages.systemSettingsManager.testSendLark.cancel')}
                centered
                destroyOnHidden
                width={560}
            >
                <Form
                    form={form}
                    layout="vertical"
                    initialValues={{idType: 'openId' as LarkRecipientIdType}}
                >
                    <Form.Item
                        label={t('pages.systemSettingsManager.testSendLark.idTypeLabel')}
                        name="idType"
                        rules={[{required: true}]}
                    >
                        <Select
                            options={ID_TYPE_OPTIONS.map((idType) => ({
                                value: idType,
                                label: t(`pages.systemSettingsManager.testSendLark.idTypes.${idType}`),
                            }))}
                        />
                    </Form.Item>
                    <Form.Item
                        label={t('pages.systemSettingsManager.testSendLark.idValueLabel')}
                        name="idValue"
                        rules={[{required: true, message: t('pages.systemSettingsManager.testSendLark.idValueRequired')}]}
                    >
                        <Input
                            placeholder={t('pages.systemSettingsManager.testSendLark.idValuePlaceholder')}
                            autoComplete="off"
                        />
                    </Form.Item>
                    <Form.Item
                        label={t('pages.systemSettingsManager.testSendLark.contentLabel')}
                        name="content"
                        extra={t('pages.systemSettingsManager.testSendLark.contentHint')}
                    >
                        <MessageChainEditor
                            minRows={4}
                            maxRows={8}
                            placeholder={t('pages.systemSettingsManager.testSendLark.contentPlaceholder')}
                        />
                    </Form.Item>
                </Form>
            </Modal>
        </>
    );
}
