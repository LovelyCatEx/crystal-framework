# Data Table

`EntityTable` — a generic data table component with built-in pagination, search, filtering, time range selection, column visibility control, and row selection.

## Basic Props

### entityName

The entity name, used in error messages and other display text.

```tsx
<EntityTable entityName="User" ... />
```

### columns

Table column definitions, typed as `EntityTableColumns<ENTITY>`. Typically wrapped in a `useXxxColumns()` hook for reuse and i18n access.

**Type definition**:

```ts
interface EntityTableColumn<ENTITY, COLUMN_DATA_TYPE> {
    title: string;            // Column header
    dataIndex: string;        // Data field name
    key: string;              // Unique key for column visibility control and dedup
    fixed?: 'start' | 'end' | 'left' | 'right' | boolean;
    width?: number;
    render: (columnData: COLUMN_DATA_TYPE, row: ENTITY) => ReactNode;
}
```

**Example** — a user table:

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

Usage in a page:

```tsx
const columns = useUserTableColumns();

<EntityTable columns={columns} ... />
```

::: tip Note
- `render` receives two arguments: the value at `dataIndex` (first param) and the entire row (second param). Most of the time you only need `row` — use `_` for the first param.
- `key` must be unique and stable. The auto-appended "Record Time" and "Action" columns use it for dedup.
- A "Record Time" column (createdTime + modifiedTime) is automatically appended. Hide it with `hideRecordTimeColumn`. If `tableRowActionsRender` is provided, an "Action" column is also appended.
:::

### query

The paginated query function called on page change, search, or filter change.

```ts
query: <T extends BaseManagerReadDTO>(props: T) => Promise<PaginatedResponseData<ENTITY>>
```

The component automatically passes `page`, `pageSize`, and all filter params. Returns `{ total, records }`.

## Filter Bar Actions

### tableActions

Filter bar items rendered after the search box. Each item has a label and a control.

```tsx
<EntityTable
    tableActions={[
        {
            label: <span>Status</span>,
            children: (
                <Select ... onChange={(v) => setFilter('status', v)} />
            ),
        },
    ]}
/>
```

```ts
{
    label: ReactNode;     // Label above the control
    children: ReactNode;  // The actual control (Input, Select, etc.)
}
```

In non-readonly mode, `ManagerPageContainer` automatically prepends the batch operation selector to `tableActions`. The built-in time range picker, FilterBuilder, and refresh button are also appended.

### tablePrefixActions

Filter bar items rendered before the search box. Same structure as `tableActions`.

## Global Search

### searchKeywords

When provided, a global search box is displayed. Enter a keyword and press Enter to search. All fields in `searchKeywords` are **OR**-combined — a record matches if any field contains the keyword.

```tsx
<EntityTable searchKeywords={['username', 'email', 'nickname']} />
```

Effect: typing `alice` and pressing Enter → queries records where `username`, `email`, or `nickname` contains `alice`.

## Inline Filters

### simpleFilters

Per-field filter conditions provided by the developer. All conditions are **AND**-combined. Use with `filters` from `useManagerQueryParams`.

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
    field: string;        // Query field name
    operator?: string;    // Comparison operator, default "contains"
    value: unknown;       // Filter value, skipped when undefined/null/''
    urlKey?: string;      // URL param name, defaults to `field`
}
```

::: tip Note
- `operator` defaults to `"contains"` (substring match). Use `"eq"` explicitly for numeric or enum fields.
- `urlKey` controls the URL param name. When omitted, the URL param name equals `field`. When provided, the URL uses `urlKey` while the request still uses `field`.
:::

## Advanced Filtering

### filterableFields

When provided, a FilterBuilder advanced condition builder is displayed in the filter bar. Users can freely compose AND/OR conditions across multiple fields with various operators.

```tsx
<EntityTable
    filterableFields={[
        { field: 'username', type: 'text',   label: 'Username' },
        { field: 'email',    type: 'text',   label: 'Email' },
        { field: 'nickname', type: 'text',   label: 'Nickname' },
    ]}
/>
```

See [Advanced Filtering](/en/develop/frontend/manager/standard-manager#advanced-filtering) for details.

## Time Range

### showTimeRangeFilter

Whether to show the built-in time range picker. Defaults to `true`. The selected range is AND-ed into the query as `createdTime` GTE/LTE conditions.

```tsx
<EntityTable showTimeRangeFilter={false} />
```

## Column Control

### hideRecordTimeColumn

Whether to hide the automatically appended "Record Time" column (displays createdTime and modifiedTime). Defaults to `false`.

```tsx
<EntityTable hideRecordTimeColumn />
```

## Row Selection

### tableSelection

Controls row selection behavior. When omitted, `ManagerPageContainer` automatically decides based on `readonlyMode`: `checkbox` by default in non-readonly mode, disabled in read-only mode.

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

## Row Actions

### tableRowActionsRender

Render custom action buttons before the built-in edit/delete buttons on each row.

```tsx
<EntityTable
    tableRowActionsRender={(record) => (
        <Button icon={<DownloadOutlined />} onClick={() => download(record)} />
    )}
/>
```

## URL Sync

### queryParamsSync

Called automatically **after each query** to write the current page, search keyword, and time range to the browser address bar. Pass `syncToUrl` from `useManagerQueryParams()`.

**When**: After the user changes pages, types a search keyword, switches time range, or modifies FilterBuilder conditions — EntityTable fires the query, then calls `queryParamsSync(urlParams)` to update the URL.

**Effect**: The URL becomes `?page=1&searchKeyword=alice&startTime=1700000000000`. Refreshing the page or sharing the link preserves all filter state.

### initialQueryValues

Extracted **from the URL on first load** to restore the previous browsing state. Pass `initialQueryValues` from `useManagerQueryParams()`.

**How it works**: `useManagerQueryParams` parses `window.location.search` on init and extracts reserved keys (`page`, `pageSize`, `searchKeyword`, `startTime`, `endTime`, `query`) into the `initialQueryValues` object.

**Effect**: A user arriving from `?page=3&searchKeyword=alice` sees the table on page 3 with `alice` pre-filled in the search box.

```tsx
const { syncToUrl, initialQueryValues } = useManagerQueryParams({ schema: { status: 'string' } });

<EntityTable
    queryParamsSync={syncToUrl}
    initialQueryValues={initialQueryValues}
/>
```

### initialQueryValues Fields

| Field | Type | Description |
|-------|------|-------------|
| `page` | `number` | Current page number |
| `pageSize` | `number` | Items per page |
| `searchKeyword` | `string` | Search keyword |
| `startTime` | `number \| string` | Time range start timestamp |
| `endTime` | `number \| string` | Time range end timestamp |
| `query` | `unknown` | Serialized FilterBuilder query node |

## Ref Methods

Control the table externally via `ref`:

```tsx
const tableRef = useRef<EntityTableRef>(null);

tableRef.current?.refreshData();
tableRef.current?.refreshData({ resetPage: true });
tableRef.current?.clearSelection();
```

### refreshData(options?)

Re-trigger the query.

| Parameter | Type | Description |
|-----------|------|-------------|
| `options.resetPage` | `boolean` | Whether to reset to page 1, default `false` |

### clearSelection()

Clear all selected rows.

---

## Complete Props Reference

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `entityName` | `string` | — | Entity name |
| `columns` | `EntityTableColumns<ENTITY>` | — | Table column definitions |
| `query` | `(props: T) => Promise<PaginatedResponseData<ENTITY>>` | — | Paginated query function |
| `tableActions` | `TableAction[]` | — | Filter bar items after the search box |
| `tablePrefixActions` | `TableAction[]` | — | Filter bar items before the search box |
| `searchKeywords` | `string[]` | — | Global search fields, search box hidden when omitted |
| `simpleFilters` | `SimpleFilter[]` | — | Inline filter conditions |
| `filterableFields` | `FilterableField[]` | — | FilterBuilder available fields |
| `showTimeRangeFilter` | `boolean` | `true` | Whether to show the time range picker |
| `hideRecordTimeColumn` | `boolean` | `false` | Whether to hide the record time column |
| `tableSelection` | `TableSelectionConfig` | — | Row selection config |
| `tableRowActionsRender` | `(record: ENTITY) => ReactNode` | — | Custom row action buttons |
| `queryParamsSync` | `(params: object) => void` | — | URL sync callback |
| `initialQueryValues` | `object` | — | Initial values from URL |
| `children` | `ReactNode` | — | Content below the table |
