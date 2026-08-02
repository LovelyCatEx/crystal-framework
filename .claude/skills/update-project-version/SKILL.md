---
name: update-project-version
description: 发版 / 升级版本号时使用。一次发版必须联动修改 4 处：pom.xml 的 revision、GlobalConstants.APP_VERSION、所有语言 index.md 的 tagline、以及中英双语 change-logs.md。changelog 内容必须依据 git log 汇总并中英同步，不是只填版本号。
---

# 修改项目版本号（发版）

## 触发条件

当用户要求发版、升级版本、写 release / changelog 时使用。**只要涉及版本变更，就必须按本 Skill 联动修改下述全部 4 处文件，禁止只改其中一处。**

## 需要修改的文件（4 处，缺一不可）

下表是一次发版的完整清单，动手前先逐项确认：

| # | 文件 | 改什么 |
|---|---|---|
| 1 | `pom.xml` | `<revision>` |
| 2 | `crystal-shared/.../shared/constants/GlobalConstants.kt` | `APP_VERSION` |
| 3 | `docs/index.md` + `docs/<lang>/index.md`（如 `docs/en/index.md`） | `tagline: vX.Y.Z` |
| 4 | `docs/change-logs.md` + `docs/<lang>/change-logs.md`（如 `docs/en/change-logs.md`） | 新增版本块，内容依据 git log |

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

### 3. docs 各语言文档首页 index.md

**文件：** 中文首页在**根目录** `docs/index.md`（不是 `docs/zh-CN/index.md`），其他语言在各自子目录，如 `docs/en/index.md`。二者都要改。

版本号写在 frontmatter 的 `tagline` 字段：

```yaml
tagline: vX.Y.Z
```

用 `find docs -maxdepth 2 -name index.md` 扫全部 index.md，逐个把 `tagline` 里的版本号替换为新版本。新增语言时会新增对应 `index.md`，同步更新即可。

### 4. change-logs.md — 更新日志（中英双语，必须同步）

**文件：** 中文 `docs/change-logs.md` + 每个语言子目录一份，如 `docs/en/change-logs.md`。**两份都要改，且条目一一对应**（遵守 CLAUDE.md「文档修改必须跨语言同步，条目数量一一对应，禁止翻译中额外添加原文没有的细节」）。

在文件的 `## 公告` 块之后、上一版本块之前，**按时间倒序**插入新版本块，末尾以 `---` 分隔。

#### changelog 内容怎么来（重点，禁止编造）

内容**必须**来自实际提交，用 `git log --oneline <上一个 tag>..HEAD` 获取（如 `git log --oneline v1.13.2..HEAD`），再按语义前缀归类：

- `feat(...)` → **新功能 / Features**
- `fix(...)` → **Bug 修复 / Bug Fixes**
- `refactor / chore / style / docs(...)` → **其他 / Others**

规则：

- **禁止编造** git log 里没有的条目；禁止照抄本文档示例的 `- ...` 占位内容。
- 每条**简洁一句话**描述该提交做了什么，去掉琐碎细节。
- 语义紧密相关的多个提交可**合并为一条**（如同一功能的多次迭代）。
- 中英两份的条目数量与顺序必须完全对应，英文是中文的等价翻译，不多写不少写。
- 空的分节（如本次没有 Bug 修复）直接省略该 `###` 小标题。
- 版本日期取最新提交日期（`git log -1 --format=%ci HEAD`），格式 `YYYY-MM-DD`。

#### 格式模板

中文 `docs/change-logs.md`：

```markdown
## vX.Y.Z

YYYY-MM-DD

### 新功能
+ feat(模块): 一句话描述。

### Bug 修复
+ fix(模块): 一句话描述。

### 其他
+ refactor(模块): 一句话描述。

---
```

英文 `docs/en/change-logs.md`（分节标题为 `Features` / `Bug Fixes` / `Others`，条目与中文一一对应）：

```markdown
## vX.Y.Z

YYYY-MM-DD

### Features
+ feat(module): One-line description.

### Bug Fixes
+ fix(module): One-line description.

### Others
+ refactor(module): One-line description.

---
```

> 列表符号沿用文件里既有版本块的写法（本项目用 `+`），保持一致即可。

## 完成后自检

- [ ] 4 处文件全部改到，`pom.xml` / `APP_VERSION` / 所有 `index.md` tagline 版本号完全一致。
- [ ] 中英两份 changelog 都已新增版本块，条目数量、顺序一一对应。
- [ ] changelog 每条都能在 `git log` 里找到对应提交，无编造。
- [ ] 只用了只读 git 命令（log / tag），未执行任何 git 写操作（commit / push / tag 创建等由用户自行执行）。
