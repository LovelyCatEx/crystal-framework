import {useMemo, useState} from "react";
import type {Key} from "react";
import {Button, Popconfirm, Space, Spin, Switch, Table, Tag} from "antd";
import type {ColumnsType, TableRowSelection} from "antd/es/table/interface";
import {DeleteOutlined, EditOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import type {BaseEntity} from "@/types/BaseEntity.ts";
import {CopyableToolTip} from "@/components/CopyableToolTip.tsx";
import {usePermissionTranslator} from "@/i18n/permission-translations.tsx";

export interface PermissionTreeItem extends BaseEntity {
    name: string;
    type: number;
    description?: string | null;
    path?: string | null;
}

export type PermissionScope = "system" | "x" | "tenant" | "i.tenant";

const SCOPE_COLORS: Record<PermissionScope, string> = {
    system: "blue",
    x: "purple",
    tenant: "orange",
    "i.tenant": "green",
};

const SCOPE_ORDER: Record<PermissionScope, number> = {
    x: 0,
    system: 1,
    tenant: 2,
    "i.tenant": 3,
};

const TYPE_COLORS: Record<number, string> = {0: "green", 1: "gold", 2: "geekblue"};

// i18n keys cannot contain dots (i18next treats them as nested-key separators),
// so `i.tenant` maps to the safe key `iTenant`.
const SCOPE_I18N_KEY: Record<PermissionScope, string> = {
    system: "system",
    x: "x",
    tenant: "tenant",
    "i.tenant": "iTenant",
};

const UNKNOWN_MODULE_KEY = "_other";

/**
 * Splits a permission name into `{scope, module}` following the backend PermissionMatrix
 * convention: `i.tenant.*` is a compound scope, other prefixes (`x`, `system`, `tenant`)
 * are single-segment scopes. The module is always the first segment after the scope.
 */
function parsePermission(name: string): { scope: PermissionScope; module: string } {
    // Strip legacy `:path` suffix (e.g. "system.monitor:/manager/…" → "system.monitor")
    const cleanName = name.split(":")[0];
    if (cleanName.startsWith("i.tenant.")) {
        const module = cleanName.slice("i.tenant.".length).split(".")[0] || UNKNOWN_MODULE_KEY;
        return {scope: "i.tenant", module};
    }
    const parts = cleanName.split(".");
    const first = parts[0];
    if (first === "system" || first === "x" || first === "tenant") {
        return {scope: first, module: parts[1] || UNKNOWN_MODULE_KEY};
    }
    return {scope: "system", module: first || UNKNOWN_MODULE_KEY};
}

type ModuleRow<T extends PermissionTreeItem> = {
    kind: "module";
    rowKey: string;
    moduleKey: string;
    totalCount: number;
    scopes: PermissionScope[];
    children: ScopeRow<T>[];
};

type ScopeRow<T extends PermissionTreeItem> = {
    kind: "scope";
    rowKey: string;
    scope: PermissionScope;
    moduleKey: string;
    childrenCount: number;
    children: LeafRow<T>[];
};

type LeafRow<T extends PermissionTreeItem> = {
    kind: "leaf";
    rowKey: string;
    scope: PermissionScope;
    data: T;
};

type TableRow<T extends PermissionTreeItem> = ModuleRow<T> | ScopeRow<T> | LeafRow<T>;

export type PermissionTreeTableMode = "readonly" | "checkbox" | "radio";

const LEAF_KEY_PREFIX = "perm_";

const toLeafKey = (id: string): string => `${LEAF_KEY_PREFIX}${id}`;
const isLeafKey = (key: Key): boolean => String(key).startsWith(LEAF_KEY_PREFIX);
const fromLeafKey = (key: Key): string => String(key).slice(LEAF_KEY_PREFIX.length);

export interface PermissionTreeTableProps<T extends PermissionTreeItem> {
    permissions: T[];
    loading?: boolean;
    typeLabel: (type: number) => string;
    onEdit?: (row: T) => void;
    onDelete?: (row: T) => void;
    entityName?: string;
    /**
     * Selection mode. `readonly` (default) renders a plain catalog table;
     * `checkbox` enables multi-select with parent-row cascading;
     * `radio` enables single-select restricted to leaf permission rows.
     */
    mode?: PermissionTreeTableMode;
    /** Selected permission ids. For `radio` mode, pass an array of 0 or 1 element. */
    selectedIds?: string[];
    /** Fires with the selected permission ids (leaf ids only, never group keys). */
    onChange?: (selectedIds: string[]) => void;
    /** Permission ids whose selection checkbox/radio should be disabled. */
    disabledIds?: string[];
}

export function PermissionTreeTable<T extends PermissionTreeItem>(props: PermissionTreeTableProps<T>) {
    const {t, i18n} = useTranslation();
    const translatePermission = usePermissionTranslator();
    const [useI18nDescription, setUseI18nDescription] = useState(true);
    const {
        permissions, loading, typeLabel, onEdit, onDelete, entityName,
        mode = "readonly", selectedIds, onChange, disabledIds,
    } = props;

    const resolveDescription = (row: T): string => {
        if (useI18nDescription) {
            return translatePermission(row.name) ?? (row.description || "-");
        }
        return row.description || "-";
    };

    const groupedData = useMemo<ModuleRow<T>[]>(() => {
        const moduleBuckets = new Map<string, Map<PermissionScope, LeafRow<T>[]>>();
        for (const permission of permissions) {
            const {scope, module} = parsePermission(permission.name);
            const leaf: LeafRow<T> = {
                kind: "leaf",
                rowKey: `perm_${permission.id}`,
                scope,
                data: permission,
            };
            let scopeMap = moduleBuckets.get(module);
            if (!scopeMap) {
                scopeMap = new Map();
                moduleBuckets.set(module, scopeMap);
            }
            const existing = scopeMap.get(scope);
            if (existing) {
                existing.push(leaf);
            } else {
                scopeMap.set(scope, [leaf]);
            }
        }
        return Array.from(moduleBuckets.entries())
            .sort(([a], [b]) => a.localeCompare(b))
            .map(([moduleKey, scopeMap]) => {
                const scopeRows: ScopeRow<T>[] = Array.from(scopeMap.entries())
                    .sort(([a], [b]) => SCOPE_ORDER[a] - SCOPE_ORDER[b])
                    .map(([scope, leaves]) => {
                        leaves.sort((a, b) => a.data.name.localeCompare(b.data.name));
                        return {
                            kind: "scope",
                            rowKey: `group_${moduleKey}_${scope}`,
                            scope,
                            moduleKey,
                            childrenCount: leaves.length,
                            children: leaves,
                        };
                    });
                return {
                    kind: "module",
                    rowKey: `group_${moduleKey}`,
                    moduleKey,
                    totalCount: scopeRows.reduce((s, r) => s + r.childrenCount, 0),
                    scopes: scopeRows.map((r) => r.scope),
                    children: scopeRows,
                };
            });
    }, [permissions]);

    const defaultExpandedRowKeys = useMemo(
        () => groupedData.map((m) => m.rowKey),
        [groupedData],
    );

    const showActions = Boolean(onEdit || onDelete);

    const columns = useMemo<ColumnsType<TableRow<T>>>(() => {
        const resolveModuleLabel = (moduleKey: string): string | null => {
            const key = `components.permissionTree.modules.${moduleKey}`;
            return i18n.exists(key) ? t(key) : null;
        };
        const resolveScopeLabel = (scope: PermissionScope): string =>
            t(`components.permissionTree.scopes.${SCOPE_I18N_KEY[scope]}`);

        const cols: ColumnsType<TableRow<T>> = [
            {
                title: t("components.columns.userPermission.permission"),
                key: "name",
                render: (_, row) => {
                    if (row.kind === "module") {
                        const label = resolveModuleLabel(row.moduleKey);
                        return (
                            <Space size={8} wrap>
                                <span className="font-bold text-base">{label ?? row.moduleKey}</span>
                                {label && (
                                    <span className="text-gray-400 text-xs font-mono">{row.moduleKey}</span>
                                )}
                                <Tag color="processing">{row.totalCount}</Tag>
                                {row.scopes.map((s) => (
                                    <Tag key={s} color={SCOPE_COLORS[s]} className="opacity-70">
                                        {resolveScopeLabel(s)}
                                    </Tag>
                                ))}
                            </Space>
                        );
                    }
                    if (row.kind === "scope") {
                        return (
                            <Space size={8}>
                                <Tag color={SCOPE_COLORS[row.scope]}>
                                    {resolveScopeLabel(row.scope)}
                                </Tag>
                                <Tag>{row.childrenCount}</Tag>
                            </Space>
                        );
                    }
                    return (
                        <div className="flex items-center gap-2">
                            <Tag color={TYPE_COLORS[row.data.type] ?? "default"} className="shrink-0">
                                {typeLabel(row.data.type)}
                            </Tag>
                            <div className="flex flex-col min-w-0">
                                <span className="text-sm">{resolveDescription(row.data)}</span>
                                <CopyableToolTip title={row.data.name}>
                                    <span className="font-mono text-xs text-gray-400">{row.data.name}</span>
                                </CopyableToolTip>
                            </div>
                        </div>
                    );
                },
            },
            {
                title: t("components.columns.userPermission.path"),
                key: "path",
                width: 320,
                render: (_, row) => {
                    if (row.kind !== "leaf") return null;
                    return row.data.path || "-";
                },
            },
        ];

        if (showActions) {
            cols.push({
                title: t("components.columns.permissionTree.actions"),
                key: "actions",
                width: 100,
                fixed: "right",
                render: (_, row) => {
                    if (row.kind !== "leaf") return null;
                    return (
                        <Space>
                            {onEdit && (
                                <Button
                                    type="text"
                                    size="small"
                                    icon={<EditOutlined/>}
                                    onClick={() => onEdit(row.data)}
                                />
                            )}
                            {onDelete && (
                                <Popconfirm
                                    title={t("components.managerPageContainer.deleteConfirm", {
                                        entityName: entityName ?? "",
                                    })}
                                    onConfirm={() => onDelete(row.data)}
                                    okText={t("components.managerPageContainer.confirm")}
                                    cancelText={t("components.managerPageContainer.cancel")}
                                >
                                    <Button
                                        type="text"
                                        size="small"
                                        icon={<DeleteOutlined/>}
                                        danger
                                    />
                                </Popconfirm>
                            )}
                        </Space>
                    );
                },
            });
        }
        return cols;
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [t, i18n, typeLabel, useI18nDescription, translatePermission, onEdit, onDelete, showActions, entityName]);

    const rowSelection = useMemo<TableRowSelection<TableRow<T>> | undefined>(() => {
        if (mode === "readonly") return undefined;

        const disabledLeafKeys = new Set((disabledIds ?? []).map(toLeafKey));
        const selectedLeafKeys = (selectedIds ?? []).map(toLeafKey);

        if (mode === "radio") {
            return {
                type: "radio",
                selectedRowKeys: selectedLeafKeys,
                // Non-leaf rows (module/scope groups) are not selectable in radio mode.
                renderCell: (_checked, row, _index, originNode) =>
                    row.kind === "leaf" ? originNode : null,
                getCheckboxProps: (row) => ({
                    disabled: row.kind !== "leaf" || disabledLeafKeys.has(row.rowKey),
                }),
                onChange: (_keys, rows) => {
                    const ids = rows.filter((r) => r.kind === "leaf").map((r) => r.data.id);
                    onChange?.(ids);
                },
            };
        }

        // checkbox: parent rows cascade selection onto their leaf children.
        return {
            type: "checkbox",
            checkStrictly: false,
            selectedRowKeys: selectedLeafKeys,
            getCheckboxProps: (row) => ({
                disabled: row.kind === "leaf" && disabledLeafKeys.has(row.rowKey),
            }),
            onChange: (keys) => {
                onChange?.(keys.filter(isLeafKey).map(fromLeafKey));
            },
        };
    }, [mode, selectedIds, disabledIds, onChange]);

    return (
        <Spin spinning={!!loading}>
            <div className="mb-3">
                <Space size={8}>
                    <span>{t("pages.permissionCatalog.source.label")}</span>
                    <Switch
                        checked={useI18nDescription}
                        checkedChildren={t("pages.permissionCatalog.source.i18n")}
                        unCheckedChildren={t("pages.permissionCatalog.source.db")}
                        onChange={setUseI18nDescription}
                    />
                </Space>
            </div>
            <Table<TableRow<T>>
                key={defaultExpandedRowKeys.join(",")}
                rowKey="rowKey"
                dataSource={groupedData}
                columns={columns}
                rowSelection={rowSelection}
                expandable={{
                    defaultExpandedRowKeys,
                    rowExpandable: (row) => row.kind !== "leaf",
                }}
                pagination={false}
                scroll={{x: "max-content"}}
            />
        </Spin>
    );
}
