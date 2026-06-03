import {Button, Form, Input, message, Modal} from "antd";
import {SendOutlined} from "@ant-design/icons";
import {useState} from "react";
import {useTranslation} from "react-i18next";
import {testSendEmail} from "@/api/system/system-settings.api.ts";

export interface TestSendEmailButtonProps {
    disabled?: boolean;
}

interface TestSendEmailFormValues {
    email: string;
}

export function TestSendEmailButton({disabled}: TestSendEmailButtonProps) {
    const {t} = useTranslation();
    const [open, setOpen] = useState(false);
    const [sending, setSending] = useState(false);
    const [form] = Form.useForm<TestSendEmailFormValues>();

    const handleOk = () => {
        form.validateFields()
            .then((values) => {
                setSending(true);
                return testSendEmail(values.email)
                    .then(() => {
                        void message.success(t('pages.systemSettingsManager.testSendEmail.sendSuccess'));
                        setOpen(false);
                        form.resetFields();
                    })
                    .catch(() => {
                        void message.error(t('pages.systemSettingsManager.testSendEmail.sendFailed'));
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
                disabled={disabled}
                onClick={() => setOpen(true)}
            >
                {t('pages.systemSettingsManager.testSendEmail.button')}
            </Button>
            <Modal
                title={t('pages.systemSettingsManager.testSendEmail.modalTitle')}
                open={open}
                confirmLoading={sending}
                onOk={handleOk}
                onCancel={() => {
                    if (!sending) {
                        setOpen(false);
                        form.resetFields();
                    }
                }}
                okText={t('pages.systemSettingsManager.testSendEmail.confirm')}
                cancelText={t('pages.systemSettingsManager.testSendEmail.cancel')}
                centered
                destroyOnHidden
            >
                <Form form={form} layout="vertical">
                    <Form.Item
                        label={t('pages.systemSettingsManager.testSendEmail.emailLabel')}
                        name="email"
                        rules={[
                            {required: true, message: t('pages.systemSettingsManager.testSendEmail.emailRequired')},
                            {type: 'email', message: t('pages.systemSettingsManager.testSendEmail.emailInvalid')},
                        ]}
                    >
                        <Input
                            placeholder={t('pages.systemSettingsManager.testSendEmail.emailPlaceholder')}
                            autoComplete="off"
                        />
                    </Form.Item>
                </Form>
            </Modal>
        </>
    );
}
