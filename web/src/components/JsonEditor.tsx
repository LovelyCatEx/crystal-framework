import {Button, Card, Input, InputNumber, Select, Switch, Tag, Tabs, Tooltip, Typography} from "antd";
import {
    DeleteOutlined,
    PlusOutlined,
    EditOutlined,
    DownOutlined,
    RightOutlined,
    CheckCircleOutlined,
    ExclamationCircleOutlined
} from "@ant-design/icons";
import {useEffect, useState} from "react";
import TextArea from "antd/es/input/TextArea";

const {Text} = Typography;
const {Option} = Select;

type JsonValue = string | number | boolean | null | JsonObject | JsonArray;
type JsonObject = { [key: string]: JsonValue };
type JsonArray = JsonValue[];

interface JsonEditorProps {
    value?: string;
    onChange?: (value: string) => void;
    placeholder?: string;
}

type ValueType = 'string' | 'number' | 'boolean' | 'null' | 'object' | 'array';

const TYPES = [
    {label: '字符串', value: 'string'},
    {label: '数字', value: 'number'},
    {label: '布尔', value: 'boolean'},
    {label: '对象', value: 'object'},
    {label: '数组', value: 'array'},
    {label: '空值', value: 'null'},
];

const ROOT_TYPES = [
    {label: '对象', value: 'object'},
    {label: '数组', value: 'array'},
];

const getDefaultValue = (type: ValueType): JsonValue => {
    switch (type) {
        case 'string':
            return '';
        case 'number':
            return 0;
        case 'boolean':
            return false;
        case 'object':
            return {};
        case 'array':
            return [];
        case 'null':
            return null;
        default:
            return '';
    }
};

const getValueType = (value: JsonValue): ValueType => {
    if (value === null) return 'null';
    if (Array.isArray(value)) return 'array';
    if (typeof value === 'object') return 'object';
    if (typeof value === 'boolean') return 'boolean';
    if (typeof value === 'number') return 'number';
    return 'string';
};

interface JsonNodeProps {
    name: string | number;
    value: JsonValue;
    onUpdate: (value: JsonValue) => void;
    onDelete: () => void;
    depth?: number;
    isRoot?: boolean;
    isArrayItem?: boolean;
}

function JsonNode({name, value, onUpdate, onDelete, depth = 0, isRoot = false, isArrayItem = false}: JsonNodeProps) {
    const [collapsed, setCollapsed] = useState(false);
    const type = getValueType(value);

    const handleChangeType = (newType: ValueType) => {
        onUpdate(getDefaultValue(newType));
    };

    const handleChangeValue = (newValue: string | number | boolean) => {
        onUpdate(newValue);
    };

    const handleChildUpdate = (key: string | number, newValue: JsonValue) => {
        if (type === 'object' && typeof value === 'object' && value !== null && !Array.isArray(value)) {
            onUpdate({...value, [key]: newValue});
        } else if (type === 'array' && Array.isArray(value)) {
            const newArray = [...value];
            newArray[key as number] = newValue;
            onUpdate(newArray);
        }
    };

    const handleChildDelete = (key: string | number) => {
        if (type === 'object' && typeof value === 'object' && value !== null && !Array.isArray(value)) {
            const {[key]: _, ...rest} = value;
            onUpdate(rest);
        } else if (type === 'array' && Array.isArray(value)) {
            const newArray = [...value];
            newArray.splice(key as number, 1);
            onUpdate(newArray);
        }
    };

    const handleAddChild = () => {
        if (type === 'object' && typeof value === 'object' && value !== null && !Array.isArray(value)) {
            let baseKey = "newKey";
            let counter = 1;
            let newKey = baseKey;
            while (newKey in value) {
                newKey = `${baseKey}_${counter++}`;
            }
            onUpdate({...value, [newKey]: ""});
        } else if (type === 'array' && Array.isArray(value)) {
            onUpdate([...value, ""]);
        }
    };

    const handleRenameKey = (oldKey: string | number, newKey: string) => {
        if (oldKey === newKey || !newKey || typeof value !== 'object' || value === null || Array.isArray(value)) return;
        const {[oldKey]: val, ...rest} = value as JsonObject;
        onUpdate({...rest, [newKey]: val});
    };

    const renderLabel = () => {
        if (isRoot) return <Text strong className="text-blue-600">ROOT</Text>;

        return (
            <div className="flex items-center gap-2">
                <Text type="secondary" className="font-mono text-xs">[{type}]</Text>
                {typeof name === 'number' ? (
                    <Tag className="font-mono text-xs m-0">Index {name}</Tag>
                ) : (
                    <Input
                        size="small"
                        className="w-32 font-medium"
                        defaultValue={name}
                        onBlur={(e) => handleRenameKey(name, e.target.value)}
                    />
                )}
            </div>
        );
    };

    const renderValueEditor = () => {
        switch (type) {
            case 'string':
                return (
                    <Input
                        size="small"
                        className="flex-1"
                        value={value as string}
                        onChange={(e) => handleChangeValue(e.target.value)}
                    />
                );
            case 'number':
                return (
                    <InputNumber
                        size="small"
                        className="w-full"
                        value={value as number}
                        onChange={(v) => handleChangeValue(v ?? 0)}
                    />
                );
            case 'boolean':
                return (
                    <Switch
                        size="small"
                        checked={value as boolean}
                        onChange={handleChangeValue}
                    />
                );
            case 'null':
                return <Text disabled italic>null</Text>;
            case 'object':
            case 'array':
                const count = type === 'object'
                    ? (typeof value === 'object' && value !== null && !Array.isArray(value) ? Object.keys(value).length : 0)
                    : (Array.isArray(value) ? value.length : 0);
                return (
                    <Text type="secondary" className="text-xs">
                        {type === 'object' ? `{ ${count} items }` : `[ ${count} items ]`}
                    </Text>
                );
            default:
                return null;
        }
    };

    const isComplex = type === 'object' || type === 'array';

    return (
        <div className={`flex flex-col mb-2 ${depth > 0 ? 'ml-6 border-l border-gray-100 pl-4' : ''}`}>
            <div className="flex items-center justify-between group py-1 hover:bg-gray-50 rounded px-2 transition-all">
                <div className="flex items-center gap-3 flex-1">
                    {isComplex && (
                        <div
                            className="cursor-pointer hover:text-blue-500 transition-colors"
                            onClick={() => setCollapsed(!collapsed)}
                        >
                            {collapsed ? <RightOutlined/> : <DownOutlined/>}
                        </div>
                    )}

                    {renderLabel()}

                    <div className="flex-1 max-w-md flex items-center gap-2">
                        {!isComplex && renderValueEditor()}
                    </div>
                </div>

                <div className="flex items-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                    <Select
                        size="small"
                        value={type}
                        className="w-28"
                        onChange={handleChangeType}
                        bordered={false}
                    >
                        {(isRoot ? ROOT_TYPES : TYPES).map(t => <Option key={t.value} value={t.value}>{t.label}</Option>)}
                    </Select>

                    {isComplex && (
                        <Button
                            size="small"
                            type="text"
                            icon={<PlusOutlined className="text-green-500"/>}
                            onClick={handleAddChild}
                        />
                    )}

                    {!isRoot && (
                        <Button
                            size="small"
                            type="text"
                            danger
                            icon={<DeleteOutlined/>}
                            onClick={onDelete}
                        />
                    )}
                </div>
            </div>

            {!collapsed && isComplex && (
                <div className="mt-1">
                    {type === 'object' && typeof value === 'object' && value !== null && !Array.isArray(value) ? (
                        Object.entries(value).length === 0 ? (
                            <div className="ml-8 py-1 italic text-gray-400 text-xs">Empty Object</div>
                        ) : (
                            Object.entries(value).map(([k, v]) => (
                                <JsonNode
                                    key={k}
                                    name={k}
                                    value={v}
                                    depth={depth + 1}
                                    onUpdate={(val) => handleChildUpdate(k, val)}
                                    onDelete={() => handleChildDelete(k)}
                                />
                            ))
                        )
                    ) : type === 'array' && Array.isArray(value) ? (
                        value.length === 0 ? (
                            <div className="ml-8 py-1 italic text-gray-400 text-xs">Empty Array</div>
                        ) : (
                            value.map((v, i) => (
                                <JsonNode
                                    key={i}
                                    name={i}
                                    value={v}
                                    depth={depth + 1}
                                    onUpdate={(val) => handleChildUpdate(i, val)}
                                    onDelete={() => handleChildDelete(i)}
                                    isArrayItem={true}
                                />
                            ))
                        )
                    ) : null}
                </div>
            )}
        </div>
    );
}

export function JsonEditor({value = '{}', onChange, placeholder}: JsonEditorProps) {
    const [activeTab, setActiveTab] = useState('visual');
    const [jsonData, setJsonData] = useState<JsonValue>({});
    const [jsonText, setJsonText] = useState(value);
    const [isValid, setIsValid] = useState(true);

    useEffect(() => {
        try {
            const parsed = JSON.parse(value || '{}');

            if (typeof parsed !== 'object' || parsed === null) {
                const wrapped = {value: parsed};
                setJsonData(wrapped);
                setJsonText(JSON.stringify(wrapped, null, 2));
                onChange?.(JSON.stringify(wrapped));
            } else {
                setJsonData(parsed);
                setJsonText(JSON.stringify(parsed, null, 2));
            }
            setIsValid(true);
        } catch {
            setJsonText(value);
            setIsValid(false);
        }
    }, [value]);

    const handleVisualChange = (newData: JsonValue) => {
        setJsonData(newData);
        setJsonText(JSON.stringify(newData, null, 2));
        onChange?.(JSON.stringify(newData));
        setIsValid(true);
    };

    const handleTextChange = (text: string) => {
        setJsonText(text);
        try {
            const parsed = JSON.parse(text);
            setJsonData(parsed);
            setIsValid(true);
            onChange?.(JSON.stringify(parsed));
        } catch {
            setIsValid(false);
        }
    };

    const tabItems = [
        {
            key: 'visual',
            label: (
                <span className="flex items-center gap-1">
                    <EditOutlined/> 可视化
                </span>
            ),
            children: (
                <div className="overflow-auto max-h-[500px] p-4">
                    <JsonNode
                        isRoot
                        name="Root"
                        value={jsonData}
                        onUpdate={handleVisualChange}
                        onDelete={() => {}}
                    />
                </div>
            ),
        },
        {
            key: 'json',
            label: (
                <span className="flex items-center gap-1">
                    {'{ }'} 源码
                </span>
            ),
            children: (
                <div className="relative">
                    <TextArea
                        value={jsonText}
                        onChange={(e) => handleTextChange(e.target.value)}
                        className={`font-mono text-sm min-h-[300px] border-0 resize-none ${!isValid ? 'bg-red-50' : ''}`}
                        spellCheck={false}
                    />
                    {!isValid && (
                        <div
                            className="absolute bottom-4 right-4 bg-red-500 text-white px-3 py-1 rounded-full text-xs shadow-lg flex items-center gap-2">
                            <ExclamationCircleOutlined/>
                            无效的 JSON 格式
                        </div>
                    )}
                </div>
            ),
        },
    ];

    return (
        <Card
            size="small"
            className="shadow-sm"
            tabProps={{size: 'small'}}
            tabList={[
                {
                    key: 'visual',
                    tab: (
                        <span className="flex items-center gap-1 px-2">
                            <EditOutlined/> 可视化
                        </span>
                    ),
                },
                {
                    key: 'json',
                    tab: (
                        <span className="flex items-center gap-1 px-2">
                            {'{ }'} 源码
                        </span>
                    ),
                },
            ]}
            activeTabKey={activeTab}
            onTabChange={(key) => setActiveTab(key)}
        >
            {activeTab === 'visual' ? (
                <div className="overflow-auto max-h-[500px] p-2">
                    <JsonNode
                        isRoot
                        name="Root"
                        value={jsonData}
                        onUpdate={handleVisualChange}
                        onDelete={() => {}}
                    />
                </div>
            ) : (
                <div className="relative" style={{minHeight: 300}}>
                    <TextArea
                        value={jsonText}
                        onChange={(e) => handleTextChange(e.target.value)}
                        className={`font-mono text-sm border-0 resize-none ${!isValid ? 'bg-red-50' : ''}`}
                        style={{minHeight: 300}}
                        spellCheck={false}
                    />
                    {!isValid && (
                        <div
                            className="absolute bottom-4 right-4 bg-red-500 text-white px-3 py-1 rounded-full text-xs shadow-lg flex items-center gap-2">
                            <ExclamationCircleOutlined/>
                            无效的 JSON 格式
                        </div>
                    )}
                </div>
            )}

            <div className="px-4 py-2 border-t border-gray-100 flex items-center justify-between bg-gray-50">
                <Text type="secondary" className="text-xs">
                    {isValid ? (
                        <span className="flex items-center gap-1 text-green-600">
                            <CheckCircleOutlined/> 格式有效
                        </span>
                    ) : (
                        <span className="flex items-center gap-1 text-red-600">
                            <ExclamationCircleOutlined/> 格式错误
                        </span>
                    )}
                </Text>
            </div>
        </Card>
    );
}
