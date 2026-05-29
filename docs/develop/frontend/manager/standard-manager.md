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

::: tip 底层组件
`ManagerPageContainer` 的数据表格、过滤、搜索等功能均由 [实体数据表格](/develop/frontend/components/entity-table) 组件提供。如需脱离 `ManagerPageContainer` 单独使用表格，或查看完整参数列表，请阅读该组件文档。
:::

## 使用全局搜索

传入 `searchKeywords` 即可在过滤栏显示一个全局搜索框，输入关键词后按回车触发查询。`searchKeywords` 中列出的字段会以 **OR** 方式组合——只要任一字段包含关键词即匹配。

```tsx
<ManagerPageContainer
    searchKeywords={['username', 'email', 'nickname']}
    // ...
/>
```

效果：用户在搜索框输入 `alice` 按回车 → 查询 `username`、`email`、`nickname` 任一字段包含 `alice` 的记录，表格回第 1 页，URL 同步 `?searchKeyword=alice`。

::: tip 与高级筛选的组合
同时配置 `searchKeywords` 和 `filterableFields` 时，过滤栏会自动出现 AND/OR 切换开关，控制全局搜索与高级筛选条件的组合逻辑。
:::

---

## 添加过滤条件

在管理页面中，通常会提供下拉框、输入框等控件让用户筛选数据。框架通过 `useManagerQueryParams` 统一管理这些过滤状态，并且自动同步到浏览器 URL（`?status=active&userId=123`），这样用户刷新页面或分享链接后筛选条件不会丢失。

整个流程分四步：

1. **声明 schema** — 定义有哪些过滤条件、每个是什么类型
2. **用 filters 读值** — 在控件的 `defaultValue` / `value` 中读取当前过滤值
3. **用 setFilter 写值** — 在控件的 `onChange` / `onPressEnter` 中更新过滤值
4. **同步到 URL** — 把 `syncToUrl` 和 `initialQueryValues` 传给 `ManagerPageContainer`

### 第一步：声明 schema

```tsx
import { useManagerQueryParams } from '@/compositions/use-manager-query-params.ts';

const { filters, setFilter, syncToUrl, initialQueryValues } = useManagerQueryParams({
    schema: {
        status: 'string',   // 状态，字符串类型
        userId: 'string',   // 用户 ID，字符串类型
        type: 'number',     // 类型，数字类型
    }
});
```

`schema` 中的每个 key 就是一个过滤条件。`'string'` 表示文本值，`'number'` 会从 URL 读取时自动调用 `Number()` 转为数字，`'boolean'` 识别 `'true'` / `'false'`。

效果：声明后，`filters` 对象中就有了 `.status`、`.userId`、`.type` 三个字段，初始值都是 `undefined`（表示不过滤）。

### 第二步：用 filters 读取当前值

`filters` 是一个响应式对象，包含每个过滤条件的当前值。当用户操作控件 → `setFilter` 更新后 → `filters` 立即变化。

```tsx
// filters.status 当前是 'active'、'disabled' 或 undefined（全部）
console.log(filters.status);  // => 'active'

// filters.userId 当前是用户输入的文本，或 undefined（未输入）
console.log(filters.userId);  // => '123'
```

### 第三步：用 setFilter 更新值

`setFilter(key, value)` 更新某个过滤字段。**value 传 `undefined` 表示清除该条件（不过滤）。**

```tsx
// 设置 status 为 'active'
setFilter('status', 'active');

// 清除 status 条件（回到"全部"）
setFilter('status', undefined);

// 设置 userId 为输入框的值，空字符串视为清除
setFilter('userId', e.target.value || undefined);
```

典型用法：Select 选择"全部"时传 `undefined`，Input 清空时传 `undefined`。

### 第四步：同步到 URL 并触发刷新

```tsx
// 1. 把 syncToUrl / initialQueryValues 传给容器
<ManagerPageContainer
    queryParamsSync={syncToUrl}
    initialQueryValues={initialQueryValues}
    simpleFilters={[
        // 将 filters 中的值映射为查询条件，传给后端
        { field: 'status',  value: filters.status },
        { field: 'user_id', value: filters.userId ? Number(filters.userId) : undefined },
        { field: 'type',    value: filters.type },
    ]}
    // ...
/>

// 2. 监听 filters 变化，手动触发刷新
useEffect(() => {
    pageRef.current?.refreshData({ resetPage: true });
}, [filters.status, filters.userId, filters.type]);
```

效果：过滤条件变化后 URL 自动变为 `?status=active&userId=123`，表格刷新到第 1 页。分享该 URL 或刷新页面，过滤条件不丢失。

### 完整示例

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
            <ActionBarComponent title="实体管理" subtitle="管理所有实体" />
            <ManagerPageContainer
                ref={pageRef}
                entityName="实体"
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

**参数** — `schema` 字段类型：

| 类型值 | TypeScript 类型 | 说明 |
|--------|----------------|------|
| `'string'` | `string` | 文本，原样读写 |
| `'number'` | `number` | 数字，从 URL 读取时自动 `Number()` 转换 |
| `'boolean'` | `boolean` | 布尔，从 URL 读取时识别 `'true'` / `'false'` |

**返回值**：

| 返回值 | 类型 | 说明 |
|--------|------|------|
| `filters` | `{ [K in keyof S]: S[K] \| undefined }` | 当前过滤值对象，字段名与 schema 一致，值类型由 schema 决定。所有字段默认为 `undefined` |
| `setFilter(key, value)` | `(key: string, value: any \| undefined) => void` | 更新某个过滤字段。`key` 必须是 schema 中声明的字段名。`value` 传 `undefined` 清除条件 |
| `syncToUrl` | `(params: object) => void` | 传给 `ManagerPageContainer` 的 `queryParamsSync`，查询后自动同步到 URL |
| `initialQueryValues` | `object` | 传给 `ManagerPageContainer` 的 `initialQueryValues`，页面加载时从 URL 恢复状态 |
| `getInitialParam(key)` | `(key: string) => string` | 读取 URL 中某个字段的初始字符串值（不经过类型转换） |

---

## 高级条件搜索

`ManagerPageContainer` 内置了 `FilterBuilder` 高级条件搜索组件。传入 `filterableFields` 即可在过滤栏显示一个可视化筛选器，用户可以自由组合任意字段的 AND/OR 条件，支持多种运算符（等于、不等于、包含、模糊匹配等）。

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

当同时配置了 `searchKeywords`（全局搜索）和 `filterableFields`（高级筛选）时，过滤栏会自动显示一个 AND/OR 切换开关，用于控制全局搜索条件与高级筛选条件之间的组合逻辑。

### FilterableField 字段类型与运算符

| `type` | 可用运算符 |
|--------|-----------|
| `text` | eq、ne、contains、like、not_contains、is_null、is_not_null |
| `number` | eq、ne、gt、gte、lt、lte、is_null、is_not_null |
| `select` | eq、ne、is_null、is_not_null |
| `date` | eq、ne、gt、gte、lt、lte、is_null、is_not_null |
| `dateTime` | eq、ne、gt、gte、lt、lte、is_null、is_not_null |

### 运算符含义

| 运算符 | 含义 | 说明 |
|--------|------|------|
| `eq` | 等于 | 精确匹配 |
| `ne` | 不等于 | 排除匹配 |
| `gt` | 大于 | 值大于指定值 |
| `gte` | 大于等于 | 值大于或等于指定值 |
| `lt` | 小于 | 值小于指定值 |
| `lte` | 小于等于 | 值小于或等于指定值 |
| `contains` | 包含 | 字符串包含子串 |
| `not_contains` | 不包含 | 字符串不包含子串 |
| `like` | 模糊匹配 | SQL LIKE 语法，支持 `%` 通配符 |
| `is_null` | 为空 | 字段值为 NULL |
| `is_not_null` | 非空 | 字段值不为 NULL |

`FilterableField` 结构：

```ts
interface FilterableField {
    field: string;                // 数据字段名
    label: string;                // 字段显示名称
    type: 'text' | 'number' | 'select' | 'date' | 'dateTime';
    options?: { label: string; value: string | number }[];  // 仅 select 类型，下拉选项
    renderValue?: (ctx: {        // 自定义值输入控件
        value: unknown;
        onChange: (v: unknown) => void;
        operator: string | null;
    }) => ReactNode;
}
```

::: tip
内置的 `created_time` 和 `modified_time` 会自动追加到可用字段中，无需手动添加。
:::

---

## 参数说明

### 基础展示参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `entityName` | `string` | — | 实体名称，用于弹窗标题、操作提示等文案 |
| `title` | `string` | — | 内置 ActionBar 的标题（`showActionBar` 为 `true` 时生效） |
| `subtitle` | `string` | — | 内置 ActionBar 的副标题 |
| `columns` | `EntityTableColumns<ENTITY>` | — | 表格列定义，通常由 `useXxxColumns()` hook 返回 |

---

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

### 显示控制参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `showActionBar` | `boolean` | `true` | 是否显示组件内置的 ActionBar（含新增按钮）。通常设为 `false`，在页面外层自行放置 `ActionBarComponent` |
| `readonlyMode` | `boolean` | `false` | 只读模式，隐藏新增按钮、行内编辑/删除按钮，并禁用批量操作选择框 |
| `showRowActions` | `boolean` | `true` | 是否显示每行的编辑/删除操作按钮 |
| `showTimeRangeFilter` | `boolean` | `true` | 是否在过滤栏显示内置的时间范围选择器，详见 [过滤栏参数](#过滤栏参数) |
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
| `queryParamsSync` | `(params: Record<string, unknown>) => void` | 每次查询后的回调，用于将当前查询参数同步到 URL。传入 `useManagerQueryParams()` 返回的 `syncToUrl` 即可 |
| `initialQueryValues` | `object` | 初始查询值，用于从 URL 恢复分页、搜索关键词和时间范围。传入 `useManagerQueryParams()` 返回的 `initialQueryValues` 即可 |

---

### 过滤栏参数

| 参数 | 类型 | 说明 |
|------|------|------|
| `tableActions` | `TableAction[]` | 过滤栏操作项，渲染在搜索框之后。每项包含 `label`（标签）、`children`（控件）。非只读模式下，内置的批量操作选择器会自动插入到最前面 |
| `tablePrefixActions` | `TableAction[]` | 过滤栏操作项，渲染在搜索框之前。结构与 `tableActions` 相同 |
| `searchKeywords` | `string[]` | 全局搜索框的搜索字段，按 OR 组合匹配。不传则不显示搜索框 |
| `simpleFilters` | `SimpleFilter[]` | 行内过滤条件，每个字段的过滤值会 AND 组合到查询中。配合 `useManagerQueryParams` 的 `filters` 使用 |
| `filterableFields` | `FilterableField[]` | 高级筛选器（FilterBuilder）的可用字段。不传则不显示 FilterBuilder |
| `showTimeRangeFilter` | `boolean` | 是否显示内置的时间范围选择器（createdTime 范围），默认 `true` |

`TableAction` 结构：

```ts
{
    label: ReactNode;           // 控件上方的标签文字
    children: ReactNode;        // 实际的控件（Input、Select 等）
}
```

`SimpleFilter` 结构：

```ts
{
    field: string;              // 查询字段名
    operator?: string;          // 比较运算符，默认 "contains"
    value: unknown;             // 过滤值，undefined/null/'' 时自动忽略
    urlKey?: string;            // URL 参数名，默认与 field 相同
}
```

::: tip SimpleFilter 要点
- 所有 `simpleFilters` 之间是 **AND** 关系，全部满足才匹配。
- `operator` 默认 `"contains"`（字符串模糊匹配），数值/枚举类建议显式传 `"eq"`。
- `value` 为 `undefined`、`null` 或空字符串时，该条自动跳过不参与查询。
- `urlKey` 用于 URL 同步：如果不传，URL 参数名等于 `field`；传入后 URL 用 `urlKey`，请求仍用 `field`。适合字段名与前端展示名不一致的场景（如 `field: 'user_id'`，`urlKey: 'userId'`）。
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

::: tip 国际化 Key 命名
页面标题使用 `pages.{页面名}.title` / `.subtitle`，实体名称使用 `entityNames.{实体名}`，过滤条件标签使用 `pages.{页面名}.filter.{字段}`，高级筛选字段标签使用 `components.filterBuilder.{字段}`。详见[国际化](../i18n)。
:::

::: warning 枚举类型字段
当过滤栏或表格列涉及后端枚举类型时，**严禁**使用分散的 `t('components.columns.xxx.typeXxx')` 键或硬编码数字。必须按[枚举翻译四步流程](../i18n#枚举翻译)：

1. `src/types/` 中定义 TypeScript 枚举常量
2. `locales/{locale}.ts` 的 `enums` 命名空间添加翻译
3. `enum-helpers.ts` 注册 `getXxx()` 函数
4. 组件中通过 `getXxxType(EnumType.VALUE)` 获取标签

```tsx
// ❌ 错误
options={[
    { value: 0, label: t('components.columns.xxx.typeBoolean') },
    { value: 1, label: t('components.columns.xxx.typeLimit') },
]}

// ✅ 正确
import { MyType } from "@/types/xxx.types.ts";
import { getMyType } from "@/i18n/enum-helpers.ts";

options={[
    { value: MyType.BOOLEAN, label: getMyType(MyType.BOOLEAN) },
    { value: MyType.LIMIT, label: getMyType(MyType.LIMIT) },
]}
```
:::
