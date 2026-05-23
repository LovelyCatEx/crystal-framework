---
name: add-mail-templates
description: 基于 sdk 中 MailModuleConfigure 配置机制，为业务模块添加自定义邮件模板分类、模板类型和默认模板时使用。
---

# 添加自定义邮件模板

## 触发条件
当用户明确表示需要新增一套邮件模板、邮件模板分类、邮件模板类型时使用。

## 输入格式
用户应尽量提供以下信息；如果缺失，根据业务语义采用合理默认值并在输出中说明：
1. 模板分类：分类 `name` 与 `description`。
2. 模板类型：类型 `name`、`description`、变量列表。
3. 默认模板：模板 `name`、`description`、邮件标题 `title`、HTML 内容 `content`。

## 前提信息

### MailModuleConfigure

mail 模块通过 Spring Bean 自动收集所有 `MailModuleConfigure` 实现，并在启动时按如下顺序注册到数据库：
1. `configureTemplateCategory(categories)`：注册邮件模板分类。
2. `configureTemplateType(categories, templateTypes)`：注册邮件模板类型。
3. `configureTemplate(categories, templateTypes, templates)`：注册具体邮件模板。

`MailModuleAutoConfigure` 会读取所有 `MailModuleConfigure` Bean，按 `OrderComparator.INSTANCE` 排序后执行上述三个阶段，然后：
- 如果数据库中不存在同名分类，则创建分类；存在则跳过。
- 如果数据库中不存在同名模板类型，则创建类型；存在则跳过。
- 如果数据库中不存在同名模板，则创建模板；存在则跳过。

### 声明类参数

**MailTemplateCategoryDeclaration**
- `name: String`：分类唯一名称，建议小写下划线，例如 `order`。
- `description: String?`：分类描述。

**MailTemplateTypeDeclaration**
- `name: String`：模板类型唯一名称，建议以分类开头，例如 `order_paid_notice`。
- `description: String?`：类型描述。
- `variables: Array<String>`：模板允许使用的变量名列表。
- `allowMultiple: Boolean`：是否允许该类型下存在多个可用模板。
- `categoryDeclaration: MailTemplateCategoryDeclaration`：所属分类声明，必须使用同一个声明对象。

**MailTemplateDeclaration**
- `name: String`：模板唯一名称，建议 `default_${typeName}`。
- `description: String?`：模板描述。
- `title: String`：邮件标题，可以使用 <code v-pre>{{变量名}}</code> 占位符。
- `content: String`：邮件 HTML 内容，使用 <code v-pre>{{变量名}}</code> 占位符。
- `active: Boolean`：是否启用。
- `type: MailTemplateTypeDeclaration`：所属模板类型。

### 变量/占位符规则
1. 占位符格式：<code v-pre>{{变量名}}</code>
2. 占位符 key 必须与 `variables` 中的变量名完全一致（大小写和下划线）
3. 发送时 placeholders 中不存在的 key 会原样保留

## 执行步骤

### 源码贡献方式（在框架源码内修改）

1. 阅读 `crystal-mail` 中的 `SystemMailDeclaration` 和 `crystal-starter` 中的 `TenantMailDeclaration`，了解现有模板。
2. 检查是否有同名 category/type/template，若有重复则停止并告知用户。
3. 在目标模块的 constants 包下创建 XxxMailDeclaration，定义变量常量、分类、模板类型、默认模板。
4. 创建 `@Configuration` 类实现 `MailModuleConfigure`，注册三个阶段的声明。
5. 如果需要可覆盖默认模板的模式，参照 Tenant 模式添加 `XxxMailTemplateConfigure` 接口 + `@ConditionalOnMissingBean` 默认实现。

### 独立插件方式（外部 JAR 插件）

1. 创建 Declaration 对象（可放在插件任意包下）。
2. 创建 `@Configuration` 类实现 `MailModuleConfigure`，注册三个阶段的声明。
3. 插件打包后放入 `/ext` 目录，框架启动时自动发现。

## 输出格式
完成后说明：
1. 新增的邮件模板分类：`name`、`description`
2. 新增的模板类型：`name`、`description`、`variables`
3. 新增的默认模板：`name`、`title`、`active`
4. 验证方式：启动后观察日志中 `* name` 输出
