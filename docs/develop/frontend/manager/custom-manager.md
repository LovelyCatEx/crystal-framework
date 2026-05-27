# 非标准化 Manager 页面

并非所有管理页面都适合套用 `ManagerPageContainer`。当页面有以下特征时，建议直接手写页面结构：

- 布局不是简单的"过滤栏 + 表格"，例如监控大盘、图表页、多 Tab 页
- 数据来源不是单一的分页接口
- 需要高度定制的交互逻辑（实时推送、拖拽、画布等）

## 参考示例

`SystemMonitorPage`（系统监控页）是一个典型的非标准化 Manager 页面。它使用 ECharts 渲染折线图、定时轮询数据，布局完全自定义，与标准表格页面没有任何关联。

源码位于：

```
web/src/pages/manager/monitor/SystemMonitorPage.tsx
```

## 基础结构

非标准化页面同样建议遵循以下基础结构，保持与框架整体风格一致：

```tsx
import { useTranslation } from 'react-i18next';
import { ActionBarComponent } from '@/components/ActionBarComponent.tsx';
import { StandardCard } from '@/components/card/StandardCard.tsx';

export default function MyCustomPage() {
    const { t } = useTranslation();

    return (
        <>
            <ActionBarComponent
                title={t('pages.myCustomPage.title')}
                subtitle={t('pages.myCustomPage.subtitle')}
            />
            {/* 自定义内容区域 */}
            <StandardCard>
                ...
            </StandardCard>
        </>
    );
}
```

`ActionBarComponent` 提供统一的页面标题栏，`StandardCard` 提供统一的卡片容器样式，两者都是可选的，但建议保留以维持视觉一致性。
