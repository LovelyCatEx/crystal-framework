# 实体数据表格

`EntityTable` —— 通用数据表格组件，内置分页、搜索、筛选、时间范围选择、列显隐控制和行选择功能。

## 基础参数

### entityName

实体名称，用于错误提示等文案中显示。

```tsx
<EntityTable entityName="用户" ... />
```

### columns

表格列定义，类型为 `EntityTableColumns<ENTITY>`。通常封装在 `useXxxColumns()` hook 中，以便复用和访问 i18n。

**类型定义**：

```ts
interface EntityTableColumn<ENTITY, COLUMN_DATA_TYPE> {
    title: string;            // 列标题
    dataIndex: string;        // 数据字段名
    key: string;              // 唯一键，用于列显隐控制和自动追加列的 Key 去重
    fixed?: 'start' | 'end' | 'left' | 'right' | boolean;
    width?: number;
    render: (columnData: COLUMN_DATA_TYPE, row: ENTITY) => ReactNode;
}
```

**编写示例** — 以用户表格为例：

```tsx
// components/columns/UserEntityColumns.tsx
import type { EntityTableColumns } from '@/components/table/entity-table.types.ts';
import type { User } from '@/types/user/user.types.ts';

export function useUserTableColumns(): EntityTableColumns<User> {
    const { t } = useTranslation();

    return [
        {
            title: t('components.columns.user.userInfo'),
            dataIndex: 'id',
            key: 'id',
            render: (_, row) => (
                <Space>
                    <AvatarResource fileEntityId={row.avatar} />
                    <span>@{row.username}</span>
                </Space>
            ),
        },
        {
            title: t('components.columns.user.nickname'),
            dataIndex: 'nickname',
            key: 'nickname',
            render: (_, row) => <span>{row.nickname}</span>,
        },
        {
            title: t('components.columns.user.email'),
            dataIndex: 'email',
            key: 'email',
            render: (_, row) => <span>{row.email}</span>,
        },
    ];
}
```

页面中使用：

```tsx
const columns = useUserTableColumns();

<EntityTable columns={columns} ... />
```

::: tip 注意
- `render` 收到两个参数：当前列的 `dataIndex` 对应值（第一个参数）和整行数据 `row`（第二个参数）。一般用 `row` 就够了，第一个参数用 `_` 占位。
- `key` 必须唯一且稳定，表格自动追加的"记录时间"列和"操作"列会据此去重。
- 除业务列外，表格会自动追加"记录时间"列（createdTime + modifiedTime），可通过 `hideRecordTimeColumn` 隐藏。若传了 `tableRowActionsRender`，还会自动追加"操作"列。
:::

### query

分页查询函数，每次翻页、搜索、筛选条件变化时调用。

```ts
query: <T extends BaseManagerReadDTO>(props: T) => Promise<PaginatedResponseData<ENTITY>>
```

组件自动传入 `page`、`pageSize` 和所有过滤参数。返回 `{ total, records }`。

## 过滤栏操作项

### tableActions

渲染在搜索框之后的过滤栏操作项。每项包含标签和控件。

```tsx
<EntityTable
    tableActions={[
        {
            label: <span>状态</span>,
            children: (
                <Select ... onChange={(v) => setFilter('status', v)} />
            ),
        },
    ]}
/>
```

```ts
{
    label: ReactNode;     // 控件上方的标签
    children: ReactNode;  // 实际控件（Input、Select 等）
}
```

非只读模式下，`ManagerPageContainer` 会自动将批量操作选择器插入到 `tableActions` 最前面。此外，内置的时间范围选择器、FilterBuilder 和刷新按钮也会追加到 `tableActions` 中。

### tablePrefixActions

渲染在搜索框之前的过滤栏操作项，结构与 `tableActions` 完全相同。

## 全局搜索

### searchKeywords

传入后会显示全局搜索框。用户输入关键词按回车查询，`searchKeywords` 中的字段以 **OR** 方式组合——任一字段包含关键词即匹配。

```tsx
<EntityTable searchKeywords={['username', 'email', 'nickname']} />
```

效果：输入 `alice` 回车 → 查询 username、email、nickname 任一包含 `alice` 的记录。

## 行内过滤

### simpleFilters

开发者提供的逐字段过滤条件，所有条件以 **AND** 方式组合。配合 `useManagerQueryParams` 的 `filters` 使用。

```tsx
<EntityTable
    simpleFilters={[
        { field: 'status',  value: filters.status },
        { field: 'user_id', value: filters.userId ? Number(filters.userId) : undefined },
    ]}
/>
```

```ts
{
    field: string;        // 查询字段名
    operator?: string;    // 比较运算符，默认 "contains"
    value: unknown;       // 过滤值，undefined/null/'' 时自动跳过
    urlKey?: string;      // URL 参数名，默认与 field 相同
}
```

::: tip 注意
- `operator` 默认 `"contains"`（子串匹配），数值或枚举类字段建议显式传 `"eq"`。
- `urlKey` 用于 URL 同步：不传时 URL 参数名等于 `field`；传入后 URL 中用 `urlKey`，请求仍用 `field`。
:::

## 高级筛选

### filterableFields

传入后会在过滤栏显示 FilterBuilder 高级条件筛选器。用户可以自由组合多字段的 AND/OR 条件，支持多种运算符。

```tsx
<EntityTable
    filterableFields={[
        { field: 'username', type: 'text',   label: '用户名' },
        { field: 'email',    type: 'text',   label: '邮箱' },
        { field: 'nickname', type: 'text',   label: '昵称' },
    ]}
/>
```

详细用法见 [高级条件搜索](/develop/frontend/manager/standard-manager#高级条件搜索)。

## 时间范围

### showTimeRangeFilter

是否显示内置的时间范围选择器，默认为 `true`。选择的时间范围以 `createdTime` 字段 GTE/LTE 条件 AND 到查询中。

```tsx
<EntityTable showTimeRangeFilter={false} />
```

## 列控制

### hideRecordTimeColumn

是否隐藏表格末尾自动追加的"记录时间"列（显示 createdTime 和 modifiedTime），默认为 `false`。

```tsx
<EntityTable hideRecordTimeColumn />
```

## 表格选择

### tableSelection

控制行选择行为。不传时由 `ManagerPageContainer` 根据 `readonlyMode` 自动决定：非只读模式默认 `checkbox`，只读模式自动禁用。

```tsx
<EntityTable
    tableSelection={{
        type: 'checkbox',
        onChange: (records) => setSelected(records),
        isDisabled: (record) => record.status === 'locked',
    }}
/>
```

```ts
{
    type: 'checkbox' | 'radio' | 'disabled';
    onChange?: (records: ENTITY[]) => void;
    isDisabled?: (record: ENTITY) => boolean;
}
```

## 行操作

### tableRowActionsRender

在每行内置的编辑/删除按钮之前插入自定义操作按钮。

```tsx
<EntityTable
    tableRowActionsRender={(record) => (
        <Button icon={<DownloadOutlined />} onClick={() => download(record)} />
    )}
/>
```

## URL 同步

### queryParamsSync

每次查询**之后**自动调用，将当前的分页、搜索关键词和时间范围写入浏览器地址栏。传入 `useManagerQueryParams()` 返回的 `syncToUrl` 即可。

**时机**：用户翻页、输入搜索回车、切换时间范围、改变 FilterBuilder 条件后，EntityTable 发起查询，拿到结果后调用 `queryParamsSync(urlParams)` 更新 URL。

**效果**：URL 自动变为 `?page=1&searchKeyword=alice&startTime=1700000000000`，刷新页面或分享链接，条件不丢失。

### initialQueryValues

页面**首次加载**时，从 URL 中提取初始值，用于恢复上次的浏览状态。传入 `useManagerQueryParams()` 返回的 `initialQueryValues` 即可。

**原理**：`useManagerQueryParams` 在初始化时解析 `window.location.search`，提取所有非 schema 保留字段（`page`、`pageSize`、`searchKeyword`、`startTime`、`endTime`、`query`），组装成 `initialQueryValues` 对象。

**效果**：用户从 `?page=3&searchKeyword=alice` 的链接进入页面，表格直接定位到第 3 页、搜索框回填 `alice`。

```tsx
const { syncToUrl, initialQueryValues } = useManagerQueryParams({ schema: { status: 'string' } });

<EntityTable
    queryParamsSync={syncToUrl}
    initialQueryValues={initialQueryValues}
/>
```

### initialQueryValues 字段

| 字段 | 类型 | 说明 |
|------|------|------|
| `page` | `number` | 当前页码 |
| `pageSize` | `number` | 每页条数 |
| `searchKeyword` | `string` | 搜索关键词 |
| `startTime` | `number \| string` | 时间范围起始时间戳 |
| `endTime` | `number \| string` | 时间范围结束时间戳 |
| `query` | `unknown` | FilterBuilder 序列化的查询节点 |

## Ref 方法

通过 `ref` 可以在外部控制表格：

```tsx
const tableRef = useRef<EntityTableRef>(null);

tableRef.current?.refreshData();
tableRef.current?.refreshData({ resetPage: true });
tableRef.current?.clearSelection();
```

### refreshData(options?)

重新触发查询。

| 参数 | 类型 | 说明 |
|------|------|------|
| `options.resetPage` | `boolean` | 是否重置到第 1 页，默认 `false` |

### clearSelection()

清除所有已选中的行。

---

## 参数总表

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `entityName` | `string` | — | 实体名称 |
| `columns` | `EntityTableColumns<ENTITY>` | — | 表格列定义 |
| `query` | `(props: T) => Promise<PaginatedResponseData<ENTITY>>` | — | 分页查询函数 |
| `tableActions` | `TableAction[]` | — | 搜索框后的过滤栏操作项 |
| `tablePrefixActions` | `TableAction[]` | — | 搜索框前的过滤栏操作项 |
| `searchKeywords` | `string[]` | — | 全局搜索字段，不传不显示搜索框 |
| `simpleFilters` | `SimpleFilter[]` | — | 行内过滤条件 |
| `filterableFields` | `FilterableField[]` | — | FilterBuilder 可用字段 |
| `showTimeRangeFilter` | `boolean` | `true` | 是否显示时间范围选择器 |
| `hideRecordTimeColumn` | `boolean` | `false` | 是否隐藏记录时间列 |
| `tableSelection` | `TableSelectionConfig` | — | 行选择配置 |
| `tableRowActionsRender` | `(record: ENTITY) => ReactNode` | — | 行自定义操作按钮 |
| `queryParamsSync` | `(params: object) => void` | — | URL 同步回调 |
| `initialQueryValues` | `object` | — | URL 初始值 |
| `children` | `ReactNode` | — | 表格下方的内容 |
