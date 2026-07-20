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
    enumValues: string[] | null;
    tab: string | null;
    group: string | null;
    isSecret: boolean;
    hasValue: boolean;
}

export enum SystemSettingsItemValueType {
    STRING = 'STRING',
    NUMBER = 'NUMBER',
    DECIMAL = 'DECIMAL',
    BOOLEAN = 'BOOLEAN',
    ENUM_SINGLE = 'ENUM_SINGLE',
    ENUM_MULTIPLE = 'ENUM_MULTIPLE',
    STRING_ARRAY = 'STRING_ARRAY',
    NUMBER_ARRAY = 'NUMBER_ARRAY',
    DECIMAL_ARRAY = 'DECIMAL_ARRAY',
    BOOLEAN_ARRAY = 'BOOLEAN_ARRAY',
}

export interface SystemMaintenanceStatusVO {
    canAccess: boolean;
    maintenanceMode: boolean
}