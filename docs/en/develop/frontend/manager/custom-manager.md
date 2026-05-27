# Custom Manager Page

Not every management page fits the `ManagerPageContainer` mold. Consider building a custom page when:

- The layout is not a simple "filter bar + table" — e.g. dashboards, chart pages, multi-tab views
- Data comes from multiple sources rather than a single paginated API
- The page requires highly custom interactions such as real-time streaming, drag-and-drop, or canvas rendering

## Reference Example

`SystemMonitorPage` is a typical custom manager page. It renders line charts with ECharts, polls data on a timer, and has a fully custom layout with no relation to the standard table pattern.

Source file:

```
web/src/pages/manager/monitor/SystemMonitorPage.tsx
```

## Base Structure

Custom pages should still follow this base structure to stay visually consistent with the rest of the framework:

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
            {/* Custom content */}
            <StandardCard>
                ...
            </StandardCard>
        </>
    );
}
```

`ActionBarComponent` provides a consistent page title bar, and `StandardCard` provides a consistent card container style. Both are optional but recommended for visual consistency.
