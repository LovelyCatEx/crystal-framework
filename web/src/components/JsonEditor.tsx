import {Button, Card, Divider, Input, Select, Space, Tag, Tooltip, Typography} from "antd";
import {DeleteOutlined, PlusOutlined, EditOutlined, EyeOutlined} from "@ant-design/icons";
import {useEffect, useState} from "react";
import TextArea from "antd/es/input/TextArea";

const {Text} = Typography;

type JsonValue = string | number | boolean | null | JsonObject | JsonArray;
type JsonObject = { [key: string]: JsonValue };
type JsonArray = JsonValue[];

interface JsonEditorProps {
    value?: string;
    onChange?: (value: string) => void;
    placeholder?: string;
}

type ValueType = 'string' | 'number' | 'boolean' | 'null' | 'object' | 'array';

interface JsonNodeProps {
    name: string;
    value: JsonValue;
    onChange: (oldName: string, newName: string | null, value: JsonValue) => void;
    onDelete: () => void;
    depth?: number;
    isArrayItem?: boolean;
}

function getValueType(value: JsonValue): ValueType {
    if (value === null) return 'null';
    if (Array.isArray(value)) return 'array';
    if (typeof value === 'object') return 'object';
    if (typeof value === 'boolean') return 'boolean';
    if (typeof value === 'number') return 'number';
    return 'string';
}

function JsonNode({name, value, onChange, onDelete, depth = 0, isArrayItem = false}: JsonNodeProps) {
    const [isExpanded, setIsExpanded] = useState(true);
    const [editingName, setEditingName] = useState(name);
    const valueType = getValueType(value);
    const indent = depth * 16;

    const handleTypeChange = (newType: ValueType) => {
        let newValue: JsonValue;
        switch (newType) {
            case 'string':
                newValue = '';
                break;
            case 'number':
                newValue = 0;
                break;
            case 'boolean':
                newValue = false;
                break;
            case 'null':
                newValue = null;
                break;
            case 'object':
                newValue = {};
                break;
            case 'array':
                newValue = [];
                break;
            default:
                newValue = '';
        }
        onChange(name, null, newValue);
    };

    const handleValueChange = (newValue: string | number | boolean) => {
        onChange(name, null, newValue);
    };

    const handleNameChange = () => {
        if (editingName !== name && editingName.trim() !== '') {
            onChange(name, editingName.trim(), value);
        } else {
            setEditingName(name);
        }
    };

    const handleObjectChange = (oldKey: string, newKey: string | null, newValue: JsonValue) => {
        if (typeof value === 'object' && value !== null && !Array.isArray(value)) {
            if (newKey === null) {
                const newObj = {...value, [oldKey]: newValue};
                onChange(name, null, newObj);
            } else if (newKey !== oldKey) {
                const newObj: JsonObject = {};
                for (const [k, v] of Object.entries(value)) {
                    if (k === oldKey) {
                        newObj[newKey] = newValue;
                    } else {
                        newObj[k] = v;
                    }
                }
                onChange(name, null, newObj);
            }
        }
    };

    const handleArrayChange = (index: number, newValue: JsonValue) => {
        if (Array.isArray(value)) {
            const newArr = [...value];
            newArr[index] = newValue;
            onChange(name, null, newArr);
        }
    };

    const handleAddField = () => {
        if (typeof value === 'object' && value !== null && !Array.isArray(value)) {
            const newKey = `field${Object.keys(value).length + 1}`;
            onChange(name, null, {...value, [newKey]: ''});
        }
    };

    const handleAddArrayItem = () => {
        if (Array.isArray(value)) {
            onChange(name, null, [...value, '']);
        }
    };

    const handleDeleteField = (key: string) => {
        if (typeof value === 'object' && value !== null && !Array.isArray(value)) {
            const newObj = {...value};
            delete newObj[key];
            onChange(name, null, newObj);
        }
    };

    const handleDeleteArrayItem = (index: number) => {
        if (Array.isArray(value)) {
            const newArr = [...value];
            newArr.splice(index, 1);
            onChange(name, null, newArr);
        }
    };

    const renderValueInput = () => {
        switch (valueType) {
            case 'string':
                return (
                    <Input
                        value={value as string}
                        onChange={(e) => handleValueChange(e.target.value)}
                        placeholder="字符串值"
                        className="flex-1"
                    />
                );
            case 'number':
                return (
                    <Input
                        type="number"
                        value={value as number}
                        onChange={(e) => handleValueChange(Number(e.target.value))}
                        placeholder="数字值"
                        className="flex-1"
                    />
                );
            case 'boolean':
                return (
                    <Select
                        value={value as boolean}
                        onChange={(v) => handleValueChange(v)}
                        options={[
                            {label: 'true', value: true},
                            {label: 'false', value: false},
                        ]}
                        className="w-28"
                    />
                );
            case 'null':
                return <Tag color="default">null</Tag>;
            default:
                return null;
        }
    };

    const isComplex = valueType === 'object' || valueType === 'array';

    return (
        <div style={{marginLeft: indent}} className="mb-2">
            <div className="flex items-center gap-2 flex-wrap">
                {isArrayItem ? (
                    <div className="w-full">
                        <div className="flex flex-row items-center space-x-2 mb-2">
                            {isComplex && (
                                <Button
                                    type="text"
                                    size="small"
                                    onClick={() => setIsExpanded(!isExpanded)}
                                    className="px-1"
                                >
                                    {isExpanded ? '▼' : '▶'}
                                </Button>
                            )}
                            <Select<ValueType>
                                className="w-24 max-w-24"
                                value={valueType}
                                onChange={handleTypeChange}
                                options={[
                                    {label: 'string', value: 'string'},
                                    {label: 'number', value: 'number'},
                                    {label: 'boolean', value: 'boolean'},
                                    {label: 'null', value: 'null'},
                                    {label: 'object', value: 'object'},
                                    {label: 'array', value: 'array'},
                                ]}
                            />
                            <Tag className="font-mono text-xs">{name}</Tag>
                            <Tooltip title="删除">
                                <Button
                                    type="text"
                                    danger
                                    size="small"
                                    icon={<DeleteOutlined/>}
                                    onClick={onDelete}
                                />
                            </Tooltip>
                        </div>
                        {!isComplex && (
                            <div className="pl-8">
                                {renderValueInput()}
                            </div>
                        )}
                    </div>
                ) : (
                    <div className="w-full flex flex-row items-center space-x-2">
                        {isComplex && (
                            <Button
                                type="text"
                                size="small"
                                onClick={() => setIsExpanded(!isExpanded)}
                                className="px-1"
                            >
                                {isExpanded ? '▼' : '▶'}
                            </Button>
                        )}

                        <Select<ValueType>
                            className="w-24 max-w-24"
                            value={valueType}
                            onChange={handleTypeChange}
                            options={[
                                {label: 'string', value: 'string'},
                                {label: 'number', value: 'number'},
                                {label: 'boolean', value: 'boolean'},
                                {label: 'null', value: 'null'},
                                {label: 'object', value: 'object'},
                                {label: 'array', value: 'array'},
                            ]}
                        />

                        <Input
                            value={editingName}
                            onChange={(e) => setEditingName(e.target.value)}
                            onBlur={handleNameChange}
                            onPressEnter={handleNameChange}
                            placeholder="字段名"
                            className="w-full h-8 font-mono text-xs"
                        />

                        <Tooltip title="删除">
                            <Button
                                type="text"
                                danger
                                size="small"
                                icon={<DeleteOutlined/>}
                                onClick={onDelete}
                            />
                        </Tooltip>
                    </div>
                )}

                {!isArrayItem && !isComplex && renderValueInput()}

                {isComplex && isExpanded && (
                    <Button
                        type="dashed"
                        size="small"
                        icon={<PlusOutlined/>}
                        onClick={valueType === 'array' ? handleAddArrayItem : handleAddField}
                    >
                        添加{valueType === 'array' ? '项' : '字段'}
                    </Button>
                )}
            </div>

            {isComplex && isExpanded && (
                <div className="mt-2 pl-4 border-l-2 border-gray-200">
                    {valueType === 'object' && typeof value === 'object' && value !== null && (
                        Object.entries(value).map(([key, val]) => (
                            <JsonNode
                                key={key}
                                name={key}
                                value={val}
                                onChange={handleObjectChange}
                                onDelete={() => handleDeleteField(key)}
                                depth={depth + 1}
                            />
                        ))
                    )}
                    {valueType === 'array' && Array.isArray(value) && (
                        value.map((val, index) => (
                            <JsonNode
                                key={index}
                                name={`[${index}]`}
                                value={val}
                                onChange={(_, __, newVal) => handleArrayChange(index, newVal)}
                                onDelete={() => handleDeleteArrayItem(index)}
                                depth={depth + 1}
                                isArrayItem={true}
                            />
                        ))
                    )}
                </div>
            )}
        </div>
    );
}

export function JsonEditor({value = '{}', onChange, placeholder}: JsonEditorProps) {
    const [mode, setMode] = useState<'visual' | 'json'>('visual');
    const [rootType, setRootType] = useState<'object' | 'array'>('object');
    const [jsonObject, setJsonObject] = useState<JsonObject>({});
    const [jsonArray, setJsonArray] = useState<JsonArray>([]);
    const [jsonText, setJsonText] = useState(value);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const parseJson = () => {
            try {
                const parsed = JSON.parse(value || '{}');
                if (Array.isArray(parsed)) {
                    setRootType('array');
                    setJsonArray(parsed);
                    setJsonObject({});
                } else if (typeof parsed === 'object' && parsed !== null) {
                    setRootType('object');
                    setJsonObject(parsed);
                    setJsonArray([]);
                } else {
                    // 原始类型，包装成对象
                    setRootType('object');
                    setJsonObject({value: parsed});
                    setJsonArray([]);
                }
                setJsonText(JSON.stringify(parsed, null, 2));
                setError(null);
            } catch {
                setError('无效的JSON格式');
                setJsonText(value);
            }
        };
        parseJson();
    }, [value]);

    const handleRootTypeChange = (newType: 'object' | 'array') => {
        setRootType(newType);
        if (newType === 'object') {
            const newData: JsonObject = {};
            setJsonObject(newData);
            setJsonArray([]);
            const jsonString = JSON.stringify(newData);
            setJsonText(JSON.stringify(newData, null, 2));
            onChange?.(jsonString);
        } else {
            const newData: JsonArray = [];
            setJsonArray(newData);
            setJsonObject({});
            const jsonString = JSON.stringify(newData);
            setJsonText(JSON.stringify(newData, null, 2));
            onChange?.(jsonString);
        }
    };

    const handleObjectChange = (oldKey: string, newKey: string | null, newValue: JsonValue) => {
        if (newKey === null) {
            const newData = {...jsonObject, [oldKey]: newValue};
            setJsonObject(newData);
            const jsonString = JSON.stringify(newData);
            setJsonText(JSON.stringify(newData, null, 2));
            onChange?.(jsonString);
        } else if (newKey !== oldKey) {
            const newData: JsonObject = {};
            for (const [k, v] of Object.entries(jsonObject)) {
                if (k === oldKey) {
                    newData[newKey] = newValue;
                } else {
                    newData[k] = v;
                }
            }
            setJsonObject(newData);
            const jsonString = JSON.stringify(newData);
            setJsonText(JSON.stringify(newData, null, 2));
            onChange?.(jsonString);
        }
    };

    const handleArrayChange = (index: number, newValue: JsonValue) => {
        const newArr = [...jsonArray];
        newArr[index] = newValue;
        setJsonArray(newArr);
        const jsonString = JSON.stringify(newArr);
        setJsonText(JSON.stringify(newArr, null, 2));
        onChange?.(jsonString);
    };

    const handleAddRootField = () => {
        const newKey = `field${Object.keys(jsonObject).length + 1}`;
        const newData = {...jsonObject, [newKey]: ''};
        setJsonObject(newData);
        const jsonString = JSON.stringify(newData);
        setJsonText(JSON.stringify(newData, null, 2));
        onChange?.(jsonString);
    };

    const handleAddRootArrayItem = () => {
        const newData = [...jsonArray, ''];
        setJsonArray(newData);
        const jsonString = JSON.stringify(newData);
        setJsonText(JSON.stringify(newData, null, 2));
        onChange?.(jsonString);
    };

    const handleDeleteRootField = (key: string) => {
        const newData = {...jsonObject};
        delete newData[key];
        setJsonObject(newData);
        const jsonString = JSON.stringify(newData);
        setJsonText(JSON.stringify(newData, null, 2));
        onChange?.(jsonString);
    };

    const handleDeleteRootArrayItem = (index: number) => {
        const newData = [...jsonArray];
        newData.splice(index, 1);
        setJsonArray(newData);
        const jsonString = JSON.stringify(newData);
        setJsonText(JSON.stringify(newData, null, 2));
        onChange?.(jsonString);
    };

    const handleJsonTextChange = (text: string) => {
        setJsonText(text);
        try {
            const parsed = JSON.parse(text);
            if (Array.isArray(parsed)) {
                setRootType('array');
                setJsonArray(parsed);
                setJsonObject({});
            } else if (typeof parsed === 'object' && parsed !== null) {
                setRootType('object');
                setJsonObject(parsed);
                setJsonArray([]);
            }
            setError(null);
            onChange?.(JSON.stringify(parsed));
        } catch {
            setError('JSON格式错误');
        }
    };

    const renderRootContent = () => {
        if (rootType === 'object') {
            return (
                <>
                    {Object.keys(jsonObject).length === 0 ? (
                        <div className="text-center py-8 text-gray-400">
                            <Text type="secondary">暂无数据</Text>
                        </div>
                    ) : (
                        Object.entries(jsonObject).map(([key, val]) => (
                            <div key={key}>
                                <JsonNode
                                    name={key}
                                    value={val}
                                    onChange={handleObjectChange}
                                    onDelete={() => handleDeleteRootField(key)}
                                    depth={0}
                                />
                                <Divider orientation="horizontal" />
                            </div>
                        ))
                    )}
                    <Button
                        type="dashed"
                        block
                        icon={<PlusOutlined/>}
                        onClick={handleAddRootField}
                        className="mt-2"
                    >
                        添加字段
                    </Button>
                </>
            );
        } else {
            return (
                <>
                    {jsonArray.length === 0 ? (
                        <div className="text-center py-8 text-gray-400">
                            <Text type="secondary">暂无数据</Text>
                        </div>
                    ) : (
                        jsonArray.map((val, index) => (
                            <div key={index}>
                                <JsonNode
                                    name={`[${index}]`}
                                    value={val}
                                    onChange={(_, __, newVal) => handleArrayChange(index, newVal)}
                                    onDelete={() => handleDeleteRootArrayItem(index)}
                                    depth={0}
                                    isArrayItem={true}
                                />
                                <Divider orientation="horizontal" />
                            </div>
                        ))
                    )}
                    <Button
                        type="dashed"
                        block
                        icon={<PlusOutlined/>}
                        onClick={handleAddRootArrayItem}
                        className="mt-2"
                    >
                        添加项
                    </Button>
                </>
            );
        }
    };

    return (
        <Card
            size="small"
            className="border rounded-lg"
            title={
                <div className="flex justify-between items-center">
                    <Space>
                        <Button
                            type={mode === 'visual' ? 'primary' : 'default'}
                            size="small"
                            icon={<EyeOutlined/>}
                            onClick={() => setMode('visual')}
                        >
                            可视化
                        </Button>
                        <Button
                            type={mode === 'json' ? 'primary' : 'default'}
                            size="small"
                            icon={<EditOutlined/>}
                            onClick={() => setMode('json')}
                        >
                            JSON
                        </Button>
                        {mode === 'visual' && (
                            <Select
                                size="small"
                                value={rootType}
                                onChange={handleRootTypeChange}
                                options={[
                                    {label: '对象 {}', value: 'object'},
                                    {label: '数组 []', value: 'array'},
                                ]}
                                className="w-28"
                            />
                        )}
                    </Space>
                    {error && <Text type="danger" className="text-xs">{error}</Text>}
                </div>
            }
        >
            {mode === 'visual' ? (
                <div className="max-h-96 overflow-y-auto">
                    {renderRootContent()}
                </div>
            ) : (
                <TextArea
                    value={jsonText}
                    onChange={(e) => handleJsonTextChange(e.target.value)}
                    rows={8}
                    placeholder={placeholder || "输入JSON格式数据..."}
                    className="font-mono text-xs"
                    status={error ? 'error' : undefined}
                />
            )}
        </Card>
    );
}
