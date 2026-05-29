---
name: update-project-version
description: 更新项目版本号，同步修改 Maven revision、Kotlin 常量、文档版本号，并更新 changelog。
---

# 修改项目版本号

## 触发条件

当用户要求修改项目版本号（如发版、版本升级）时使用。

## 需要修改的文件

### 1. pom.xml — Maven revision 属性

**文件：** `pom.xml`

```xml
<revision>X.Y.Z</revision>
```

找到 `<properties>` 块中的 `<revision>` 标签，修改值为新版本号。所有子模块的 `<version>` 均引用 `${revision}`，只需修改此处一处。

### 2. GlobalConstants.kt — Kotlin 版本常量

**文件：** `crystal-shared/src/main/kotlin/com/lovelycatv/crystalframework/shared/constants/GlobalConstants.kt`

```kotlin
const val APP_VERSION = "X.Y.Z"
```

`APP_VERSION` 用于 API 路径中的 `{version}` 占位符（`/api/{version}/...`），需与 pom.xml 保持一致。

### 3. docs/ 各语言文档首页

**目录：** `docs/*/index.md`（如 `docs/zh-CN/index.md`、`docs/en/index.md` 等）

所有语言子目录下的 `index.md` 中均包含版本号，需要逐个扫描并更新。查找 `version: X.Y.Z` 或正文中出现的版本号并替换为新版本。

新增语言时也会新增对应的 `index.md`，确保同步更新即可。

### 4. docs/change-logs.md — 更新日志

**文件：** `docs/change-logs.md`

按时间倒序插入新版本条目，格式参考已有记录：

```markdown
## vX.Y.Z (YYYY-MM-DD)

### 新功能
- ...

### Bug 修复
- ...

### 其他
- ...

---
```
