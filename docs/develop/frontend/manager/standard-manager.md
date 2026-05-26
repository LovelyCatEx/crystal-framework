# 标准化 Manager 页面

`ManagerPageContainer` 是框架内置的通用管理页面容器组件，封装了数据表格、分页、搜索、新增/编辑/删除弹窗、批量操作、时间范围筛选、列显示控制等完整功能。

对于实体类型继承自 `BaseEntity` 的 CRUD 管理页面，直接使用 `ManagerPageContainer` 即可，无需手动搭建表格和弹窗。

## 快速上手

一个最简单的标准化 Manager 页面：

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

## 添加过滤条件

使用 `useManagerQueryParams` 配合 `schema` 声明过滤字段，过滤状态会自动同步到 URL，支持分享链接和刷新后回填。

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

    // 任意 filter 变化时重置到第一页并刷新
    useEffect(() => {
        pageRef.current?.refreshData({ resetPage: true });
    }, [filters.status, filters.userId, filters.type]);

    return (
        <>
            <ActionBarComponent ... />
            <ManagerPageContainer
                ref={pageRef}
                // ...其他必填参数
                queryParamsSync={syncToUrl}
                initialQueryValues={initialQueryValues}
                extraQueryParams={filters}
                tableActions={[
                    {
                        label: <span>状态</span>,
                        children: (
                            <Select
                                defaultValue={filters.status ?? 'all'}
                                options={[
                                    { value: 'all', label: '全部' },
                                    { value: 'active', label: '启用' },
                                    { value: 'disabled', label: '禁用' },
                                ]}
                                onChange={(v) => setFilter('status', v === 'all' ? undefined : v)}
                            />
                        ),
                    },
                    {
                        label: <span>用户 ID</span>,
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

### useManagerQueryParams schema 字段类型

| 类型值 | TypeScript 类型 | 说明 |
|--------|----------------|------|
| `'string'` | `string` | 文本，原样读写 |
| `'number'` | `number` | 数字，从 URL 读取时自动 `Number()` 转换 |
| `'boolean'` | `boolean` | 布尔，从 URL 读取时识别 `'true'` / `'false'` |

### useManagerQueryParams 返回值

| 返回值 | 类型 | 说明 |
|--------|------|------|
| `filters` | `FiltersFromSchema<S>` | 当前过滤值对象，所有字段均为可选 |
| `setFilter(key, value)` | `function` | 更新单个过滤字段，传 `undefined` 清除该字段 |
| `syncToUrl` | `function` | 传给 `queryParamsSync`，每次查询后自动同步 URL |
| `initialQueryValues` | `object` | 传给 `initialQueryValues`，从 URL 恢复分页和搜索状态 |
| `getInitialParam(key)` | `function` | 手动模式下读取 URL 中某个字段的初始值（字符串） |

---

## 参数说明

### 数据操作参数

这四个参数是组件运行的核心，必须提供。

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `query` | `(props: T) => Promise<PaginatedResponseData<ENTITY>>` | ✅ | 分页查询函数，组件会自动传入 `page`、`pageSize`、`searchKeyword` 及所有过滤参数 |
| `create` | `(props: T) => Promise<unknown>` | ✅ | 新增函数，弹窗提交时调用 |
| `update` | `(props: T) => Promise<unknown>` | ✅ | 更新函数，编辑弹窗提交时调用 |
| `delete` | `(props: T) => Promise<unknown>` | ✅ | 删除函数，接收 `{ ids: string[] }` |

只读页面不需要增删改时，传入返回 `null` 的空函数即可：

```tsx
delete={async () => null}
update={async () => null}
create={async () => null}
```

---

### 基础展示参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `entityName` | `string` | — | 实体名称，用于弹窗标题、操作提示等文案 |
| `title` | `string` | — | 内置 ActionBar 的标题（`showActionBar` 为 `true` 时生效） |
| `subtitle` | `string` | — | 内置 ActionBar 的副标题 |
| `columns` | `EntityTableColumns<ENTITY>` | — | 表格列定义，通常由 `useXxxColumns()` hook 返回 |

---

### 显示控制参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `showActionBar` | `boolean` | `true` | 是否显示组件内置的 ActionBar（含新增按钮）。通常设为 `false`，在页面外层自行放置 `ActionBarComponent` |
| `readonlyMode` | `boolean` | `false` | 只读模式，隐藏新增按钮、行内编辑/删除按钮，并禁用批量操作选择框 |
| `showRowActions` | `boolean` | `true` | 是否显示每行的编辑/删除操作按钮 |
| `showTimeRangeFilter` | `boolean` | `true` | 是否在过滤栏显示内置的时间范围选择器 |
| `hideRecordTimeColumn` | `boolean` | `false` | 是否隐藏表格末尾自动追加的"记录时间"列 |

---

### 弹窗参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `editModalFormChildren` | `ReactNode \| ((editingItem: ENTITY \| null) => ReactNode)` | 新增/编辑弹窗内的表单内容。可以是静态 JSX，也可以是接收当前编辑对象的函数（用于根据编辑状态动态渲染） |
| `editModalInitialValues` | `object` | 新增弹窗打开时表单的初始值 |

#### 字段名与实体字段保持一致

`editModalFormChildren` 内部的每个 `Form.Item` 的 `name` 属性必须与实体类的字段名保持一致。

- **编辑时**：组件会自动调用 `form.setFieldsValue(record)` 将当前行数据填入表单，`name` 对应的字段会被自动回填。
- **新增时**：表单重置为空，若传了 `editModalInitialValues` 则按其填充初始值。

#### 提交后的数据流向

用户点击弹窗的确认按钮后，组件内部调用 `form.submit()`，触发 Ant Design Form 的 `onFinish`，将**整个表单的字段值对象**作为参数传出：

- 若当前是**编辑状态**（从表格行点击编辑打开），调用 `props.update(values)`
- 若当前是**新增状态**（点击新增按钮打开），调用 `props.create(values)`

其中 `values` 就是表单收集到的完整对象，结构与实体字段一一对应。编辑时表单中还会包含一个隐藏的 `id` 字段（组件内部自动注入），因此 `update` 收到的 `values` 里会带有 `id`，可以直接传给后端接口。

```tsx
// 示例：表单字段与实体对应
editModalFormChildren={
    <>
        {/* name="username" 对应实体的 username 字段 */}
        <Form.Item name="username" label="用户名" rules={[{ required: true }]}>
            <Input />
        </Form.Item>

        {/* name="roleId" 对应实体的 roleId 字段 */}
        <Form.Item name="roleId" label="角色">
            <Select options={roleOptions} />
        </Form.Item>
    </>
}

// update 收到的 values 示例（编辑时）：
// { id: 'xxx', username: 'alice', roleId: '2' }

// create 收到的 values 示例（新增时）：
// { username: 'bob', roleId: '1' }
```

`editModalFormChildren` 函数形式示例（根据新增/编辑状态动态渲染）：

```tsx
editModalFormChildren={(editingItem) => (
    <>
        <Form.Item name="username" label="用户名" rules={[{ required: true }]}>
            <Input />
        </Form.Item>
        {/* 仅编辑时显示状态字段 */}
        {editingItem && (
            <Form.Item name="status" label="状态">
                <Select options={statusOptions} />
            </Form.Item>
        )}
    </>
)}
```

---

### 过滤与 URL 同步参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `extraQueryParams` | `Record<string, unknown>` | 额外的查询参数，每次发起查询时会合并进请求体。配合 `useManagerQueryParams({ schema })` 的 `filters` 使用，无需在每个 `tableAction` 上写 `queryParamsProvider` |
| `queryParamsSync` | `(params: Record<string, unknown>) => void` | 每次查询后的回调，用于将当前查询参数同步到 URL。传入 `useManagerQueryParams()` 返回的 `syncToUrl` 即可 |
| `initialQueryValues` | `object` | 初始查询值，用于从 URL 恢复分页、搜索关键词和时间范围。传入 `useManagerQueryParams()` 返回的 `initialQueryValues` 即可 |

---

### 过滤栏参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `tableActions` | `TableAction[]` | 过滤栏右侧的操作项，每项包含 `label`（标签）、`children`（控件）和可选的 `queryParamsProvider`（返回该项的查询参数，不需要 URL 同步时使用） |
| `tablePrefixActions` | `TableAction[]` | 过滤栏左侧的操作项，结构与 `tableActions` 相同。内置的批量操作选择器会自动插入到此处 |

`TableAction` 结构：

```ts
{
    label: ReactNode;           // 控件上方的标签文字
    children: ReactNode;        // 实际的控件（Input、Select 等）
    queryParamsProvider?: () => object;  // 可选，返回该控件对应的查询参数
}
```

::: tip queryParamsProvider vs extraQueryParams
- `extraQueryParams`（推荐）：配合 `useManagerQueryParams({ schema })` 使用，过滤状态统一管理，自动同步 URL，代码最简洁。
- `queryParamsProvider`：适合不需要 URL 同步、或需要对某个字段做特殊处理的场景。两者可以混用，`queryParamsProvider` 的优先级更高。
:::

---

### 表格选择参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `tableSelection` | `TableSelectionConfig` | 自定义行选择行为。不传时，非只读模式下默认启用 checkbox 批量选择；只读模式下自动禁用 |

`TableSelectionConfig` 结构：

```ts
{
    type: 'disabled' | 'checkbox' | 'radio';
    onChange?: (records: ENTITY[]) => void;   // 选中行变化时的回调
    isDisabled?: (record: ENTITY) => boolean; // 某行是否禁止选择
}
```

---

### 行操作参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `tableRowActionsRender` | `(record: ENTITY) => ReactNode` | 在每行内置的编辑/删除按钮之前，插入自定义操作按钮 |

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

### Ref 方法

通过 `ref` 可以从外部控制组件：

```tsx
const pageRef = useRef<ManagerPageContainerRef | null>(null);

// 刷新数据
pageRef.current?.refreshData();

// 刷新并重置到第一页
pageRef.current?.refreshData({ resetPage: true });

// 打开新增弹窗
pageRef.current?.openModal();

// 清空表格选中状态
pageRef.current?.clearSelection();
```
