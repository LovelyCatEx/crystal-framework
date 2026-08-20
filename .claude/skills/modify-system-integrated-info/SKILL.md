---
name: modify-system-integrated-info
description: 在 SystemIntegratedInfo 中添加新的系统状态信息，当需要在前端启动时获取系统级配置或状态时使用。
---

# 修改 SystemIntegratedInfo

## 触发条件

- 需要在前端启动时获取某个系统级配置或状态
- 需要在前端根据系统配置动态调整 UI 行为
- 该信息不需要认证即可访问

## 关联 Skills

- `add-system-settings` - 如果新字段来自系统设置表（需要持久化且可通过管理后台修改）

---

## 强制步骤（必须执行）

### 第 1 步：调查现有实现（必须）

**目的：** 理解现有数据结构和使用方式，避免重复造轮子

**必须读取的文件：**
1. `crystal-system/src/main/kotlin/.../controller/SystemIntegratedInfoController.kt` - 了解后端如何构建返回数据
2. `crystal-system/src/main/kotlin/.../controller/vo/SystemIntegratedInfoVO.kt` - 了解后端 VO 定义
3. `web/src/types/system/system-integrated.types.ts` - 了解前端类型定义
4. `web/src/context/SystemIntegratedContext.tsx` - 了解前端如何消费这些数据
5. `web/src/api/system/system-integrated.api.ts` - 了解前端如何调用 API

**必须确认的问题：**
- 现有返回了哪些字段？
- 前端如何使用这些字段？
- 是否已经有类似的字段可以复用？

---

### 第 2 步：后端修改 VO（必须）

**文件：** `crystal-system/src/main/kotlin/.../controller/vo/SystemIntegratedInfoVO.kt`

**操作：** 在数据类中添加新字段

**示例：**
```kotlin
data class SystemIntegratedInfoVO(
    val maintenance: MaintenanceInfoVO,
    val waterMark: WaterMark,
    val enabledOAuthPlatforms: List<Int>,
    val disabledModules: List<String>,  // 已有字段
    val newField: YourType,              // ← 新增字段
)
```

---

### 第 3 步：后端在 Controller 中填充新字段（必须）

**文件：** `crystal-system/src/main/kotlin/.../controller/SystemIntegratedInfoController.kt`

**操作：** 在 `getSystemIntegratedInfo()` 方法中构建新字段的值

**示例：**
```kotlin
@Unauthorized
@GetMapping("/integratedInfo")
suspend fun getSystemIntegratedInfo(): ApiResponse<*> {
    val systemSettings = systemSettingsService.getSystemSettings()
    
    // 构建你的新字段值
    val newFieldValue = ... 
    
    return ApiResponse.success(
        SystemIntegratedInfoVO(
            maintenance = ...,
            waterMark = ...,
            enabledOAuthPlatforms = ...,
            disabledModules = ...,
            newField = newFieldValue,  // ← 填充新字段
        )
    )
}
```

---

### 第 4 步：前端修改类型定义（必须）

**文件：** `web/src/types/system/system-integrated.types.ts`

**操作：** 在接口中添加对应的 TypeScript 类型

**示例：**
```typescript
export interface SystemIntegratedInfoVO {
    maintenance: MaintenanceInfoVO;
    waterMark: WaterMarkInfo;
    enabledOAuthPlatforms: number[];
    disabledModules: string[];
    newField: YourType;  // ← 添加新字段类型
}
```

---

### 第 5 步：前端在 Context 中提供访问方式（必须）

**文件：** `web/src/context/SystemIntegratedContext.tsx`

**必须修改的地方：**

**1. 更新 Context Value 接口**（如果需要派生字段或辅助方法）
```typescript
interface SystemIntegratedContextValue {
    integratedInfo: SystemIntegratedInfoVO | undefined;
    // ... 其他字段
    newField: YourType | undefined;           // ← 直接暴露字段
    // 或
    helperMethod: (param: string) => boolean; // ← 提供辅助方法
    isLoading: boolean;
    error: Error | undefined;
    mutate: () => void;
}
```

**2. 在 Provider 中填充 value**
```typescript
export function SystemIntegratedProvider({children}: { children: React.ReactNode }) {
    const {data, isLoading, error, mutate} = useSWR<SystemIntegratedInfoVO>(
        'systemIntegratedInfo',
        () => getSystemIntegratedInfo().then((res) => res.data!),
    );

    const value: SystemIntegratedContextValue = {
        integratedInfo: data,
        // ... 其他字段
        newField: data?.newField,
        helperMethod: (param) => /* 实现逻辑 */,
        isLoading,
        error,
        mutate,
    };

    return (
        <SystemIntegratedContext.Provider value={value}>
            {children}
        </SystemIntegratedContext.Provider>
    );
}
```

---

### 第 6 步：编译验证（必须）

**后端：**
```bash
./mvnw clean compile -DskipTests
```

**前端：**
```bash
cd web && npx tsc --noEmit
```

**必须确认：**
- 后端编译通过
- 前端 TypeScript 检查通过
- API 调用返回包含新字段（可通过浏览器 DevTools 查看 `/api/v1/system/integratedInfo` 的响应）

---

## 可选步骤

### 可选：如果新字段来自 SystemSettings

**关联 Skill：** `add-system-settings`

**何时需要：** 新字段的值来自系统设置表（需要持久化且可通过管理后台修改）

**操作：**
1. 按照 `add-system-settings` skill 的完整流程添加系统设置项
2. 在 `SystemIntegratedInfoController` 中通过 `systemSettingsService.getSystemSettings()` 读取

---

### 可选：添加前端使用示例的注释

**文件：** `web/src/context/SystemIntegratedContext.tsx`

**建议：** 在接口定义上方添加 JSDoc 注释说明用途和使用示例

```typescript
/**
 * Example usage:
 * const {newField} = useSystemIntegrated();
 * if (newField.enabled) { ... }
 */
interface SystemIntegratedContextValue {
    // ...
}
```

---

## 关键原则

- **SystemIntegratedInfo 是前端启动时一次性获取的全局系统状态**
- **不需要认证即可访问**（`@Unauthorized` 注解）
- **用于前端根据系统配置调整 UI 行为**
- **不适合频繁变化的数据**（SWR 有缓存，默认不会频繁刷新）
- **前后端类型必须保持一致**

---

## 示例：添加模块禁用状态

### 后端 VO
```kotlin
data class SystemIntegratedInfoVO(
    // ... 其他字段
    val disabledModules: List<String>,  // ← 已有示例
)
```

### 后端 Controller
```kotlin
val disabledModules = buildList {
    if (!systemSettings.module.tenantEnabled) add(SystemModulePathConstants.Tenant.KEY)
    if (!systemSettings.module.approvalEnabled) add(SystemModulePathConstants.Approval.KEY)
}
```

### 前端类型
```typescript
export interface SystemIntegratedInfoVO {
    // ... 其他字段
    disabledModules: string[];
}
```

### 前端 Context
```typescript
interface SystemIntegratedContextValue {
    disabledModules: string[];
    isModuleEnabled: (moduleKey: string) => boolean;  // 辅助方法
}

const value: SystemIntegratedContextValue = {
    disabledModules: data?.disabledModules ?? [],
    isModuleEnabled: (moduleKey: string) => !(data?.disabledModules ?? []).includes(moduleKey),
    // ...
};
```

### 前端使用
```typescript
const {isModuleEnabled} = useSystemIntegrated();
const tenantEnabled = isModuleEnabled('tenant');

{tenantEnabled && <TenantFeature />}
```
