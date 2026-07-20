import {Modal, Table, Tag, Typography} from "antd";
import {useTranslation} from "react-i18next";
import type {ColumnsType} from "antd/es/table";
import type {SettingsChange} from "@/utils/settings-value.ts";

const {Text} = Typography;

export interface SettingsChangesModalProps {
    open: boolean;
    onClose: () => void;
    changes: SettingsChange[];
    keyTranslationMap: Map<string, string>;
}

function formatDisplayValue(value: unknown, t: (k: string) => string): string {
    if (value === null || value === undefined || value === '') {
        return t('components.settings.valueEmpty');
    }
    if (Array.isArray(value)) {
        return value.length === 0 ? t('components.settings.valueEmpty') : value.map((v) => String(v)).join(', ');
    }
    if (typeof value === 'boolean') {
        return String(value);
    }
    return String(value);
}

export function SettingsChangesModal(props: SettingsChangesModalProps) {
    const {t} = useTranslation();

    const columns: ColumnsType<SettingsChange> = [
        {
            title: t('components.settings.changesColumnKey'),
            dataIndex: 'key',
            key: 'key',
            render: (_, row) => (
                <Text>{props.keyTranslationMap.get(row.key) ?? row.key}</Text>
            ),
        },
        {
            title: t('components.settings.changesColumnBefore'),
            dataIndex: 'before',
            key: 'before',
            render: (_, row) => (
                row.isSecret
                    ? <Tag color="default">{t('components.settings.secretUnchangedTag')}</Tag>
                    : <Text type="secondary">{formatDisplayValue(row.before, t)}</Text>
            ),
        },
        {
            title: t('components.settings.changesColumnAfter'),
            dataIndex: 'after',
            key: 'after',
            render: (_, row) => (
                row.isSecret
                    ? <Tag color="blue">{t('components.settings.secretModifiedTag')}</Tag>
                    : <Text strong>{formatDisplayValue(row.after, t)}</Text>
            ),
        },
    ];

    return (
        <Modal
            title={t('components.settings.changesModalTitle', {count: props.changes.length})}
            open={props.open}
            onCancel={props.onClose}
            footer={null}
            width={720}
            centered
        >
            <Table<SettingsChange>
                rowKey="key"
                columns={columns}
                dataSource={props.changes}
                pagination={false}
                size="small"
            />
        </Modal>
    );
}
