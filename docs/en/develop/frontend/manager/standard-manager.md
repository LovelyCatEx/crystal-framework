# Standard Manager Page

`ManagerPageContainer` is the framework's built-in generic management page container. It encapsulates a full feature set including data table, pagination, search, create/edit/delete modals, batch operations, time range filtering, and column visibility control.

For CRUD management pages whose entity type extends `BaseEntity`, `ManagerPageContainer` covers everything out of the box — no manual table or modal setup required.

## Quick Start

A minimal standard manager page:

```tsx
import { ManagerPageContainer } from '@/components/ManagerPageContainer.tsx';
import { useMyEntityColumns } from '@/components/columns/MyEntityColumns.tsx';
import { MyEntityController } from '@/api/my-entity.api.ts';
import { ActionBarComponent } from '@/components/ActionBarComponent.tsx';
import { useTranslation } from 'react-i18next';

export default function MyEntityManagerPage() {
    const { t } = useTranslation();
    const columns = useMyEntityColumns();

    return (
        <>
            <ActionBarComponent
                title={t('pages.myEntity.title')}
                subtitle={t('pages.myEntity.subtitle')}
            />
            <ManagerPageContainer
                entityName={t('entityNames.myEntity')}
                title=""
                subtitle=""
                showActionBar={false}
                columns={columns}
                editModalFormChildren={<MyEntityForm />}
                query={async (props) => (await MyEntityController.query(props)).data!}
                delete={async (props) => MyEntityController.delete(props)}
                update={async (props) => MyEntityController.update(props)}
                create={async (props) => MyEntityController.create(props)}
            />
        </>
    );
}
```

::: tip Underlying Component
The data table, filtering, and search features of `ManagerPageContainer` are all powered by the [Data Table](/en/develop/frontend/components/entity-table) component. To use the table independently of `ManagerPageContainer`, or to see the full props reference, check out that component's documentation.
:::

## Global Search

Pass `searchKeywords` to show a global search box in the filter bar. Enter a keyword and press Enter to search. All fields in `searchKeywords` are **OR**-combined — the record matches if any field contains the keyword.

```tsx
<ManagerPageContainer
    searchKeywords={['username', 'email', 'nickname']}
    // ...
/>
```

**Effect**: The user types `alice` and presses Enter → the query matches records where `username`, `email`, or `nickname` contains `alice`, the table resets to page 1, and the URL syncs to `?searchKeyword=alice`.

::: tip Combining with Advanced Filters
When both `searchKeywords` and `filterableFields` are configured, an AND/OR toggle switch automatically appears in the filter bar, controlling how global search and advanced filter conditions combine.
:::

---

## Adding Filters

Management pages typically provide dropdowns, inputs, and other controls for users to filter data. The framework uses `useManagerQueryParams` to centrally manage filter state, automatically syncing it to the browser URL (`?status=active&userId=123`) so filters survive page refreshes and shared links.

The workflow has four steps:

1. **Declare a schema** — define what filters exist and their types
2. **Read with `filters`** — use `filters.xxx` in control `defaultValue` / `value`
3. **Write with `setFilter`** — call `setFilter('key', value)` in control `onChange` / `onPressEnter`
4. **Sync to URL** — pass `syncToUrl` and `initialQueryValues` to `ManagerPageContainer`

### Step 1: Declare the Schema

```tsx
import { useManagerQueryParams } from '@/compositions/use-manager-query-params.ts';

const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({
    schema: {
        status: 'string',   // status, text type
        userId: 'string',   // user ID, text type
        type: 'number',     // type, number type
    }
});
```

Each key in `schema` is a filter condition. `'string'` means text, `'number'` auto-converts from URL via `Number()`, `'boolean'` recognizes `'true'` / `'false'`.

**Effect**: After declaration, `filters` gains `.status`, `.userId`, and `.type` fields, all initially `undefined` (meaning "no filter applied").

### Step 2: Read Values with `filters`

`filters` is a reactive object holding the current value of each filter condition. When the user interacts with a control → `setFilter` updates → `filters` changes immediately.

```tsx
// filters.status is currently 'active', 'disabled', or undefined (all)
console.log(filters.status);  // => 'active'

// filters.userId is the user's input text, or undefined (not entered)
console.log(filters.userId);  // => '123'
```

### Step 3: Update Values with `setFilter`

`setFilter(key, value)` updates a filter field. **Pass `undefined` as the value to clear it (no filtering).**

```tsx
// Set status to 'active'
setFilter('status', 'active');

// Clear the status condition (back to "all")
setFilter('status', undefined);

// Set userId from input value; treat empty string as clear
setFilter('userId', e.target.value || undefined);
```

Typical patterns: Select "All" → pass `undefined`; Input cleared → pass `undefined`.

### Step 4: Sync to URL and Trigger Refresh

```tsx
// 1. Pass syncToUrl / initialQueryValues to the container
<ManagerPageContainer
    queryParamsSync={syncToUrl}
    initialQueryValues={initialQueryValues}
    simpleFilters={[
        // Map filters values to query conditions sent to the backend
        { field: 'status',  value: filters.status },
        { field: 'user_id', value: filters.userId ? Number(filters.userId) : undefined },
        { field: 'type',    value: filters.type },
    ]}
    // ...
/>

// 2. Watch filters changes and trigger refresh
useEffect(() => {
    pageRef.current?.refreshData({ resetPage: true });
}, [filters.status, filters.userId, filters.type]);
```

**Effect**: The URL updates to `?status=active&userId=123` automatically, and the table resets to page 1. Share the URL or refresh the page — filter state is preserved.

### Complete Example

```tsx
import { useRef, useEffect } from 'react';
import { Input, Select } from 'antd';
import { ManagerPageContainer, type ManagerPageContainerRef } from '@/components/ManagerPageContainer.tsx';
import { useManagerQueryParams } from '@/compositions/use-manager-query-params.ts';

export default function MyEntityManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);

    const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({
        schema: {
            status: 'string',
            userId: 'string',
        }
    });

    useEffect(() => {
        pageRef.current?.refreshData({ resetPage: true });
    }, [filters.status, filters.userId]);

    return (
        <>
            <ActionBarComponent title="Entity Management" subtitle="Manage all entities" />
            <ManagerPageContainer
                ref={pageRef}
                entityName="Entity"
                title=""
                subtitle=""
                showActionBar={false}
                columns={columns}
                editModalFormChildren={<MyForm />}
                queryParamsSync={syncToUrl}
                initialQueryValues={initialQueryValues}
                simpleFilters={[
                    { field: 'status',  value: filters.status },
                    { field: 'user_id', value: filters.userId ? Number(filters.userId) : undefined },
                ]}
                tableActions={[
                    {
                        label: <span>Status</span>,
                        children: (
                            <Select
                                defaultValue={filters.status ?? 'all'}
                                options={[
                                    { value: 'all', label: 'All' },
                                    { value: 'active', label: 'Active' },
                                    { value: 'disabled', label: 'Disabled' },
                                ]}
                                onChange={(v) => setFilter('status', v === 'all' ? undefined : v)}
                            />
                        ),
                    },
                    {
                        label: <span>User ID</span>,
                        children: (
                            <Input
                                defaultValue={filters.userId}
                                allowClear
                                onPressEnter={(e) => setFilter('userId', (e.target as HTMLInputElement).value || undefined)}
                                onChange={(e) => { if (e.target.value === '') setFilter('userId', undefined); }}
                            />
                        ),
                    },
                ]}
                query={async (props) => (await MyEntityController.query(props)).data!}
                delete={async (props) => MyEntityController.delete(props)}
                update={async (props) => MyEntityController.update(props)}
                create={async (props) => MyEntityController.create(props)}
            />
        </>
    );
}
```

### useManagerQueryParams API

**Parameter** — `schema` field types:

| Type value | TypeScript type | Notes |
|------------|----------------|-------|
| `'string'` | `string` | Plain text, read/written as-is |
| `'number'` | `number` | Automatically converted via `Number()` when read from URL |
| `'boolean'` | `boolean` | Recognizes `'true'` / `'false'` strings from URL |

**Return values**:

| Return value | Type | Description |
|--------------|------|-------------|
| `filters` | `{ [K in keyof S]: S[K] \| undefined }` | Current filter values. Field names match the schema. All fields default to `undefined` |
| `setFilter(key, value)` | `(key: string, value: any \| undefined) => void` | Update a filter field. `key` must be a field declared in the schema. Pass `undefined` as `value` to clear the condition |
| `syncToUrl` | `(params: object) => void` | Pass to `ManagerPageContainer`'s `queryParamsSync`. Syncs query params to URL after each query |
| `initialQueryValues` | `object` | Pass to `ManagerPageContainer`'s `initialQueryValues`. Restores state from URL on page load |
| `getInitialParam(key)` | `(key: string) => string` | Read the initial string value of a URL param (no type conversion) |

---

## Advanced Filtering

The `ManagerPageContainer` has built-in `FilterBuilder` support for advanced conditional search. Pass `filterableFields` to enable a visual query builder in the filter bar, allowing users to freely compose AND/OR conditions across multiple fields with various operators.

```tsx
<ManagerPageContainer
    filterableFields={[
        { field: 'username',  type: 'text',   label: t('pages.userManager.filter.username') },
        { field: 'email',     type: 'text',   label: t('pages.userManager.filter.email') },
        { field: 'nickname',  type: 'text',   label: t('pages.userManager.filter.nickname') },
    ]}
    searchKeywords={['username', 'email', 'nickname']}
    // ...
/>
```

When both `searchKeywords` (global search) and `filterableFields` (advanced filter) are configured, an AND/OR toggle switch automatically appears in the filter bar to control how the two groups combine.

### FilterableField Types & Operators

| `type` | Available Operators |
|--------|--------------------|
| `text` | eq, ne, contains, like, not_contains, is_null, is_not_null |
| `number` | eq, ne, gt, gte, lt, lte, is_null, is_not_null |
| `select` | eq, ne, is_null, is_not_null |
| `date` | eq, ne, gt, gte, lt, lte, is_null, is_not_null |
| `dateTime` | eq, ne, gt, gte, lt, lte, is_null, is_not_null |

### Operator Reference

| Operator | Meaning | Description |
|----------|---------|-------------|
| `eq` | Equals | Exact match |
| `ne` | Not Equals | Exclude match |
| `gt` | Greater Than | Value greater than specified |
| `gte` | Greater Than or Equal | Value greater than or equal to specified |
| `lt` | Less Than | Value less than specified |
| `lte` | Less Than or Equal | Value less than or equal to specified |
| `contains` | Contains | String contains substring |
| `not_contains` | Not Contains | String does not contain substring |
| `like` | Like | SQL LIKE syntax, supports `%` wildcard |
| `is_null` | Is Null | Field value is NULL |
| `is_not_null` | Is Not Null | Field value is not NULL |

`FilterableField` structure:

```ts
interface FilterableField {
    field: string;                // Data field name
    label: string;                // Human-readable field label
    type: 'text' | 'number' | 'select' | 'date' | 'dateTime';
    options?: { label: string; value: string | number }[];  // select-type only, dropdown options
    renderValue?: (ctx: {        // Custom value input control
        value: unknown;
        onChange: (v: unknown) => void;
        operator: string | null;
    }) => ReactNode;
}
```

::: tip
Built-in `created_time` and `modified_time` fields are automatically appended and do not need to be manually added.
:::

---

## Props Reference

### Display Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `entityName` | `string` | — | Entity name used in modal titles and operation messages |
| `title` | `string` | — | Title for the built-in ActionBar (only when `showActionBar` is `true`) |
| `subtitle` | `string` | — | Subtitle for the built-in ActionBar |
| `columns` | `EntityTableColumns<ENTITY>` | — | Table column definitions, typically returned by a `useXxxColumns()` hook |

---

### Data Operation Props

These four props are required for the component to function.

| Prop | Type | Required | Description |
|------|------|----------|-------------|
| `query` | `(props: T) => Promise<PaginatedResponseData<ENTITY>>` | ✅ | Paginated query function. The component automatically passes `page`, `pageSize`, `searchKeyword`, and all filter params |
| `create` | `(props: T) => Promise<unknown>` | ✅ | Create function, called when the create modal is submitted |
| `update` | `(props: T) => Promise<unknown>` | ✅ | Update function, called when the edit modal is submitted |
| `delete` | `(props: T) => Promise<unknown>` | ✅ | Delete function, receives `{ ids: string[] }` |

For read-only pages, pass no-op functions returning `null`:

```tsx
delete={async () => null}
update={async () => null}
create={async () => null}
```

---

### Visibility Control Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `showActionBar` | `boolean` | `true` | Whether to show the built-in ActionBar (with the create button). Usually set to `false` when placing `ActionBarComponent` outside the container |
| `readonlyMode` | `boolean` | `false` | Read-only mode: hides create button, row edit/delete buttons, and disables batch selection |
| `showRowActions` | `boolean` | `true` | Whether to show per-row edit/delete action buttons |
| `showTimeRangeFilter` | `boolean` | `true` | Whether to show the built-in time range picker. See [Filter Bar Props](#filter-bar-props) |
| `hideRecordTimeColumn` | `boolean` | `false` | Whether to hide the "Record Time" column that is automatically appended to the table |

---

### Modal Props

| Prop | Type | Description |
|------|------|-------------|
| `editModalFormChildren` | `ReactNode \| ((editingItem: ENTITY \| null) => ReactNode)` | Form content inside the create/edit modal. Can be static JSX or a function receiving the current editing record |
| `editModalInitialValues` | `object` | Initial form values when the create modal opens |

#### Field Names Must Match Entity Fields

Each `Form.Item`'s `name` prop inside `editModalFormChildren` must match the corresponding field name on the entity.

- **Edit mode**: The component automatically calls `form.setFieldsValue(record)` to populate the form with the selected row's data. Fields are filled in by matching `name` to the entity's field names.
- **Create mode**: The form is reset to empty. If `editModalInitialValues` is provided, those values are applied as defaults.

#### What Happens on Submit

When the user clicks the modal's confirm button, the component calls `form.submit()` internally, which triggers Ant Design Form's `onFinish` and passes the **complete form values object** as the argument:

- **Edit mode** (opened via a row's edit button): calls `props.update(values)`
- **Create mode** (opened via the create button): calls `props.create(values)`

`values` is the full object collected from the form, with keys matching the entity fields. In edit mode, the form also contains a hidden `id` field injected by the component automatically — so `update` receives `values` with `id` included, ready to pass directly to the backend.

```tsx
// Example: form fields matching entity fields
editModalFormChildren={
    <>
        {/* name="username" maps to the entity's username field */}
        <Form.Item name="username" label="Username" rules={[{ required: true }]}>
            <Input />
        </Form.Item>

        {/* name="roleId" maps to the entity's roleId field */}
        <Form.Item name="roleId" label="Role">
            <Select options={roleOptions} />
        </Form.Item>
    </>
}

// values received by update (edit mode):
// { id: 'xxx', username: 'alice', roleId: '2' }

// values received by create (create mode):
// { username: 'bob', roleId: '1' }
```

Function form — render different fields based on create vs. edit:

```tsx
editModalFormChildren={(editingItem) => (
    <>
        <Form.Item name="username" label="Username" rules={[{ required: true }]}>
            <Input />
        </Form.Item>
        {/* Only show status field when editing */}
        {editingItem && (
            <Form.Item name="status" label="Status">
                <Select options={statusOptions} />
            </Form.Item>
        )}
    </>
)}
```

---

### Filter & URL Sync Props

| Prop | Type | Description |
|------|------|-------------|
| `queryParamsSync` | `(params: Record<string, unknown>) => void` | Called after each query to sync params to the URL. Pass `syncToUrl` from `useManagerQueryParams()` |
| `initialQueryValues` | `object` | Initial query values to restore pagination, search keyword, and time range from URL. Pass `initialQueryValues` from `useManagerQueryParams()` |

---

### Filter Bar Props

| Prop | Type | Description |
|------|------|-------------|
| `tableActions` | `TableAction[]` | Filter bar items rendered after the search box. Each item has `label` and `children`. In non-readonly mode, the built-in batch operation selector is prepended automatically |
| `tablePrefixActions` | `TableAction[]` | Filter bar items rendered before the search box. Same structure as `tableActions` |
| `searchKeywords` | `string[]` | Fields used by the global search box (OR-combined). Search box is hidden when omitted |
| `simpleFilters` | `SimpleFilter[]` | Inline filter conditions AND-combined into every query. Use with `filters` from `useManagerQueryParams` |
| `filterableFields` | `FilterableField[]` | Fields available in the FilterBuilder. FilterBuilder is hidden when omitted |
| `showTimeRangeFilter` | `boolean` | Whether to show the built-in time range picker (createdTime range), default `true` |

`TableAction` structure:

```ts
{
    label: ReactNode;           // Label text above the control
    children: ReactNode;        // The actual control (Input, Select, etc.)
}
```

`SimpleFilter` structure:

```ts
{
    field: string;              // Query field name
    operator?: string;          // Comparison operator, default "contains"
    value: unknown;             // Filter value, ignored when undefined/null/''
    urlKey?: string;            // URL param name, defaults to `field`
}
```

::: tip SimpleFilter Notes
- All `simpleFilters` are **AND**-combined — every condition must match.
- `operator` defaults to `"contains"` (substring match). For numeric/enum fields, pass `"eq"` explicitly.
- When `value` is `undefined`, `null`, or an empty string, that filter is silently skipped.
- `urlKey` controls the URL param name. When omitted, the URL param name equals `field`. Use it when the field name and the desired URL key differ (e.g. `field: 'user_id'`, `urlKey: 'userId'`).
:::
```

---

### Table Selection Props

| Prop | Type | Description |
|------|------|-------------|
| `tableSelection` | `TableSelectionConfig` | Custom row selection behavior. When omitted, checkbox selection is enabled by default in non-readonly mode |

`TableSelectionConfig` structure:

```ts
{
    type: 'disabled' | 'checkbox' | 'radio';
    onChange?: (records: ENTITY[]) => void;
    isDisabled?: (record: ENTITY) => boolean;
}
```

---

### Row Action Props

| Prop | Type | Description |
|------|------|-------------|
| `tableRowActionsRender` | `(record: ENTITY) => ReactNode` | Render custom action buttons before the built-in edit/delete buttons on each row |

```tsx
tableRowActionsRender={(record) => (
    <Button
        type="text"
        size="small"
        icon={<EyeOutlined />}
        onClick={() => openDetail(record)}
    />
)}
```

---

### Ref Methods

Control the component externally via `ref`:

```tsx
const pageRef = useRef<ManagerPageContainerRef | null>(null);

// Refresh data
pageRef.current?.refreshData();

// Refresh and reset to page 1
pageRef.current?.refreshData({ resetPage: true });

// Open the create modal
pageRef.current?.openModal();

// Clear table row selection
pageRef.current?.clearSelection();
```

::: tip i18n Key Naming
Page titles use `pages.{pageName}.title` / `.subtitle`, entity names use `entityNames.{entityName}`, filter labels use `pages.{pageName}.filter.{field}`, and advanced filter field labels use `components.filterBuilder.{field}`. See [i18n](../frontend/components/i18n).
:::
