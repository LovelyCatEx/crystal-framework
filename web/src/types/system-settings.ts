export interface GetSystemSettingsSchemaData {
    groups: string[];
    items: {
        [key: string]: SystemSettingsSchema
    }
}

export interface SystemSettingsSchema {
    sort: number;
    valueType: string;
    value: string | null;
    defaultValue: string | null;
    group: string | null;
}

export enum SystemSettingsItemValueType {
    STRING = 'STRING',
    NUMBER = 'NUMBER',
    DECIMAL = 'DECIMAL',
    BOOLEAN = 'BOOLEAN',
}