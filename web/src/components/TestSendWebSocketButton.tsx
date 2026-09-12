import {Button, Form, Input, message, Modal, Select, Switch, theme} from "antd";
import {SendOutlined, ApiOutlined} from "@ant-design/icons";
import {useEffect, useState, useRef, useCallback} from "react";
import {useTranslation} from "react-i18next";
import {useWebSocketConnection} from "@/compositions/use-websocket-connection.ts";
import {BUILTIN_WEBSOCKET_CHANNELS} from "@/constants/websocket-channels.ts";

export interface TestSendWebSocketButtonProps {
    disabled?: boolean;
}

interface TestSendWebSocketFormValues {
    channel: string;
    type: string;
    requiresAuth: boolean;
}

export function TestSendWebSocketButton({disabled}: TestSendWebSocketButtonProps) {
    const {t} = useTranslation();
    const {token} = theme.useToken();
    const [open, setOpen] = useState(false);
    const [form] = Form.useForm<TestSendWebSocketFormValues>();
    const [messages, setMessages] = useState<Array<{role: 'sent' | 'received', content: string, time: number}>>([]);
    const [payloadInput, setPayloadInput] = useState('{}');
    const [shouldConnect, setShouldConnect] = useState(false);
    const messagesEndRef = useRef<HTMLDivElement>(null);

    const requiresAuth = Form.useWatch('requiresAuth', form) ?? false;
    const selectedChannel = Form.useWatch('channel', form);

    // Extract channel from tags array if it exists
    const actualChannel = Array.isArray(selectedChannel) ? selectedChannel[0] : selectedChannel;

    // Stable callbacks to prevent useEffect re-runs
    const handleDisconnected = useCallback(() => {
        setShouldConnect(false);
        void message.warning(t('pages.systemSettingsManager.testSendWebSocket.connectionLost'));
    }, [t]);

    const handleError = useCallback(() => {
        setShouldConnect(false);
        void message.error(t('pages.systemSettingsManager.testSendWebSocket.connectionError'));
    }, [t]);

    const {connected, send, lastMessage} = useWebSocketConnection(
        actualChannel || 'test',
        {
            anonymous: !requiresAuth,
            enabled: shouldConnect,
            onDisconnected: handleDisconnected,
            onError: handleError
        }
    );

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    useEffect(() => {
        if (lastMessage && shouldConnect) {
            setMessages(prev => [...prev, {
                role: 'received',
                content: JSON.stringify(lastMessage, null, 2),
                time: Date.now()
            }]);
        }
    }, [lastMessage, shouldConnect]);

    const handleSend = () => {
        const values = form.getFieldsValue();

        if (!connected || !shouldConnect) {
            void message.error(t('pages.systemSettingsManager.testSendWebSocket.notConnected'));
            return;
        }

        if (!actualChannel || !values.type) {
            void message.error(t('pages.systemSettingsManager.testSendWebSocket.fillRequired'));
            return;
        }

        let payload: unknown;
        try {
            payload = JSON.parse(payloadInput);
        } catch (e) {
            void message.error(t('pages.systemSettingsManager.testSendWebSocket.invalidJson'));
            return;
        }

        const messageToSend = {
            channel: actualChannel,
            type: values.type,
            payload: payload
        };

        send(messageToSend);

        setMessages(prev => [...prev, {
            role: 'sent',
            content: JSON.stringify(messageToSend, null, 2),
            time: Date.now()
        }]);

        setPayloadInput('{}');
    };

    const handleConnect = () => {
        if (!actualChannel) {
            void message.error(t('pages.systemSettingsManager.testSendWebSocket.channelRequired'));
            return;
        }

        setShouldConnect(true);

        // Wait a bit for connection to establish
        setTimeout(() => {
            if (connected) {
                void message.success(t('pages.systemSettingsManager.testSendWebSocket.connected'));
            }
        }, 500);
    };

    const handleDisconnect = () => {
        setShouldConnect(false);
        void message.info(t('pages.systemSettingsManager.testSendWebSocket.disconnected'));
    };

    return (
        <>
            <Button
                icon={<SendOutlined/>}
                disabled={disabled}
                onClick={() => setOpen(true)}
            >
                {t('pages.systemSettingsManager.testSendWebSocket.button')}
            </Button>
            <Modal
                title={t('pages.systemSettingsManager.testSendWebSocket.modalTitle')}
                open={open}
                onCancel={() => {
                    setOpen(false);
                    form.resetFields();
                    setMessages([]);
                    setPayloadInput('{}');
                    setShouldConnect(false);
                }}
                footer={null}
                centered
                destroyOnClose
                width={1000}
            >
                <div className="flex gap-4" style={{height: '600px'}}>
                    {/* Left side - Configuration */}
                    <div className="flex flex-col gap-4" style={{width: '250px'}}>
                        <Form form={form} layout="vertical" initialValues={{requiresAuth: false}}>
                            <Form.Item
                                label={t('pages.systemSettingsManager.testSendWebSocket.requiresAuthLabel')}
                                name="requiresAuth"
                                valuePropName="checked"
                            >
                                <Switch disabled={shouldConnect} />
                            </Form.Item>

                            <Form.Item
                                label={t('pages.systemSettingsManager.testSendWebSocket.channelLabel')}
                                name="channel"
                                rules={[
                                    {required: true, message: t('pages.systemSettingsManager.testSendWebSocket.channelRequired')},
                                ]}
                            >
                                <Select
                                    placeholder={t('pages.systemSettingsManager.testSendWebSocket.channelPlaceholder')}
                                    options={BUILTIN_WEBSOCKET_CHANNELS.map(ch => ({label: ch, value: ch}))}
                                    showSearch
                                    allowClear
                                    mode="tags"
                                    maxCount={1}
                                    disabled={shouldConnect}
                                />
                            </Form.Item>

                            <Form.Item
                                label={t('pages.systemSettingsManager.testSendWebSocket.typeLabel')}
                                name="type"
                                rules={[
                                    {required: true, message: t('pages.systemSettingsManager.testSendWebSocket.typeRequired')},
                                ]}
                            >
                                <Input
                                    placeholder={t('pages.systemSettingsManager.testSendWebSocket.typePlaceholder')}
                                    autoComplete="off"
                                />
                            </Form.Item>
                        </Form>

                        <Button
                            icon={<ApiOutlined/>}
                            onClick={shouldConnect ? handleDisconnect : handleConnect}
                            block
                            type={connected ? 'primary' : 'default'}
                            loading={shouldConnect && !connected}
                        >
                            {shouldConnect
                                ? (connected
                                    ? t('pages.systemSettingsManager.testSendWebSocket.connected')
                                    : t('pages.systemSettingsManager.testSendWebSocket.connecting'))
                                : t('pages.systemSettingsManager.testSendWebSocket.connect')
                            }
                        </Button>
                    </div>

                    {/* Right side - Chat-like interface */}
                    <div className="flex-1 flex flex-col border border-gray-200 rounded">
                        {/* Messages area */}
                        <div className="flex-1 overflow-y-auto p-4 space-y-2" style={{maxHeight: '500px'}}>
                            {messages.length === 0 ? (
                                <div className="text-center text-gray-400 mt-8">
                                    {t('pages.systemSettingsManager.testSendWebSocket.noMessages')}
                                </div>
                            ) : (
                                messages.map((msg, idx) => (
                                    <div key={idx} className={`flex ${msg.role === 'sent' ? 'justify-end' : 'justify-start'}`}>
                                        <div
                                            className={`max-w-[80%] rounded p-2`}
                                            style={{
                                                backgroundColor: msg.role === 'sent' ? token.colorPrimaryBg : '#f5f5f5',
                                                color: msg.role === 'sent' ? token.colorPrimary : '#000'
                                            }}
                                        >
                                            <pre className="text-xs whitespace-pre-wrap break-all m-0">
                                                {msg.content}
                                            </pre>
                                            <div
                                                className="text-xs mt-1"
                                                style={{
                                                    color: msg.role === 'sent' ? token.colorPrimary : '#999',
                                                    opacity: 0.7
                                                }}
                                            >
                                                {new Date(msg.time).toLocaleTimeString()}
                                            </div>
                                        </div>
                                    </div>
                                ))
                            )}
                            <div ref={messagesEndRef} />
                        </div>

                        {/* Input area */}
                        <div className="border-t border-gray-200 p-4">
                            <div className="mb-2 text-sm text-gray-500">
                                {t('pages.systemSettingsManager.testSendWebSocket.payloadLabel')}
                            </div>
                            <Input.TextArea
                                value={payloadInput}
                                onChange={(e) => setPayloadInput(e.target.value)}
                                placeholder={t('pages.systemSettingsManager.testSendWebSocket.payloadPlaceholder')}
                                autoSize={{minRows: 3, maxRows: 6}}
                                disabled={!shouldConnect || !connected}
                                onPressEnter={(e) => {
                                    if (e.ctrlKey || e.metaKey) {
                                        handleSend();
                                    }
                                }}
                            />
                            <div className="flex justify-between items-center mt-2">
                                <span className="text-xs text-gray-400">
                                    {t('pages.systemSettingsManager.testSendWebSocket.sendHint')}
                                </span>
                                <Button
                                    type="primary"
                                    icon={<SendOutlined/>}
                                    onClick={handleSend}
                                    disabled={!shouldConnect || !connected}
                                >
                                    {t('pages.systemSettingsManager.testSendWebSocket.send')}
                                </Button>
                            </div>
                        </div>
                    </div>
                </div>
            </Modal>
        </>
    );
}
