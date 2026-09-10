---
name: i18n-translation
description: 添加或修改前端国际化翻译文本。任何涉及 i18n、国际化、翻译、多语言的操作都必须先阅读本 skill。
---

# 前端国际化翻译规范

## !!!绝对禁止!!!

**!!!绝对禁止修改 `web/src/i18n/i18n-rules.ts` 中的 `I18nRules` 类型定义!!!**
**!!!绝对禁止修改 `web/src/i18n/i18n-rules.ts` 中的 `I18nRules` 类型定义!!!**
**!!!绝对禁止修改 `web/src/i18n/i18n-rules.ts` 中的 `I18nRules` 类型定义!!!**

**!!!违反此规则=立即停止所有工作!!!**

---

## I18nRules 类型定义编写规范

### 谁可以修改 I18nRules

- ✅ **只有项目架构师**在经过仔细设计审查后
- ❌ **绝不是 AI 助手、普通开发者或在功能开发期间**

### 什么可以修改

**1. 添加新的接口定义（如 ManagerPageI18nNode、SettingsManagerI18nNode）**
   - 必须有清晰的用途和文档
   - 必须可在多个页面/组件间复用
   - 示例：`ManagerPageI18nNode` 定义了标准 Manager 页面的结构

**2. 添加新的顶层字段到 I18nRules（极其罕见）**
   - 需要架构级别的理由
   - 不得与现有字段重叠
   - 示例：添加新的主要区域如 "errors" 或 "workflows"

**3. 在 pages 下添加新的特定页面定义（如 systemSettingsManager）**
   - 仅用于具有独特结构要求的页面
   - 大多数页面应通过 `[key: string]: ManagerPageI18nNode | object` 使用 ManagerPageI18nNode

### 绝对禁止的修改

❌ 添加 "common" 或 "shared" 顶层字段  
❌ 修改现有接口定义（ManagerPageI18nNode、SettingsManagerI18nNode）  
❌ 删除或重命名现有顶层字段  
❌ 改变 pages/components/enums/menu/api 的结构  
❌ 添加 pages 和 components 之间的交叉引用  
❌ 使类型更宽松（削弱类型安全性）  

### 核心类型定义

**1. ManagerPageI18nNode**
```typescript
export interface ManagerPageI18nNode {
    title: string;                            // 必须 - 页面标题
    subtitle: string;                         // 必须 - 页面副标题
    modal?: { [key: string]: object };        // 可选 - Modal 表单字段
    filter?: { [key: string]: object };       // 可选 - 表格筛选
    tableActions?: { [key: string]: object }; // 可选 - 表格操作
    action?: { [key: string]: string | object }; // 可选 - 操作按钮
    messages?: { [key: string]: string };     // 可选 - 提示信息
    form?: { [key: string]: object };         // 可选 - 表单
    tabs?: { [key: string]: string };         // 可选 - 标签页
    [key: string]: unknown;                   // 其他页面特定字段
}
```

**用途：** 标准 Manager 页面（XxxManagerPage）

**2. SettingsManagerI18nNode**
```typescript
export interface SettingsManagerI18nNode {
    keys: { [key: string]: string };    // 设置项 keys
    groups: { [key: string]: string };  // 设置分组
    tabs: { [key: string]: string };    // 设置标签页
    enums: { [key: string]: { [value: string]: string } }; // 枚举值
}
```

**用途：** 系统设置/租户设置页面（systemSettingsManager、tenantSettingsManager）

**3. I18nRules.pages 结构**
```typescript
pages: {
    systemSettingsManager: SettingsManagerI18nNode & { [key: string]: unknown };
    tenantSettingsManager: SettingsManagerI18nNode & { [key: string]: unknown };
    [key: string]: ManagerPageI18nNode | object;
}
```

**含义：**
- `systemSettingsManager` 和 `tenantSettingsManager` 使用特殊的 SettingsManagerI18nNode
- 其他所有页面：要么是 `ManagerPageI18nNode`（标准 Manager 页面，强制 title + subtitle），要么是 `object`（特殊页面如 auth、maintenance）

---

## 触发条件

当需要以下任何操作时，**必须**先阅读本 skill：

- 添加任何 i18n 翻译文本
- 修改现有翻译文本
- 在组件中使用 `t('...')` 或 `useTranslation()`
- 创建新的页面、组件、列定义、枚举等需要显示文本的内容
- 添加菜单项
- 添加表格列
- 添加系统设置项或租户设置项

## 唯一合法的工作流程

### 第 1 步：阅读类型定义（强制）

**文件：** `web/src/i18n/i18n-rules.ts`

**必须操作：** 完整阅读 `I18nRules` 接口定义及其注释，确认：
1. 你要添加的翻译属于哪个顶层字段（`pages` / `components` / `enums` / `entityNames` / `menu` / `api`）
2. 该字段下允许的结构是什么
3. 你的翻译路径在类型定义中是否被允许

**核心类型定义：**
- `ManagerPageI18nNode` - 标准 Manager 页面结构（必须有 title + subtitle）
- `SettingsManagerI18nNode` - 系统/租户设置页面结构（keys + groups + tabs + enums）

**如果说不出对应的类型定义位置 = 禁止动手**

### 第 2 步：确认翻译位置

根据使用场景，翻译必须放在以下位置之一：

| 场景 | 位置 | 类型约束 | 示例 |
|------|------|----------|------|
| Manager 页面标题 | `pages.{pageName}.title` | `ManagerPageI18nNode.title` (必须) | `pages.aiProviderManager.title` |
| Manager 页面副标题 | `pages.{pageName}.subtitle` | `ManagerPageI18nNode.subtitle` (必须) | `pages.aiProviderManager.subtitle` |
| 页面 Modal 表单字段 | `pages.{pageName}.modal.{field}.{label\|placeholder\|required}` | `ManagerPageI18nNode.modal` | `pages.aiProviderManager.modal.name.label` |
| 页面表格筛选 | `pages.{pageName}.filter.{field}` | `ManagerPageI18nNode.filter` | `pages.userManager.filter.username` |
| 页面表格操作 | `pages.{pageName}.tableActions.{action}.{label\|placeholder}` | `ManagerPageI18nNode.tableActions` | `pages.aiModelManager.tableActions.idFilter.label` |
| 页面操作按钮 | `pages.{pageName}.action.{actionName}` | `ManagerPageI18nNode.action` | `pages.userManager.action.refreshAuthority` |
| 页面提示信息 | `pages.{pageName}.messages.{messageName}` | `ManagerPageI18nNode.messages` | `pages.userManager.messages.banSuccess` |
| 表格列标题 | `components.columns.{entityName}.{columnName}` | 必须在 components.columns 下 | `components.columns.aiModel.name` |
| 通用组件文本 | `components.{componentName}.xxx` | - | `components.approvalEditor.toolbar.undo` |
| PopCard 内容 | `components.popCard.{cardType}.xxx` | - | `components.popCard.mailTemplateType.yes` |
| 枚举值翻译 | `enums.{enumName}.{VALUE}` | - | `enums.forbiddenReason.MISSING_PERMISSION` |
| 实体名称 | `entityNames.{entityName}` | - | `entityNames.user` |
| 菜单项 | `menu.{pub\|myTenant\|admin}.{menuKey}` | - | `menu.admin.userManager` |
| 菜单分组 | `menu.groups.{groupName}` | - | `menu.groups.rbac` |
| API 错误信息 | `api.{errorCode}` | - | `api.errors.USER_NOT_FOUND` |
| 系统设置 | `pages.systemSettingsManager.{keys\|groups\|tabs\|enums}` | `SettingsManagerI18nNode` | `pages.systemSettingsManager.keys.mail.smtp.host` |
| 租户设置 | `pages.tenantSettingsManager.{keys\|groups\|tabs\|enums}` | `SettingsManagerI18nNode` | `pages.tenantSettingsManager.keys.xxx` |

### 第 3 步：同步修改所有语言文件

**文件：**
- `web/src/i18n/locales/zh-CN.ts`
- `web/src/i18n/locales/en-US.ts`

**强制要求：**
1. **必须同时修改** zh-CN.ts 和 en-US.ts
2. **必须保持完全相同的结构**（字段名、层级、嵌套关系）
3. **条目数量必须一一对应**
4. 只有文本内容不同（中文 vs 英文）

### 第 4 步：验证

```bash
cd web && pnpm tsc --noEmit
```

**必须通过 TypeScript 类型检查，否则不得提交。**

---

## 详细场景说明

### 场景 1：新增 Manager 页面

**何时使用：** 创建新的管理页面（如 XxxManagerPage）

**类型约束：** 必须符合 `ManagerPageI18nNode`

**强制要求：**
- ✅ **必须**包含 `title` (string)
- ✅ **必须**包含 `subtitle` (string)
- ✅ 可选包含 `modal`, `filter`, `tableActions`, `action`, `messages`, `form`, `tabs`

**正确示例：**

```typescript
// zh-CN.ts
pages: {
    newManager: {
        title: '新功能管理',              // ✅ 必须
        subtitle: '管理新功能的配置',      // ✅ 必须
        tableActions: {                   // ✅ 可选
            idFilter: {
                label: 'ID',
                placeholder: '输入 ID'
            }
        },
        modal: {                          // ✅ 可选
            name: {
                label: '名称',
                required: '请输入名称',
                placeholder: '请输入'
            }
        }
    }
}

// en-US.ts
pages: {
    newManager: {
        title: 'New Feature Management',
        subtitle: 'Manage new feature configurations',
        tableActions: {
            idFilter: {
                label: 'ID',
                placeholder: 'Enter ID'
            }
        },
        modal: {
            name: {
                label: 'Name',
                required: 'Please enter name',
                placeholder: 'Enter name'
            }
        }
    }
}
```

**错误示例：**

```typescript
// ❌ 错误：缺少必需的 title 和 subtitle
pages: {
    newManager: {
        modal: {
            name: { label: '名称' }
        }
    }
}

// ❌ 错误：title 和 subtitle 必须是 string，不能是对象
pages: {
    newManager: {
        title: { text: '标题' },  // 错误！
        subtitle: { text: '副标题' }  // 错误！
    }
}
```

---

### 场景 2：页面 Modal 表单字段

**何时使用：** 在页面的 Modal 或 Form 中添加表单字段的 label、placeholder、validation message

**位置：** `pages.{pageName}.modal.{fieldName}.{label|placeholder|required|...}`

**类型约束：** `ManagerPageI18nNode.modal?: { [key: string]: object }`

**正确示例：**

```typescript
// AiProviderManagerPage.tsx
<Form.Item 
    name="name" 
    label={t('pages.aiProviderManager.modal.name.label')}
    rules={[{ required: true, message: t('pages.aiProviderManager.modal.name.required') }]}
>
    <Input placeholder={t('pages.aiProviderManager.modal.name.placeholder')} />
</Form.Item>
```

```typescript
// zh-CN.ts
pages: {
    aiProviderManager: {
        title: 'AI 提供商管理',
        subtitle: '管理 AI 服务提供商配置',
        modal: {
            name: {
                label: '提供商名称',
                required: '请输入提供商名称',
                placeholder: '请输入名称'
            }
        }
    }
}

// en-US.ts
pages: {
    aiProviderManager: {
        title: 'AI Provider Management',
        subtitle: 'Manage AI service provider configurations',
        modal: {
            name: {
                label: 'Provider Name',
                required: 'Please enter provider name',
                placeholder: 'Enter name'
            }
        }
    }
}
```

**错误示例：**

```typescript
// ❌ 错误：使用了不存在的 common 字段
label={t('common.name')}

// ❌ 错误：把 Modal 字段放在 columns 下
label={t('pages.aiProviderManager.columns.name.label')}

// ❌ 错误：只改了 zh-CN.ts，没改 en-US.ts
```

---

### 场景 3：表格列标题

**何时使用：** 在 `components/columns/*Columns.tsx` 中定义 EntityTableColumns 的列标题

**位置：** `components.columns.{entityName}.{columnName}`

**类型约束：** 必须在 `components.columns` 下，**不能**在 `pages.xxx.columns` 下

**正确示例：**

```typescript
// AiModelEntityColumns.tsx
export function useAiModelTableColumns(): EntityTableColumns<AiModelEntity> {
    const { t } = useTranslation();
    
    return [
        {
            title: t('components.columns.aiModel.name'),
            dataIndex: "name",
            key: "name",
        },
        {
            title: t('components.columns.aiModel.providerId'),
            dataIndex: "providerId",
            key: "providerId",
        }
    ];
}
```

```typescript
// zh-CN.ts
components: {
    columns: {
        aiModel: {
            name: '模型名称',
            providerId: '所属提供商',
            maxTokens: '最大 Token 数'
        }
    }
}

// en-US.ts
components: {
    columns: {
        aiModel: {
            name: 'Model Name',
            providerId: 'Provider',
            maxTokens: 'Max Tokens'
        }
    }
}
```

**错误示例：**

```typescript
// ❌ 错误：列标题使用了 pages.xxx.modal
title: t('pages.aiModelManager.modal.name.label')

// ❌ 错误：列标题使用了 pages.xxx.columns
title: t('pages.aiModelManager.columns.name')

// ✅ 正确：列标题必须在 components.columns
title: t('components.columns.aiModel.name')
```

---

### 场景 4：页面表格操作 (tableActions)

**何时使用：** 在 ManagerPageContainer 的 tableActions 中添加筛选器或操作

**位置：** `pages.{pageName}.tableActions.{action}.{label|placeholder}`

**类型约束：** `ManagerPageI18nNode.tableActions?: { [key: string]: object }`

**正确示例：**

```typescript
// AiModelManagerPage.tsx
<ManagerPageContainer
    tableActions={[
        {
            label: <span>{t('pages.aiModelManager.tableActions.idFilter.label')}</span>,
            children: <Input
                placeholder={t('pages.aiModelManager.tableActions.idFilter.placeholder')}
            />,
        },
    ]}
/>
```

```typescript
// zh-CN.ts
pages: {
    aiModelManager: {
        title: 'AI 模型管理',
        subtitle: '管理 AI 模型配置与定价',
        tableActions: {
            idFilter: {
                label: 'ID',
                placeholder: '输入模型 ID'
            }
        }
    }
}

// en-US.ts
pages: {
    aiModelManager: {
        title: 'AI Model Management',
        subtitle: 'Manage AI model configurations and pricing',
        tableActions: {
            idFilter: {
                label: 'ID',
                placeholder: 'Enter model ID'
            }
        }
    }
}
```

**错误示例：**

```typescript
// ❌ 错误：硬编码文本
label: <span>ID</span>
placeholder="Enter model ID"

// ❌ 错误：使用了错误的路径
label: t('pages.aiModelManager.filter.id')  // 应该是 tableActions
```

---

### 场景 5：枚举值翻译

**何时使用：** 需要显示后端枚举类型的用户友好名称（如状态、类型）

**位置：** `enums.{enumName}.{VALUE}`

**同步操作：** 必须在 `web/src/i18n/enum-helpers.ts` 中注册

**正确示例：**

```typescript
// 使用
const statusText = t('enums.forbiddenReason.MISSING_PERMISSION');
```

```typescript
// zh-CN.ts
enums: {
    forbiddenReason: {
        MISSING_PERMISSION: '缺少所需权限',
        SCOPE_MISMATCH: '资源不属于当前范围',
        PROTECTED_RESOURCE: '该资源受保护'
    },
    storageProviderType: {
        1000: 'MinIO 对象存储',
        1001: '阿里云 OSS'
    }
}

// en-US.ts
enums: {
    forbiddenReason: {
        MISSING_PERMISSION: 'Missing permission',
        SCOPE_MISMATCH: 'Resource does not belong to your scope',
        PROTECTED_RESOURCE: 'This resource is protected'
    },
    storageProviderType: {
        1000: 'MinIO Object Storage',
        1001: 'Aliyun OSS'
    }
}
```

```typescript
// enum-helpers.ts 必须注册
export function getForbiddenReason(reason: string): string {
    return translateEnum('forbiddenReason', reason);
}
```

**错误示例：**

```typescript
// ❌ 错误：枚举翻译放在 pages 下
t('pages.aiProviderManager.enums.status.enabled')

// ✅ 正确：枚举翻译必须在顶层 enums 下
t('enums.providerStatus.enabled')
```

---

### 场景 6：菜单项

**何时使用：** 在 `web/src/router/index.tsx` 中添加侧边栏菜单项

**位置：** 
- `menu.pub.{menuKey}` - 公开菜单
- `menu.myTenant.{menuKey}` - 租户成员菜单
- `menu.admin.{menuKey}` - 系统管理员菜单
- `menu.groups.{groupName}` - 菜单分组

**正确示例：**

```typescript
// router/index.tsx
export function getAdminMenus(t: TFunction): RouteItem[] {
    return [
        {
            key: '/manager/ai-provider',
            path: '/manager/ai-provider',
            label: t('menu.admin.aiProviderManager'),
            group: 'ai'
        }
    ];
}

export function getMenuGroups(t: TFunction): MenuGroup[] {
    return [
        { name: 'ai', icon: <RobotOutlined />, label: t('menu.groups.ai') }
    ];
}
```

```typescript
// zh-CN.ts
menu: {
    admin: {
        aiProviderManager: 'AI 提供商管理',
        aiModelManager: 'AI 模型管理'
    },
    groups: {
        ai: 'AI 管理',
        rbac: '权限管理'
    }
}

// en-US.ts
menu: {
    admin: {
        aiProviderManager: 'AI Provider Management',
        aiModelManager: 'AI Model Management'
    },
    groups: {
        ai: 'AI Management',
        rbac: 'RBAC'
    }
}
```

---

### 场景 7：系统设置 / 租户设置

**何时使用：** 使用 `add-system-settings` 或 `add-tenant-settings` skill 添加配置项

**位置：**
- `pages.systemSettingsManager.keys.{keyPath}` - 设置项名称
- `pages.systemSettingsManager.groups.{groupName}` - 设置分组名称
- `pages.systemSettingsManager.tabs.{tabName}` - 设置标签页名称
- `pages.systemSettingsManager.enums.{key}.{value}` - 设置项枚举值

**类型约束：** `SettingsManagerI18nNode`

**同步操作：** 必须在 `web/src/i18n/system-settings.tsx` 或 `tenant-settings.tsx` 中添加映射

**正确示例：**

```typescript
// zh-CN.ts
pages: {
    systemSettingsManager: {
        keys: {
            mail: {
                smtp: {
                    host: 'SMTP 服务器地址',
                    port: 'SMTP 端口'
                }
            }
        },
        groups: {
            mail: {
                smtp: 'SMTP 配置'
            }
        },
        tabs: {
            mail: '邮件设置'
        }
    }
}

// en-US.ts
pages: {
    systemSettingsManager: {
        keys: {
            mail: {
                smtp: {
                    host: 'SMTP Server Host',
                    port: 'SMTP Port'
                }
            }
        },
        groups: {
            mail: {
                smtp: 'SMTP Configuration'
            }
        },
        tabs: {
            mail: 'Mail Settings'
        }
    }
}
```

```typescript
// system-settings.tsx 必须添加映射
export function useSettingsKeyToTranslationMap(): Map<string, string> {
    const { t } = useTranslation();
    return new Map([
        ['mail.smtp.host', t('pages.systemSettingsManager.keys.mail.smtp.host')],
        ['mail.smtp.port', t('pages.systemSettingsManager.keys.mail.smtp.port')],
    ]);
}
```

---

## 绝对禁止的行为

### ❌ 禁止 1：修改 I18nRules 类型定义

```typescript
// ❌ 绝对禁止
export interface I18nRules {
    common: { [key: string]: string };  // 禁止添加新字段
    pages: { ... }
}
```

### ❌ 禁止 2：创建 common 或通用字段

```typescript
// ❌ 错误：创建了 common 字段
common: {
    save: '保存',
    cancel: '取消',
    confirm: '确认'
}

// ✅ 正确：每个页面/组件有自己的完整翻译
pages: {
    aiProviderManager: {
        title: 'AI 提供商管理',
        subtitle: '管理 AI 服务提供商配置',
        action: {
            save: '保存',
            cancel: '取消'
        }
    },
    aiModelManager: {
        title: 'AI 模型管理',
        subtitle: '管理 AI 模型配置与定价',
        action: {
            save: '保存',
            cancel: '取消'
        }
    }
}
```

### ❌ 禁止 3：跨页面/组件复用 i18n 路径

```typescript
// ❌ 错误：在组件中引用 pages.xxx
function MyComponent() {
    return <Button>{t('pages.aiProviderManager.action.save')}</Button>;
}

// ✅ 正确：组件有自己的翻译
function MyComponent() {
    return <Button>{t('components.myComponent.action.save')}</Button>;
}
```

### ❌ 禁止 4：列标题使用 pages 或 modal 路径

```typescript
// ❌ 错误
{
    title: t('pages.aiModelManager.modal.name.label'),
    dataIndex: "name"
}

// ❌ 错误
{
    title: t('pages.aiModelManager.columns.name'),
    dataIndex: "name"
}

// ✅ 正确
{
    title: t('components.columns.aiModel.name'),
    dataIndex: "name"
}
```

### ❌ 禁止 5：只修改一个语言文件

```typescript
// ❌ 错误：只修改了 zh-CN.ts
// zh-CN.ts
pages: {
    newPage: {
        title: '新页面',
        subtitle: '新页面描述'
    }
}

// en-US.ts
// 没有对应的 newPage

// ✅ 正确：两个文件同步修改
```

### ❌ 禁止 6：Manager 页面缺少 title 或 subtitle

```typescript
// ❌ 错误：缺少必需的 title 和 subtitle
pages: {
    newManager: {
        modal: {
            name: { label: '名称' }
        }
    }
}

// ✅ 正确：必须包含 title 和 subtitle
pages: {
    newManager: {
        title: '新功能管理',
        subtitle: '管理新功能配置',
        modal: {
            name: { label: '名称' }
        }
    }
}
```

### ❌ 禁止 7：在不确定位置时猜测或自作主张

```typescript
// ❌ 错误：不确定放哪里，就随便放一个位置
pages: {
    randomPlace: {
        myText: '我的文本'
    }
}

// ✅ 正确：先阅读 i18n-rules.ts，确认正确位置后再添加
```

---

## 常见错误对照表

| 错误写法 | 正确写法 | 说明 |
|---------|---------|------|
| `t('common.name')` | `t('pages.aiProviderManager.modal.name.label')` | 禁止使用 common |
| `t('pages.aiModelManager.modal.name.label')` (在列定义中) | `t('components.columns.aiModel.name')` | 列标题必须在 components.columns |
| `t('pages.aiModelManager.columns.name')` | `t('components.columns.aiModel.name')` | 列标题不在 pages 下 |
| `label: <span>ID</span>` (在 tableActions) | `label: <span>{t('pages.xxx.tableActions.idFilter.label')}</span>` | tableActions 必须使用 i18n |
| `placeholder="Enter ID"` | `placeholder={t('pages.xxx.tableActions.idFilter.placeholder')}` | 所有文本必须 i18n |
| Manager 页面只有 modal | 必须添加 `title` 和 `subtitle` | ManagerPageI18nNode 强制要求 |
| 只改 zh-CN.ts | 同时改 zh-CN.ts 和 en-US.ts | 必须同步 |
| 结构不一致 | 保持完全相同的结构 | 只有文本内容不同 |

---

## 快速检查清单

在添加任何翻译前，必须确认：

- [ ] 已阅读 `i18n-rules.ts` 的类型定义和注释
- [ ] 已确认翻译属于哪个顶层字段（pages/components/enums/entityNames/menu/api）
- [ ] 如果是 Manager 页面，已包含必需的 `title` 和 `subtitle`
- [ ] 如果是列标题，使用的是 `components.columns.xxx` 而不是 `pages.xxx.columns`
- [ ] 如果是 tableActions，使用的是 `pages.xxx.tableActions` 而不是硬编码
- [ ] 已确认具体的翻译路径在类型定义中被允许
- [ ] 已理解该位置的使用场景（Modal 表单？列标题？菜单？枚举？）
- [ ] 已同时修改 zh-CN.ts 和 en-US.ts
- [ ] 两个文件的结构完全一致
- [ ] 如果是枚举，已在 enum-helpers.ts 中注册
- [ ] 如果是系统设置/租户设置，已在对应的 .tsx 文件中添加映射
- [ ] 已运行 `pnpm tsc --noEmit` 验证类型检查通过

---

## 输出格式

完成翻译添加后，向用户说明：

1. 添加的翻译路径（完整的 key）
2. 所属的顶层字段（pages/components/enums/menu 等）
3. 如果是 Manager 页面，确认已包含 title 和 subtitle
4. 修改的文件列表（zh-CN.ts, en-US.ts, 以及其他相关文件）
5. TypeScript 类型检查结果
6. 如果是枚举或设置项，说明是否已完成注册/映射

## 违规后果

**违反 I18n 规范 = 严重违规，必须立即停止所有工作并修正。**

任何不符合本 skill 规范的翻译添加都必须回滚并重新按正确流程操作。
