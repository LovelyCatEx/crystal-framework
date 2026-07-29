import {Checkbox, Form, Input, InputNumber, Radio, Select, Switch, theme} from "antd";
import {useTranslation} from "react-i18next";
import type {SystemSettingsSchema} from "@/types/system/system-settings.types.ts";
import {SystemSettingsItemValueType} from "@/types/system/system-settings.types.ts";
import {isArrayValueType} from "@/utils/settings-value.ts";
import type {SettingsItemRenderer} from "./types.ts";

const {useToken} = theme;

export interface SettingsItemProps {
    settingsKey: string;
    schema: SystemSettingsSchema;
    loading?: boolean;
    keyTranslationMap: Map<string, string>;
    enumTranslator: (settingsKey: string, enumValue: string) => string;
    customRenderer?: SettingsItemRenderer;
}

export function SettingsItem(props: SettingsItemProps) {
    const {token} = useToken();
    const {t} = useTranslation();
    const formItemProps: Record<string, unknown> | undefined =
        props.schema.valueType === SystemSettingsItemValueType.BOOLEAN
            ? {getValueProps: (value: string | boolean) => ({checked: value === 'true' || value === true})}
            : undefined;

    return (
        <Form.Item
            key={props.settingsKey}
            name={props.settingsKey}
            label={
                <span className="text-sm font-medium" style={{color: token.colorTextSecondary}}>
                    {props.keyTranslationMap.get(props.settingsKey) ?? props.settingsKey}
                </span>
            }
            className="mb-0"
            {...formItemProps}
        >
            {props.customRenderer ? (
                props.customRenderer({
                    settingsKey: props.settingsKey,
                    schema: props.schema,
                    loading: props.loading
                })
            ) : props.schema.isSecret ? (
                <Input.Password
                    className="rounded-lg h-10"
                    placeholder={
                        props.schema.hasValue
                            ? t('components.settings.secretPlaceholderConfigured')
                            : t('components.settings.secretPlaceholderEmpty')
                    }
                    disabled={props.loading}
                    autoComplete="new-password"
                />
            ) : props.schema.valueType == SystemSettingsItemValueType.STRING ? (
                <Input
                    className="rounded-lg h-10"
                    placeholder={props.schema.defaultValue ?? ''}
                    disabled={props.loading}
                />
            ) : props.schema.valueType == SystemSettingsItemValueType.NUMBER ? (
                <InputNumber
                    className="w-full rounded-lg h-10"
                    defaultValue={Number.parseFloat(props.schema.defaultValue ?? '0')}
                    disabled={props.loading}
                />
            ) : props.schema.valueType == SystemSettingsItemValueType.DECIMAL ? (
                <InputNumber
                    className="w-full rounded-lg h-10"
                    defaultValue={Number.parseFloat(props.schema.defaultValue ?? '0')}
                    disabled={props.loading}
                />
            ) : props.schema.valueType == SystemSettingsItemValueType.BOOLEAN ? (
                <Switch disabled={props.loading}/>
            ) : props.schema.valueType == SystemSettingsItemValueType.ENUM_SINGLE ? (
                <Radio.Group
                    className="flex flex-col space-y-2"
                    disabled={props.loading}
                    options={(props.schema.enumValues ?? []).map((item) => ({
                        value: item,
                        label: props.enumTranslator(props.settingsKey, item),
                    }))}
                />
            ) : props.schema.valueType == SystemSettingsItemValueType.ENUM_MULTIPLE ? (
                <Checkbox.Group
                    className="flex flex-col space-y-2"
                    disabled={props.loading}
                    options={(props.schema.enumValues ?? []).map((item) => ({
                        value: item,
                        label: props.enumTranslator(props.settingsKey, item),
                    }))}
                />
            ) : props.schema.valueType == SystemSettingsItemValueType.BOOLEAN_ARRAY ? (
                <Select
                    mode="tags"
                    className="w-full"
                    disabled={props.loading}
                    tokenSeparators={[',']}
                    options={[
                        {value: 'true', label: 'true'},
                        {value: 'false', label: 'false'},
                    ]}
                />
            ) : isArrayValueType(props.schema.valueType) ? (
                <Select
                    mode="tags"
                    className="w-full"
                    disabled={props.loading}
                    tokenSeparators={[',']}
                    open={false}
                    suffixIcon={null}
                    placeholder={props.schema.defaultValue ?? ''}
                />
            ) : (
                <span className="text-red-500 text-sm">NO RENDER FOUND FOR THIS SETTINGS ITEM</span>
            )}
        </Form.Item>
    );
}
