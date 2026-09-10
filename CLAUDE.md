# CLAUDE.md

本文档为 Claude Code（claude.ai/code）在此仓库中工作时提供指导。

## 🚨 强制规则

### 规则 0: 用户提问 vs 执行指令

**用户输入只有两种：① 提问/询问  ② 执行指令。判断错误=违规=立即停止。**

**提问/询问**（疑问句、"为什么"、"怎么"、"建议"、"告诉我"）：
- ✅ 只能：纯文字回答问题
- ❌ 禁止：调用任何工具、修改文件、执行命令

**执行指令**（祈使句、"帮我写"、"实现"、"添加"、"修改"）→ 继续下一步

### 规则 1: Skills 强制扫描流程

**收到执行指令后，动手前必须执行以下流程，缺一步=违规：**

1. 列出所有 `.claude/skills/` 下的 skills
2. 逐个判断相关性
3. 读取相关 skill 的完整内容
4. 复述 skill 的关键步骤
5. 声明执行计划

**禁止**：看到任务就直接动手、自己觉得"很简单"就跳过 skill、凭经验/直觉操作。

### 规则 2: 调查现有实现

添加类似 X 的新功能 Y 时，必须先完整调查 X 的实现：
1. 找到 X 的所有相关文件
2. 逐个读取，理解完整流程
3. 列出 Y 需要仿照的部分
4. 然后才能开始写 Y

### 规则 3: 禁止质疑用户

用户说有问题，就一定有问题。

**禁止**：解释、"我找不到"、"可能是"、"实际上"、"我会改"等空话。

**正确做法**：认错 + 立即执行补救措施（调用工具重新操作）。

## 最小影响面原则

改动范围**只能等于、不能大于**问题范围：

1. 禁止为单个子类改基类
2. 优先覆写钩子，不是禁用/抛异常
3. 复杂度飙升 = 方向错误
4. 被纠正第二次 = 停手复述意图

## 构建命令

### 前端（web/）
- `cd web && pnpm dev` - 启动开发服务器
- `cd web && pnpm build` - TypeScript 检查 + 构建
- `cd web && pnpm type-check` - 类型检查

### 后端（根目录）
- `./mvnw clean install -DskipTests` - 构建所有模块
- `./mvnw clean install -pl crystal-starter -am -DskipTests` - 构建 starter

## 技术栈

- **后端**: Spring Boot 4 + Kotlin 2.3 + WebFlux + R2DBC + PostgreSQL + Redis
- **前端**: React 19 + TypeScript + Ant Design 6 + Vite 7 + pnpm + SWR

## 编码规范

详细规范见 `.claude/rules/`：

### 后端规则
@.claude/rules/backend-base.md
@.claude/rules/backend-entity.md
@.claude/rules/backend-service.md
@.claude/rules/backend-controller.md

### 前端规则
@.claude/rules/frontend-base.md
@.claude/rules/frontend-components.md
@.claude/rules/frontend-api.md
@.claude/rules/frontend-swr.md
@.claude/rules/frontend-i18n.md

### 跨端规则
@.claude/rules/git.md

## 核心禁止行为

- **破坏性 Git 命令**（commit/push/merge）- 只提供 Commit Message 让用户自行提交
- **整体回滚代码** - 必须保留有效改动，逐项修复
- **魔法值** - 必须用 constants 包的常量
- **单文件多定义** - 一个文件只能有一个类/接口/枚举

## 任务完成标准

每次任务完成后必须：
1. 逐条对照 CLAUDE.md 和相关 rules 检查合规
2. 向用户汇报检查结果
3. 不汇报视为未完成
