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

## Adding Filters

Use `useManagerQueryParams` with a `schema` to declare filter fields. Filter state is automatically synced to the URL, enabling shareable links and state restoration on refresh.

```tsx
import { useRef } from 'react';
import { Input, Select } from 'antd';
import { ManagerPageContainer, type ManagerPageContainerRef } from '@/components/ManagerPageContainer.tsx';
import { useManagerQueryParams } from '@/compositions/use-manager-query-params.ts';

export default function MyEntityManagerPage() {
    const pageRef = useRef<ManagerPageContainerRef | null>(null);

    const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({
        schema: {
            status: 'string',
            userId: 'string',
            type: 'number',
        }
    });

    // Reset to page 1 and refresh whenever any filter changes
    useEffect(() => {
        pageRef.current?.refreshData({ resetPage: true });
    }, [filters.status, filters.userId, filters.type]);

    return (
        <>
            <ActionBarComponent ... />
            <ManagerPageContainer
                ref={pageRef}
                // ...other required props
                queryParamsSync={syncToUrl}
                initialQueryValues={initialQueryValues}
                extraQueryParams={filters}
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
            />
        </>
    );
}
```

### useManagerQueryParams Schema Field Types

| Type value | TypeScript type | Notes |
|------------|----------------|-------|
| `'string'` | `string` | Plain text, read/written as-is |
| `'number'` | `number` | Automatically converted via `Number()` when read from URL |
| `'boolean'` | `boolean` | Recognizes `'true'` / `'false'` strings from URL |

### useManagerQueryParams Return Values

| Return value | Type | Description |
|--------------|------|-------------|
| `filters` | `FiltersFromSchema<S>` | Current filter values object, all fields optional |
| `setFilter(key, value)` | `function` | Update a single filter field; pass `undefined` to clear it |
| `syncToUrl` | `function` | Pass to `queryParamsSync` — syncs query params to URL after each query |
| `initialQueryValues` | `object` | Pass to `initialQueryValues` — restores pagination and search state from URL |
| `getInitialParam(key)` | `function` | Manual mode: read the initial string value of a URL param |

---

## Props Reference

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

### Display Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `entityName` | `string` | — | Entity name used in modal titles and operation messages |
| `title` | `string` | — | Title for the built-in ActionBar (only when `showActionBar` is `true`) |
| `subtitle` | `string` | — | Subtitle for the built-in ActionBar |
| `columns` | `EntityTableColumns<ENTITY>` | — | Table column definitions, typically returned by a `useXxxColumns()` hook |

---

### Visibility Control Props

| Prop | Type | Default | Description |
|------|------|---------|-------------|
| `showActionBar` | `boolean` | `true` | Whether to show the built-in ActionBar (with the create button). Usually set to `false` when placing `ActionBarComponent` outside the container |
| `readonlyMode` | `boolean` | `false` | Read-only mode: hides create button, row edit/delete buttons, and disables batch selection |
| `showRowActions` | `boolean` | `true` | Whether to show per-row edit/delete action buttons |
| `showTimeRangeFilter` | `boolean` | `true` | Whether to show the built-in time range picker in the filter bar |
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
| `extraQueryParams` | `Record<string, unknown>` | Extra query params merged into every request. Use with `useManagerQueryParams({ schema }).filters` — no `queryParamsProvider` needed on each action |
| `queryParamsSync` | `(params: Record<string, unknown>) => void` | Called after each query to sync params to the URL. Pass `syncToUrl` from `useManagerQueryParams()` |
| `initialQueryValues` | `object` | Initial query values to restore pagination, search keyword, and time range from URL. Pass `initialQueryValues` from `useManagerQueryParams()` |

---

### Filter Bar Props

| Prop | Type | Description |
|------|------|-------------|
| `tableActions` | `TableAction[]` | Filter bar items on the right side. Each item has `label`, `children`, and an optional `queryParamsProvider` |
| `tablePrefixActions` | `TableAction[]` | Filter bar items on the left side. Same structure as `tableActions`. The built-in batch operation selector is prepended here automatically |

`TableAction` structure:

```ts
{
    label: ReactNode;           // Label text above the control
    children: ReactNode;        // The actual control (Input, Select, etc.)
    queryParamsProvider?: () => object;  // Optional: returns query params for this control
}
```

::: tip queryParamsProvider vs extraQueryParams
- `extraQueryParams` (recommended): Use with `useManagerQueryParams({ schema })`. Filter state is managed centrally and synced to URL automatically — minimal boilerplate.
- `queryParamsProvider`: Suitable when URL sync is not needed, or when a field requires special transformation. Both approaches can coexist; `queryParamsProvider` takes higher priority.
:::

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
