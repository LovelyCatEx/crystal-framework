---
paths:
  - "web/src/api/**/*.ts"
---

# API 规范

## 文件放置

所有 API 文件必须放入 `api` 中的对应文件夹，按后端对应的模块/包进行分类。

特殊类型的 api 文件（`request` / `system-request`）无需分类。

## DTO/VO 放置规则

- **DTO**（前端→后端）：写在相应 api 文件的头部/尾部
- **VO**（后端→前端）：放在 `types` 文件夹中，目录结构严格对应 `api` 文件夹

**禁止一个类一个文件**，请检查有没有相似的 type 定义文件。

## Long 类型接收规则

所有后端 `Long` 类型的字段（`id`、`xxxId`、`xxxTime` 等），前端**只能用 `string` 类型来接**，禁止使用 `number`。

后端序列化时用 `ToStringSerializer` 将 `Long` → `String` 再传给前端。

## BaseEntity 对应

对于后端继承 BaseEntity 的实体类，前端对应也必须继承该接口：

```typescript
export interface BaseEntity {
    id: string;          // 后端 Long 序列化为 String
    createdTime: string;
    modifiedTime: string;
}
```

## BaseManagerController

对于后端的标准化控制器（StandardManagerController）必须继承前端的 BaseManagerController。

写法参照其他相关文件，若无特殊情况，均以 `export const` 的方式导出。

### 5 个泛型参数

```typescript
class BaseManagerController<ENTITY, C, R = BaseManagerReadDTO, U = BaseManagerUpdateDTO, D = BaseManagerDeleteDTO> {}
```

| 泛型参数 | 说明                                              |
| -------- | ------------------------------------------------- |
| ENTITY   | 实体类型（对应后端的实体类）                      |
| C        | Create DTO                                        |
| R        | Read/Query DTO（默认 BaseManagerReadDTO）         |
| U        | Update DTO（默认 BaseManagerUpdateDTO）           |
| D        | Delete DTO（默认 BaseManagerDeleteDTO）           |

### 5 个方法

| 方法            | 请求方式 | URL 模式                   | 后端注解        | 前端 Content-Type               | 说明                     |
| --------------- | -------- | -------------------------- | --------------- | ------------------------------- | ------------------------ |
| create(dto)     | POST     | /api{baseUrl}/create       | @ModelAttribute | application/x-www-form-urlencoded | 新增                     |
| query(dto)      | POST     | /api{baseUrl}/query        | @RequestBody    | application/json                | 分页查询，返回 PaginatedResponseData |
| update(dto)     | POST     | /api{baseUrl}/update       | @ModelAttribute | application/x-www-form-urlencoded | 更新                     |
| delete(dto)     | POST     | /api{baseUrl}/delete       | @ModelAttribute | application/x-www-form-urlencoded | 删除                     |
| list(params?)   | GET      | /api{baseUrl}/list?xxx=yyy | @GetMapping     | query params                    | 全量列表                 |
| getById(id)     | 复用 query | —                        | —               | —                               | 按 ID 查单条             |

**请求路径拼接规则**：`/api` + `baseUrl` + 方法路径。开发环境下 Vite 代理将 `/api` 重写为 `/api/v1` 转发到后端。

## Content-Type 规则

- **@RequestBody 后端** → 前端必须传 `{'Content-Type': 'application/json'}` 给 `doPost` / `doPut` / `doPatch`
- **@ModelAttribute 后端** → 前端使用 `doPost` / `doPut` / `doDelete` 的默认行为（`application/x-www-form-urlencoded`）
- **@GetMapping / @RequestParam 后端** → 前端使用 `doGet(url, queryParams)` 传查询参数

禁止在 api 文件中直接使用 `axios` 实例、`URLSearchParams`、`qs` 等底层 API 手动构造请求体。

`system-request.ts` 中的 `preProcessHeaders` 已默认对 POST/PUT/PATCH 设置 `application/x-www-form-urlencoded`，调用方只需在 `@RequestBody` 场景显式覆写。

## 禁止编造 API 调用

**禁止编造不存在的接口、请求类型、参数。**

写接口必须先彻底阅读相关 Controller 接口以及对应参数/DTO 的源代码再动手。

必须把新加的接口以及对应后端什么 Controller/DTO 完整的告知用户，否则视为违规代码。

## Query API 规则

**调用 Query API 时，严禁使用 PageSize = 100 甚至更大的数获取完整列表。**

如有需要必须使用 `readAll()` 函数获取全部记录。

## SWR 缓存

调用 api 时，先判断该 api 是否有缓存的价值。

本项目提供预设的 SWR-Composition 可用，位于 `compositions` 文件夹。内置多种方式，请按需使用。
