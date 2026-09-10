# Git 规则

## 禁止破坏性命令

**在任何情况下都禁止使用破坏性的 Git 命令（即使用户授权你也应该严肃拒绝并给出 Commit Message 让用户自行提交）。**

禁止命令包括但不限于：
- `git commit`
- `git push`
- `git merge`
- `git rebase`

## 允许命令

只允许只读命令：
- `git log` / `git logs`
- `git fetch`
- `git status`
- `git diff`
- `git show`
- `git branch` (查看分支)

## Commit Message 规范

**格式：`<type>(<scope>): <subject>`（一句英文）**

### 强制要求

#### type（只能是以下 5 个之一）
- `feat` - 新功能
- `fix` - 修复 bug
- `refactor` - 重构（既不是新增功能，也不是修复 bug）
- `test` - 测试相关
- `ci` - CI/CD 相关

**禁止使用其他 type**（如 `docs` / `style` / `chore` / `perf` 等）

#### scope（必须精确匹配）

**前端改动**：
- 只能是 `web`
- **禁止**使用：`frontend` / `react` / `ui` / `client` / `fe` 等

**后端改动**：
- 必须是**不带前缀的完整模块名**
- 示例：`shared` / `ai` / `resource` / `starter`
- **禁止**（如 `crystal-shared` / `crystal-ai` / `crystal-resource`）

#### subject（主题）
- 动词开头
- 小写
- 无句号
- 一句话说明改动内容

### 正确示例

✅ `feat(web): add ai model management page`
✅ `fix(crystal-shared): resolve rate limiter issue`
✅ `refactor(crystal-ai): simplify prompt template logic`
✅ `test(crystal-starter): add oauth account controller tests`

### 错误示例

❌ `feat(frontend): add ai page` - scope 错误，应该是 `web`
❌ `fix(shared): fix bug` - scope 错误，应该是 `crystal-shared`
❌ `feat(web): Add AI Page.` - subject 首字母大写、有句号
❌ `docs(web): update readme` - type 错误，只能用规定的 5 个

### 提供方式

当需要提交时，给用户提供符合上述规范的 Commit Message，让用户自行 commit 和 push。
