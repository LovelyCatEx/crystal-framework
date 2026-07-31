import React, {useMemo} from "react";
import {Button, Popconfirm, Space, Spin, Table, Tag} from "antd";
import type {ColumnsType} from "antd/es/table";
import {DeleteOutlined, EditOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import type {BaseEntity} from "@/types/BaseEntity.ts";
import {CopyableToolTip} from "@/components/CopyableToolTip.tsx";

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

export interface PermissionTreeTableProps<T extends PermissionTreeItem> {
    permissions: T[];
    loading?: boolean;
    typeLabel: (type: number) => string;
    descriptionRender?: (row: T) => React.ReactNode;
    onEdit?: (row: T) => void;
    onDelete?: (row: T) => void;
    entityName?: string;
}

export function PermissionTreeTable<T extends PermissionTreeItem>(props: PermissionTreeTableProps<T>) {
    const {t, i18n} = useTranslation();
    const {permissions, loading, typeLabel, descriptionRender, onEdit, onDelete, entityName} = props;

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
                width: 460,
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
                        <CopyableToolTip title={row.data.name}>
                            <span className="font-mono text-sm">{row.data.name}</span>
                        </CopyableToolTip>
                    );
                },
            },
            {
                title: t("components.columns.userPermission.type"),
                key: "type",
                width: 120,
                render: (_, row) => {
                    if (row.kind !== "leaf") return null;
                    const TYPE_COLORS: Record<number, string> = {0: "green", 1: "gold", 2: "geekblue"};
                    return <Tag color={TYPE_COLORS[row.data.type] ?? "default"}>{typeLabel(row.data.type)}</Tag>;
                },
            },
            {
                title: t("components.columns.userPermission.description"),
                key: "description",
                render: (_, row) => {
                    if (row.kind !== "leaf") return null;
                    if (descriptionRender) return descriptionRender(row.data);
                    return row.data.description || "-";
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
    }, [t, i18n, typeLabel, descriptionRender, onEdit, onDelete, showActions, entityName]);

    return (
        <Spin spinning={!!loading}>
            <Table<TableRow<T>>
                key={defaultExpandedRowKeys.join(",")}
                rowKey="rowKey"
                dataSource={groupedData}
                columns={columns}
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
