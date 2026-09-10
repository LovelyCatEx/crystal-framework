---
paths:
  - "web/src/components/**/*.tsx"
  - "web/src/pages/**/*.tsx"
---

# 前端组件规范

## 组件放置规则

组件必须放入 `components` 文件夹：
- 一般情况：单个 tsx 文件直接放入
- 特殊情况：同类型多变种（如 `columns`、`selector`、`card/pop`）必须建文件夹存放

**写组件前先检查是否有类似文件夹，若有必须放入对应位置。**

组件中用到的类型/枚举：
- 需要导出的：在 tsx 文件顶部（import 下方）使用 `export` 导出
- 组件内部的：不需要 export

## 超高频通用组件（优先复用，禁止重复造轮子）

### ManagerPageContainer
`components/ManagerPageContainer.tsx` - 标准 Manager 页面顶层容器

组合了顶栏 + 表格 + 增删改弹窗的全套能力。凡是「列表 + 增删改查」的标准后台页面一律用它。

核心 Props：
- `entityName`: 实体名称（通常传 `t('entityNames.xxx')`）
- `columns`: 表格列定义
- `query` / `create` / `update` / `delete`: CRUD 函数
- `readonlyMode`: 只读模式（隐藏增删改按钮）
- `searchKeywords`: 全局搜索框的搜索字段
- `simpleFilters`: 行内过滤条件
- `filterableFields`: FilterBuilder 可用字段
- `editModalFormChildren`: 新增/编辑弹窗的表单项
- `showTimeRangeFilter`: 是否显示时间范围筛选器（默认 true）
- `hideRecordTimeColumn`: 是否隐藏"记录时间"列（默认 false）

通过 `pageRef.current.refreshData()` 刷新数据。

### EntityTable
`components/table/EntityTable.tsx` - 分页数据表格底座

被 ManagerPageContainer 内部使用。需要表格但不走标准 Manager 套路（如只读展示、嵌入式表格）时单独使用。

提供：
- `query` 分页查询
- 列定义
- 行选择（`tableSelection`，`radio` 单选 / `checkbox` 多选）

### EntitySelector / EntitySelectorModal
`components/selector/` - 以表格弹窗形式选择实体

- `EntityIdSelector`: 单选（`value: string`）
- `EntitySelectorModal type="checkbox"`: 多选

已有专用封装：`UserIdSelector`、`TenantRoleIdSelector` 等，直接复用。

新实体照搬其写法，用 `additionalQueryParams` 注入额外查询条件。

**严禁 Query PageSize = 100 甚至 9999 条，单页最大只允许 20 条数据。**

### ActionBarComponent
`components/ActionBarComponent.tsx` - 页面标题栏

提供 `title` / `subtitle` / `titleActions`（右侧操作区）。

非标准化页面至少要有它作为页头；ManagerPageContainer 已内置。

### CopyableToolTip
`components/CopyableToolTip.tsx` - 带一键复制的文本 Tooltip

展示 ID、长字符串、配置等需要复制的内容时统一用它包裹，columns 中尤其高频。

### StandardCard
`components/card/StandardCard.tsx` - 统一圆角/边框/内边距风格的卡片容器

需要分块承载内容时用它替代裸 `div`/`Card`，保证视觉一致。

## 页面文件命名

**页面文件即使放在子文件夹中，也必须保留完整的模块前缀。**

✅ 正确：`pages/manager/tenant/benefit/TenantTireBenefitValueContainer.tsx`
❌ 错误：`pages/manager/tenant/benefit/Container.tsx`

## Columns 具象化展示

**禁止在表格列中只显示裸 ID。**

凡是关联了其他实体的外键字段（如 `tireTypeId`、`featureId` 等），必须通过异步请求获取关联实体的名称并展示。

参考 `TenantInvitationEntityColumns.tsx` 的 `MemberInfoDisplay` / `DepartmentInfoDisplay` 模式：
1. 在 columns 文件中定义 `XxxDisplay` React 组件
2. 加载中显示 `<Spin size="small" />`
3. 加载失败显示红色 "Unknown" tag
4. 加载完成后显示实体名称 + ID tag（小字号）

❌ 错误：`render: (_, row) => <span>{row.tireTypeId}</span>`

## Columns 文件规范

`components/columns/XxxEntityColumns.tsx` - 每个实体一份表格列定义 Hook（`useXxxTableColumns`）。

外键字段必须用 `XxxDisplay` 子组件异步展示关联实体名称（见上节），禁止裸 ID。

新实体表格必须新建对应 columns 文件。

## PopCard

`components/card/pop/` - 鼠标悬浮/点击展示实体摘要信息的卡片（如 `UserCard`、`TenantDepartmentPopCard`）

内部按 id 异步拉取 profile。在列表/详情中需要快速预览关联实体时复用，新实体照搬现有卡片写法。
