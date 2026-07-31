import {Alert, Button, Collapse, DatePicker, Form, Input, message, Modal, Select, Spin, Tag} from "antd";
import {ExperimentOutlined} from "@ant-design/icons";
import {useState} from "react";
import {useTranslation} from "react-i18next";
import type {Dayjs} from "dayjs";
import {simulateStorageProviderRouting} from "@/api/resource/storage-provider-routing-rule.api.ts";
import type {
    EvaluationNodeTraceVO,
    RuleEvaluationTraceVO,
    SimulationResultVO,
} from "@/types/resource/storage-provider-routing-rule.types.ts";
import {ResourceFileType} from "@/types/resource/file-resource.types.ts";
import {getResourceFileType} from "@/i18n/enum-helpers.ts";
import {UserIdSelector} from "./selector/UserIdSelector.tsx";

interface SimulateFormValues {
    userId?: string | null;
    fileType: number;
    fileName?: string;
    fileContentType?: string;
    fileSize?: string;
    uploadTimestamp?: Dayjs | null;
}

function ConditionNodeRenderer({node, t}: { node: EvaluationNodeTraceVO; t: (key: string) => string }) {
    if (node.type === 'leaf') {
        return (
            <div style={{marginBottom: 4}}>
                <Tag color={node.matched ? 'green' : 'default'}>
                    {node.field} {node.operator} {node.expectedValue}
                </Tag>
                <span style={{color: '#888', fontSize: 12}}>
                    {t('pages.storageProviderRoutingRuleManager.simulate.result.actualLabel')}: {node.actualValue ?? 'null'}
                </span>
            </div>
        );
    }
    return (
        <div style={{borderLeft: '2px solid #ccc', paddingLeft: 12, marginBottom: 4}}>
            <Tag color={node.matched ? 'green' : 'default'}>{node.logic}</Tag>
            {(node.children ?? []).map((child, idx) => (
                <ConditionNodeRenderer key={idx} node={child} t={t}/>
            ))}
        </div>
    );
}

function RuleTracePanel({trace, t}: { trace: RuleEvaluationTraceVO; t: (key: string) => string }) {
    const header = (
        <span>
            {trace.ruleName}
            <span style={{color: '#888', fontSize: 12, marginLeft: 8}}>
                {t('pages.storageProviderRoutingRuleManager.simulate.result.priority')}: {trace.priority}
            </span>
            <Tag
                color={trace.matched ? 'green' : 'default'}
                style={{marginLeft: 8}}
            >
                {trace.matched
                    ? t('pages.storageProviderRoutingRuleManager.simulate.result.matched')
                    : t('pages.storageProviderRoutingRuleManager.simulate.result.notMatched')}
            </Tag>
        </span>
    );

    return (
        <Collapse
            size="small"
            style={{marginBottom: 8}}
            items={[{
                key: trace.ruleId,
                label: header,
                children: (
                    <div>
                        {trace.conditionTree == null
                            ? <Tag color="blue">{t('pages.storageProviderRoutingRuleManager.simulate.result.matchAll')}</Tag>
                            : <ConditionNodeRenderer node={trace.conditionTree} t={t}/>
                        }
                        {trace.matched && trace.selectedProviderIds && trace.selectedProviderIds.length > 0 && (
                            <div style={{marginTop: 8}}>
                                <span style={{fontSize: 12, color: '#888'}}>
                                    {t('pages.storageProviderRoutingRuleManager.simulate.result.selectedProviderIds')}:{' '}
                                    {trace.selectedProviderIds.join(', ')}
                                </span>
                            </div>
                        )}
                    </div>
                ),
            }]}
        />
    );
}

function SimulationResultPanel({result, t}: { result: SimulationResultVO; t: (key: string) => string }) {
    let headerAlert: React.ReactNode;
    if (result.noRuleMatched) {
        headerAlert = (
            <Alert
                type="error"
                message={t('pages.storageProviderRoutingRuleManager.simulate.result.noRuleMatched')}
                style={{marginBottom: 12}}
            />
        );
    } else if (result.finalProvider !== null) {
        headerAlert = (
            <Alert
                type="success"
                message={`${t('pages.storageProviderRoutingRuleManager.simulate.result.selectedProvider')}: ${result.finalProvider.name} (id: ${result.finalProvider.id})`}
                style={{marginBottom: 12}}
            />
        );
    } else {
        headerAlert = (
            <Alert
                type="warning"
                message={t('pages.storageProviderRoutingRuleManager.simulate.result.matchedNoProvider')}
                style={{marginBottom: 12}}
            />
        );
    }

    return (
        <div style={{marginTop: 16, borderTop: '1px solid #f0f0f0', paddingTop: 16}}>
            <div style={{fontWeight: 600, marginBottom: 8}}>
                {t('pages.storageProviderRoutingRuleManager.simulate.result.header')}
            </div>
            {headerAlert}
            {result.ruleTraces.map((trace) => (
                <RuleTracePanel key={trace.ruleId} trace={trace} t={t}/>
            ))}
        </div>
    );
}

export function SimulateRoutingButton() {
    const {t} = useTranslation();
    const [open, setOpen] = useState(false);
    const [running, setRunning] = useState(false);
    const [result, setResult] = useState<SimulationResultVO | null>(null);
    const [form] = Form.useForm<SimulateFormValues>();

    const handleClose = () => {
        if (!running) {
            setOpen(false);
            setResult(null);
            form.resetFields();
        }
    };

    const handleSimulate = () => {
        form.validateFields()
            .then((values) => {
                const trimmedName = values.fileName?.trim() || null;
                const dotIndex = trimmedName?.lastIndexOf('.') ?? -1;
                const derivedExtension = trimmedName != null && dotIndex > 0 && dotIndex < trimmedName.length - 1
                    ? trimmedName.slice(dotIndex + 1)
                    : null;
                const dto = {
                    userId: values.userId ?? null,
                    fileType: values.fileType,
                    fileName: trimmedName,
                    fileExtension: derivedExtension,
                    fileContentType: values.fileContentType?.trim() || null,
                    fileSize: values.fileSize?.trim() || null,
                    uploadTimestamp: values.uploadTimestamp
                        ? String(values.uploadTimestamp.valueOf())
                        : null,
                };
                setRunning(true);
                return simulateStorageProviderRouting(dto)
                    .then((res) => {
                        if (res.data) {
                            setResult(res.data);
                        }
                    })
                    .catch(() => {
                        void message.error(t('pages.storageProviderRoutingRuleManager.simulate.errors.simulateFailed'));
                    })
                    .finally(() => {
                        setRunning(false);
                    });
            })
            .catch(() => {});
    };

    const fileTypeOptions = [
        ResourceFileType.USER_AVATAR,
        ResourceFileType.TENANT_ICON,
        ResourceFileType.TENANT_MEMBER_AVATAR,
    ].map((v) => ({value: v, label: getResourceFileType(v)}));

    return (
        <>
            <Button
                icon={<ExperimentOutlined/>}
                onClick={() => setOpen(true)}
            >
                {t('pages.storageProviderRoutingRuleManager.simulate.button')}
            </Button>
            <Modal
                title={t('pages.storageProviderRoutingRuleManager.simulate.modalTitle')}
                open={open}
                onCancel={handleClose}
                footer={[
                    <Button key="close" onClick={handleClose} disabled={running}>
                        {t('pages.storageProviderRoutingRuleManager.simulate.actions.close')}
                    </Button>,
                    <Button key="run" type="primary" loading={running} onClick={handleSimulate}>
                        {t('pages.storageProviderRoutingRuleManager.simulate.actions.run')}
                    </Button>,
                ]}
                width={640}
                centered
                destroyOnHidden
            >
                <div style={{color: '#888', fontSize: 13, marginBottom: 12}}>
                    {t('pages.storageProviderRoutingRuleManager.simulate.description')}
                </div>
                <Form form={form} layout="vertical">
                    <Form.Item
                        label={t('pages.storageProviderRoutingRuleManager.simulate.form.userId')}
                        name="userId"
                    >
                        <UserIdSelector/>
                    </Form.Item>
                    <Form.Item
                        label={t('pages.storageProviderRoutingRuleManager.simulate.form.fileType')}
                        name="fileType"
                        rules={[{
                            required: true,
                            message: t('pages.storageProviderRoutingRuleManager.simulate.form.fileTypeRequired'),
                        }]}
                    >
                        <Select options={fileTypeOptions}/>
                    </Form.Item>
                    <Form.Item
                        label={t('pages.storageProviderRoutingRuleManager.simulate.form.fileName')}
                        name="fileName"
                        extra={t('pages.storageProviderRoutingRuleManager.simulate.form.fileNameExtra')}
                    >
                        <Input
                            maxLength={256}
                            placeholder={t('pages.storageProviderRoutingRuleManager.simulate.form.fileNamePlaceholder')}
                        />
                    </Form.Item>
                    <Form.Item
                        label={t('pages.storageProviderRoutingRuleManager.simulate.form.fileContentType')}
                        name="fileContentType"
                    >
                        <Input
                            placeholder={t('pages.storageProviderRoutingRuleManager.simulate.form.fileContentTypePlaceholder')}
                        />
                    </Form.Item>
                    <Form.Item
                        label={t('pages.storageProviderRoutingRuleManager.simulate.form.fileSize')}
                        name="fileSize"
                    >
                        <Input
                            placeholder={t('pages.storageProviderRoutingRuleManager.simulate.form.fileSizePlaceholder')}
                        />
                    </Form.Item>
                    <Form.Item
                        label={t('pages.storageProviderRoutingRuleManager.simulate.form.uploadTimestamp')}
                        name="uploadTimestamp"
                    >
                        <DatePicker showTime style={{width: '100%'}}/>
                    </Form.Item>
                </Form>
                {running && (
                    <div style={{textAlign: 'center', padding: '16px 0'}}>
                        <Spin/>
                    </div>
                )}
                {!running && result !== null && (
                    <SimulationResultPanel result={result} t={t}/>
                )}
            </Modal>
        </>
    );
}
