---
name: add-app-config
description: 为后端添加 application.yaml 配置项（crystalframework.* 命名空间），包括 CrystalFrameworkConfiguration 嵌套类、yaml 默认值，以及在业务代码中的注入读取。
---

# 添加后端应用配置

## 触发条件

当用户需要在 `application.yaml` 中加入可在代码中读取的、由开发者维护的静态配置时使用。例如：

- 邮件、消息渠道等模块的固定文本/参数（subject、content、超时、重试次数）
- JWT 过期时间、雪花 ID 段长等基础设施参数
- 文件资源的 supported-content-types 等白名单

> 注意：可由系统管理员在管理后台动态修改的配置项**禁止**走此路径，必须使用 `add-system-settings` skill（写入数据库的 `system_settings` 表）。两者职责严格区分。

## 输入格式

用户需要提供：

1. 配置项的命名空间（必须以 `crystalframework.` 开头，例如 `crystalframework.test.smtp`）
2. 各字段名、类型、默认值
3. 用途说明（可选，写在 Java 类的 javadoc 上）
4. 在哪个 Controller / Service / Config 中读取

## 前提信息

### 唯一入口：CrystalFrameworkConfiguration

**所有 `crystalframework.*` 配置必须**写入：

```
crystal-shared/src/main/kotlin/com/lovelycatv/crystalframework/shared/config/CrystalFrameworkConfiguration.java
```

注意路径：文件位于 `kotlin` 目录下，但**是 Java 文件**（`.java`），并使用 Spring Boot 的 `@ConfigurationProperties("crystalframework")` 绑定。**禁止新建 Kotlin 版本的 ConfigurationProperties 类**。**禁止**为新配置创建独立的 `@ConfigurationProperties` 类，必须复用此类。

类结构是嵌套静态 inner class，每个一级 key（auth / resource / monitor / sharding / test / ...）对应一个静态嵌套类，二级 key 再嵌套到该类内部。所有字段均使用 Java Bean 模式：private 字段 + getter/setter + 默认值初始化。

### 字段类型映射

| yaml 写法 | Java 字段类型 | 备注 |
|---|---|---|
| `7d` / `30s` / `100ms` | `java.time.Duration` | Spring 自动解析 |
| `30000` | `long` / `int` | 整数 |
| 字符串 | `String` | |
| `- a` / `- b` | `String[]` | 列表 |
| `${ENV:default}` | 任意 | 支持环境变量占位符 |

### 注入与读取

**Kotlin 业务代码**通过构造器注入 `CrystalFrameworkConfiguration` 即可：

```kotlin
@RestController
class FooController(
    private val crystalFrameworkConfiguration: CrystalFrameworkConfiguration,
) {
    suspend fun bar() {
        val subject = crystalFrameworkConfiguration.test.smtp.subject
        // ...
    }
}
```

**禁止**使用 `@Value("\${crystalframework.xxx}")` 散点读取，必须走聚合配置类。

### application.yaml 同步

虽然 Java 类中已写默认值，仍**必须**在 `crystal-starter/src/main/resources/application.yaml` 的 `crystalframework:` 节点下显式写出键值，便于运维和调参。yaml key 用 kebab-case（`flush-interval-ms`），Java 字段用 camelCase（`flushIntervalMs`），由 Spring 自动转换。

## 执行步骤

### 第一步：添加 Java 嵌套类

在 `CrystalFrameworkConfiguration.java` 中：

1. **判断一级 key 是否已存在**。已有：`Auth` / `Resource` / `Monitor` / `Sharding` / `Test`。
   - 已存在 → 直接在对应 inner class 内追加字段或子嵌套类
   - 不存在 → 新增一个 `public static class XXX`，并在外层类添加 `private XXX xxx = new XXX()` + getter/setter
2. 每个字段必须：private + 默认值初始化 + public getter/setter

**示例：新增 `crystalframework.test.smtp.subject` / `content`**

```java
// 外层字段（如 Test 已存在则跳过）
private Test test = new Test();
public Test getTest() { return test; }
public void setTest(Test test) { this.test = test; }

// 嵌套类
public static class Test {
    private SMTP smtp = new SMTP();
    public SMTP getSmtp() { return smtp; }
    public void setSmtp(SMTP smtp) { this.smtp = smtp; }

    public static class SMTP {
        private String subject = "Crystal Framework SMTP Test";
        private String content = "This is a test email...";
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
```

### 第二步：在 application.yaml 中显式声明

在 `crystal-starter/src/main/resources/application.yaml` 的 `crystalframework:` 节点下追加：

```yaml
crystalframework:
  # ... 已有 ...
  test:
    smtp:
      subject: Crystal Framework SMTP Test
      content: This is a test email from Crystal Framework. ...
```

注意：

- yaml key 必须 kebab-case
- 字符串无需引号，除非含特殊字符
- 列表使用 `-` 项

### 第三步：在业务代码中读取

在需要的 Controller / Service / Config 中通过构造器注入 `CrystalFrameworkConfiguration`，再链式访问字段。**严禁**在业务代码中硬编码这些值（违反 CLAUDE.md "禁止魔法值" 规则）。

```kotlin
class XxxService(
    private val crystalFrameworkConfiguration: CrystalFrameworkConfiguration,
) {
    fun doSomething() {
        val cfg = crystalFrameworkConfiguration.test.smtp
        mailService.sendMail(to, cfg.subject, cfg.content)
    }
}
```

### 第四步：验证

执行 `./mvnw compile -pl crystal-starter -am -DskipTests`，确认编译通过且 Spring Boot 启动时不报 `ConfigurationProperties` 绑定错误。

## 决策流程

```
是否需要添加配置？
  │
  ├── 用户/管理员需要在后台动态修改？
  │     └── ❌ 不要用此 skill，改用 add-system-settings
  │
  └── 仅开发者维护、随版本发布？
        │
        ├── 一级 key 是否在 CrystalFrameworkConfiguration 中已存在？
        │     ├── 是 → 在已有 inner class 内追加
        │     └── 否 → 新增 inner class + 外层字段/getter/setter
        │
        ├── 在 application.yaml 显式写出键值
        │
        └── 业务代码通过构造器注入 CrystalFrameworkConfiguration 读取
```

## 常见错误

| 错误 | 修正 |
|---|---|
| 用 Kotlin 新建独立 `@ConfigurationProperties` 类 | 必须改写到 `CrystalFrameworkConfiguration.java` |
| 用 `@Value("\${crystalframework.xxx}")` 散点注入 | 必须用聚合配置类 |
| Java 字段用 camelCase，yaml 也用 camelCase | yaml 必须 kebab-case |
| 仅写 yaml 不写 Java 字段 | 必须两端同步，否则代码读不到 |
| 仅写 Java 字段不写 yaml | 默认值会生效，但运维不可见，必须 yaml 同步显式声明 |
| 把可由后台动态修改的配置写到 yaml | 应使用 `add-system-settings`（数据库存储） |
| 在业务代码硬编码这些值 | 违反"禁止魔法值"，必须从 `CrystalFrameworkConfiguration` 读 |

## 输出格式

完成后向用户说明：

1. 新增/修改的字段路径（如 `crystalframework.test.smtp.subject`）、类型、默认值
2. `CrystalFrameworkConfiguration.java` 中的修改位置（一级 key 是新增还是复用）
3. `application.yaml` 中追加的 yaml 片段
4. 业务代码中读取此配置的位置（文件:行）
