---
name: add-mail-templates
description: 基于 mail 模块的 MailModuleConfigure 配置机制，为业务模块添加自定义邮件模板分类、模板类型和默认模板时使用。
---

# 添加自定义邮件模板

## 触发条件
当用户明确表示需要新增一套邮件模板、邮件模板分类、邮件模板类型，或要求“模仿 mail 模块/tenant 模块注册邮件模板”时使用本 skill。

典型描述：
- 添加一个订单支付成功邮件模板。
- 给某业务模块新增邮件通知模板。
- 注册一套自定义邮件模板分类、模板类型和默认模板。
- 让邮件可以通过 `mailService.sendMailByType(...)` 使用新的模板类型发送。

## 输入格式
用户应尽量提供以下信息；如果缺失，可根据业务语义采用合理默认值并在输出中说明：
1. 业务模块/包名：例如 `order`、`tenant`、`user`。
2. 模板分类：分类 `name` 与 `description`，例如 `order` / `Order mail templates`。
3. 模板类型：类型 `name`、`description`、变量列表、是否允许多模板，例如 `order_paid_notice`、变量 `order_no, amount, paid_time`、`allowMultiple=false`。
4. 默认模板：模板 `name`、`description`、邮件标题 `title`、HTML 内容 `content`、是否启用 `active`。
5. 如果需要可覆盖默认模板：是否添加类似 `TenantMailTemplateConfigure` 的可覆盖配置接口与 `@ConditionalOnMissingBean` 默认实现。

## 前提信息

### MailModuleConfigure

mail 模块通过 Spring Bean 自动收集所有 `MailModuleConfigure` 实现，并在启动时按如下顺序注册到数据库：
1. `configureTemplateCategory(categories)`：注册邮件模板分类。
2. `configureTemplateType(categories, templateTypes)`：注册邮件模板类型。
3. `configureTemplate(categories, templateTypes, templates)`：注册具体邮件模板。

`MailModuleAutoConfigure` 会读取所有 `MailModuleConfigure` Bean，按 `OrderComparator.INSTANCE` 排序后执行上述三个阶段，然后：
- 如果数据库中不存在同名分类，则创建分类；存在则跳过。
- 如果数据库中不存在同名模板类型，则创建类型；存在则跳过。
- 如果数据库中不存在同名模板，则通过 `MailTemplateManagerService.create(...)` 创建模板；存在则跳过。

因此新增模板通常不需要写 migration，只需要添加配置类与声明对象。

### 声明类参数

#### MailTemplateCategoryDeclaration
1. `name: String`：分类唯一名称，建议小写下划线，例如 `tenant`、`order`。
2. `description: String?`：分类描述，可为 null。

#### MailTemplateTypeDeclaration
1. `name: String`：模板类型唯一名称，建议以分类/业务前缀开头，例如 `tenant_member_join_review`、`order_paid_notice`。
2. `description: String?`：模板类型描述，可为 null。
3. `variables: Array<String>`：模板允许/约定使用的变量名列表，必须与模板内容中的占位符一一对应。
4. `allowMultiple: Boolean`：是否允许该类型下存在多个可用模板。
   - `false`：通常用于固定业务模板。
   - `true`：可用于同类型随机选择多个 active 模板的场景。
5. `categoryDeclaration: MailTemplateCategoryDeclaration`：所属分类声明，必须使用同一个声明对象，不要临时 new 一个同名对象。

#### MailTemplateDeclaration
1. `name: String`：模板唯一名称，建议 `default_${typeName}`，例如 `default_order_paid_notice`。
2. `description: String?`：模板描述，可为 null。
3. `title: String`：邮件标题，可以使用 `{{变量名}}` 占位符。
4. `content: String`：邮件 HTML 内容，可以使用 `{{变量名}}` 占位符。
5. `active: Boolean`：是否启用。
6. `type: MailTemplateTypeDeclaration`：所属模板类型，必须引用已注册的类型声明。

### 参数/占位符规则（特别注意）

1. 模板变量名统一写在 `MailTemplateTypeDeclaration.variables` 中，例如：
   ```kotlin
   variables = arrayOf(VARIABLE_ORDER_NO, VARIABLE_AMOUNT)
   ```
2. 模板标题和内容中的占位符格式必须是双大括号：`{{变量名}}`，例如 `{{order_no}}`。
3. `MailServiceImpl.resolvePlaceholders(...)` 只会替换调用方传入的 `placeholders: Map<String, String?>` 中存在的 key：
   ```kotlin
   r = r.replace("{{$k}}", v ?: "null")
   ```
4. 占位符 key 必须与 `variables` 中的变量名完全一致，包括大小写和下划线。
5. 如果传入值为 null，会被替换成字符串 `"null"`。
6. 如果模板里写了 `{{xxx}}` 但发送时没有传入 `xxx`，该占位符会原样保留，不会自动报错。
7. 发送时推荐使用常量作为 Map 的 key，避免手写字符串：
   ```kotlin
   mailService.sendMailByType(
       to = email,
       templateTypeName = OrderMailDeclaration.orderPaidNoticeTemplateType.name,
       placeholders = mapOf(
           OrderMailDeclaration.VARIABLE_ORDER_NO to orderNo,
           OrderMailDeclaration.VARIABLE_AMOUNT to amount.toString()
       )
   )
   ```
8. 如果一个模板类型的 `allowMultiple=true`，`getAvailableTemplateByTypeName(...)` 会从该类型下所有 `active=true` 的模板中随机选择一个。

## 推荐文件结构

以新增 `order` 模块邮件模板为例，推荐新增/修改：

1. `.../order/constants/OrderMailDeclaration.kt`
   - 定义变量常量。
   - 定义分类声明。
   - 定义模板类型声明。
   - 定义默认模板声明。

2. `.../order/config/OrderMailTemplateConfigure.kt`（可选但推荐）
   - 定义可覆盖默认模板的接口。

3. `.../order/config/DefaultOrderMailTemplateConfigure.kt`（可选但推荐）
   - 返回默认模板声明。

4. `.../order/config/OrderMailTemplateAutoConfigure.kt`（可选但推荐）
   - 使用 `@ConditionalOnMissingBean(OrderMailTemplateConfigure::class)` 提供默认 Bean。

5. `.../order/config/OrderMailModuleConfigure.kt`
   - 实现 `MailModuleConfigure`。
   - 注册分类、模板类型和模板。

如果只是简单添加一套模板，也可以只写 `Declaration` + `MailModuleConfigure`，但推荐保持 tenant 模块同款结构，便于业务方覆盖默认标题和内容。

## 执行步骤

### 1. 阅读并检查现有后端代码
1. 阅读 `crystal-mail/src/main/kotlin/.../mail/config/MailModuleConfigure.kt`。
2. 阅读 `crystal-mail/src/main/kotlin/.../mail/config/MailModuleAutoConfigure.kt`，确认注册流程。
3. 阅读 `crystal-mail/src/main/kotlin/.../mail/types/*Declaration.kt`，确认参数。
4. 阅读已有示例：
   - 系统模板：`SystemMailDeclaration`、`DefaultMailModuleConfigure`、`SystemMailTemplateConfigure`。
   - 业务模板：`TenantMailDeclaration`、`TenantMailModuleConfigure`、`TenantMailTemplateConfigure`。
5. 搜索目标模块中是否已有同名 category/type/template，若有重复，必须停止并告知用户，禁止覆盖已有定义。

### 2. 添加 Declaration
在目标模块的 `constants` 包下新增或扩展 `XxxMailDeclaration`：

```kotlin
object OrderMailDeclaration {
    const val VARIABLE_ORDER_NO = "order_no"
    const val VARIABLE_AMOUNT = "amount"
    const val VARIABLE_PAID_TIME = "paid_time"

    val orderCategory = MailTemplateCategoryDeclaration(
        name = "order",
        description = "Order mail templates",
    )

    val orderPaidNoticeTemplateType = MailTemplateTypeDeclaration(
        name = "order_paid_notice",
        description = "Notification after an order is paid",
        variables = arrayOf(
            VARIABLE_ORDER_NO,
            VARIABLE_AMOUNT,
            VARIABLE_PAID_TIME,
        ),
        categoryDeclaration = orderCategory,
        allowMultiple = false,
    )

    val defaultOrderPaidNoticeTemplate = MailTemplateDeclaration(
        name = "default_order_paid_notice",
        description = "Default notification after an order is paid",
        title = "Order {{order_no}} Paid Successfully",
        content = """
            <!DOCTYPE html>
            <html lang="en">
            <body>
                <p>Your order {{order_no}} has been paid successfully.</p>
                <p>Amount: {{amount}}</p>
                <p>Paid time: {{paid_time}}</p>
            </body>
            </html>
        """.trimIndent(),
        active = true,
        type = orderPaidNoticeTemplateType,
    )
}
```

要求：
- 变量常量命名使用 `VARIABLE_XXX`。
- `variables` 中声明的变量必须在 `title/content` 中按 `{{xxx}}` 使用；如果变量只供调用方未来使用，也要在输出中说明。
- `name` 必须全局唯一，避免和系统/其他模块冲突。
- 默认模板名称推荐使用 `default_${templateTypeName}`。

### 3. 添加可覆盖模板配置接口（推荐）

```kotlin
interface OrderMailTemplateConfigure {
    fun configureOrderPaidNotice(): MailTemplateDeclaration
}
```

默认实现：

```kotlin
class DefaultOrderMailTemplateConfigure : OrderMailTemplateConfigure {
    override fun configureOrderPaidNotice(): MailTemplateDeclaration {
        return OrderMailDeclaration.defaultOrderPaidNoticeTemplate
    }
}
```

自动配置：

```kotlin
@Configuration
class OrderMailTemplateAutoConfigure {
    @Bean
    @ConditionalOnMissingBean(OrderMailTemplateConfigure::class)
    fun orderMailTemplateConfigure(): OrderMailTemplateConfigure {
        return DefaultOrderMailTemplateConfigure()
    }
}
```

要求：
- 只有当业务方确实需要替换默认模板标题/内容/active 等字段时，才暴露此接口。
- 配置接口返回的模板会在后续 `MailModuleConfigure` 中被强制修正 `name` 和 `type`，避免用户覆盖后破坏模板唯一标识和归属类型。

### 4. 添加 MailModuleConfigure 实现

```kotlin
@Configuration
class OrderMailModuleConfigure(
    private val orderMailTemplateConfigure: OrderMailTemplateConfigure,
) : MailModuleConfigure {
    override fun configureTemplateCategory(categories: MutableList<MailTemplateCategoryDeclaration>) {
        categories.add(OrderMailDeclaration.orderCategory)
    }

    override fun configureTemplateType(
        categories: Map<String, MailTemplateCategoryDeclaration>,
        templateTypes: MutableList<MailTemplateTypeDeclaration>,
    ) {
        templateTypes.add(OrderMailDeclaration.orderPaidNoticeTemplateType)
    }

    override fun configureTemplate(
        categories: Map<String, MailTemplateCategoryDeclaration>,
        templateTypes: List<MailTemplateTypeDeclaration>,
        templates: MutableList<MailTemplateDeclaration>,
    ) {
        val preProcessMailTemplateDeclaration = fun(
            declaration: MailTemplateDeclaration,
            name: String,
            type: MailTemplateTypeDeclaration,
        ): MailTemplateDeclaration {
            return declaration.copy(
                name = name,
                type = type,
            )
        }

        templates.add(
            preProcessMailTemplateDeclaration.invoke(
                orderMailTemplateConfigure.configureOrderPaidNotice(),
                OrderMailDeclaration.defaultOrderPaidNoticeTemplate.name,
                OrderMailDeclaration.defaultOrderPaidNoticeTemplate.type,
            )
        )
    }
}
```

要求：
- `@Configuration` 必须加在 `MailModuleConfigure` 实现类上，确保 Spring 能发现。
- `configureTemplateCategory` 注册分类。
- `configureTemplateType` 注册类型。
- `configureTemplate` 注册默认模板。
- 如果不需要可覆盖接口，则直接把 `OrderMailDeclaration.defaultOrderPaidNoticeTemplate` 加入 `templates`。
- 不要在 `configureTemplateType` 中使用 `categories["order"]!!` 新建类型，优先使用 Declaration 中已经绑定的同一个 category 对象。

### 5. 添加发送调用（如用户要求）
使用模板类型发送：

```kotlin
mailService.sendMailByType(
    to = email,
    templateTypeName = OrderMailDeclaration.orderPaidNoticeTemplateType.name,
    placeholders = mapOf(
        OrderMailDeclaration.VARIABLE_ORDER_NO to orderNo,
        OrderMailDeclaration.VARIABLE_AMOUNT to amount.toString(),
        OrderMailDeclaration.VARIABLE_PAID_TIME to paidTime.toString(),
    ),
)
```

或使用模板名称发送：

```kotlin
mailService.sendMail(
    to = email,
    templateName = OrderMailDeclaration.defaultOrderPaidNoticeTemplate.name,
    placeholders = mapOf(...),
)
```

优先使用 `sendMailByType`，这样管理员替换该类型下 active 模板时业务代码不需要变更。

### 6. 验证
1. 编译目标模块或整个后端，确认 import 与 Bean 注入无误。
2. 启动应用后观察日志：
   - `Checking mail template categories...`
   - `Checking mail template types...`
   - `Checking mail templates...`
3. 确认日志中新增项显示 `* name`，已存在项显示 `√ name`。
4. 如添加了发送调用，检查 `placeholders` 是否覆盖模板内全部 `{{变量名}}`。

## 输出格式
完成后向用户说明：
1. 新增/复用的邮件模板分类：`name`、`description`。
2. 新增的模板类型：`name`、`description`、`variables`、`allowMultiple`。
3. 新增的默认模板：`name`、`title`、`active`。
4. 是否添加了可覆盖配置接口及默认实现。
5. 如果添加发送调用，列出 `templateTypeName` 和传入的 `placeholders` key。
6. 说明已执行的验证命令或未执行的原因。

## 常见错误
1. 模板中使用 `{{orderNo}}`，但 variables/placeholders 中写 `order_no`，导致无法替换。
2. 忘记添加 `@Configuration`，导致 `MailModuleConfigure` 不生效。
3. 只添加模板，忘记注册分类或模板类型。
4. `MailTemplateTypeDeclaration.categoryDeclaration` 使用了同名但不同对象，导致 `MailModuleAutoConfigure` 映射不到实体。
5. 默认模板覆盖接口允许外部改掉 `name/type`，但没有在 `MailModuleConfigure` 中 copy 回固定值。
6. `allowMultiple=true` 时以为会固定选择某模板；实际上可用模板会随机选择。
7. 发送时传入 null，邮件中会显示字符串 `null`，不是空字符串。
