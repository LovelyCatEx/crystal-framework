import type {ReactNode} from "react";
import {Form, theme} from "antd";
import type {SystemSettingsSchema} from "@/types/system/system-settings.types.ts";
import {SettingsItem} from "./SettingsItem.tsx";
import type {SettingsGroupExtraRenderer, SettingsItemRenderer} from "./types.ts";

const {useToken} = theme;

export interface SettingsGroupProps {
    group: string;
    items: [string, SystemSettingsSchema][];
    loading: boolean;
    isFirst: boolean;
    groupTranslationMap: Map<string, { label: string; icon?: ReactNode }>;
    keyTranslationMap: Map<string, string>;
    enumTranslator: (settingsKey: string, enumValue: string) => string;
    itemRenderers?: Map<string, SettingsItemRenderer>;
    groupExtraRenderers?: Map<string, SettingsGroupExtraRenderer>;
}

export function SettingsGroup(props: SettingsGroupProps) {
    const {token} = useToken();
    const translatedGroup = props.groupTranslationMap.get(props.group);
    const form = Form.useFormInstance();
    const extraRenderer = props.groupExtraRenderers?.get(props.group);

    return (
        <div className={props.isFirst ? "" : "mt-8"}>
            <div
                className="flex items-center mb-6 pb-3"
                style={{borderBottom: `1px solid ${token.colorBorder}`}}
            >
                {translatedGroup?.icon && (
                    <span className="text-xl mr-3" style={{color: token.colorPrimary}}>
                        {translatedGroup.icon}
                    </span>
                )}
                <h3 className="text-lg font-semibold" style={{color: token.colorTextHeading}}>
                    {translatedGroup?.label ?? props.group}
                </h3>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {props.items.map(([key, value]) => (
                    <div
                        key={key}
                        className="bg-slate-50/50 dark:bg-slate-200/5 rounded-xl p-4 hover:bg-slate-50 dark:hover:bg-slate-200/10 transition-colors"
                    >
                        <SettingsItem
                            settingsKey={key}
                            schema={value}
                            loading={props.loading}
                            keyTranslationMap={props.keyTranslationMap}
                            enumTranslator={props.enumTranslator}
                            customRenderer={props.itemRenderers?.get(key)}
                        />
                    </div>
                ))}
            </div>

            {extraRenderer && (
                <div className="mt-6">
                    {extraRenderer({group: props.group, form})}
                </div>
            )}
        </div>
    );
}
