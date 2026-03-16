import type {ThemeColor, ThemeConfig} from "@/types/theme.types.ts";

export const THEME_STORAGE_KEY = 'app-theme-color-key';

export const themeColors: ThemeColor[] = [
    {
        key: 'pink',
        name: '樱花粉',
        colorPrimary: '#FF8DA1',
        itemSelectedBg: 'rgba(255,240,243,0.8)',
        itemSelectedColor: '#FF8DA1',
    },
    {
        key: 'blue',
        name: '天空蓝',
        colorPrimary: '#4A90E2',
        itemSelectedBg: 'rgba(74,144,226,0.1)',
        itemSelectedColor: '#4A90E2',
    },
    {
        key: 'purple',
        name: '梦幻紫',
        colorPrimary: '#9B59B6',
        itemSelectedBg: 'rgba(155,89,182,0.1)',
        itemSelectedColor: '#9B59B6',
    },
    {
        key: 'green',
        name: '清新绿',
        colorPrimary: '#27AE60',
        itemSelectedBg: 'rgba(39,174,96,0.1)',
        itemSelectedColor: '#27AE60',
    },
    {
        key: 'orange',
        name: '活力橙',
        colorPrimary: '#F39C12',
        itemSelectedBg: 'rgba(243,156,18,0.1)',
        itemSelectedColor: '#F39C12',
    },
    {
        key: 'red',
        name: '热情红',
        colorPrimary: '#E74C3C',
        itemSelectedBg: 'rgba(231,76,60,0.1)',
        itemSelectedColor: '#E74C3C',
    },
    {
        key: 'cyan',
        name: '青柠青',
        colorPrimary: '#1ABC9C',
        itemSelectedBg: 'rgba(26,188,156,0.1)',
        itemSelectedColor: '#1ABC9C',
    },
    {
        key: 'indigo',
        name: '深邃靛',
        colorPrimary: '#5D6D7E',
        itemSelectedBg: 'rgba(93,109,126,0.1)',
        itemSelectedColor: '#5D6D7E',
    },
];

export const defaultThemeColor = themeColors[0];

export function getStoredThemeKey(): string {
    return localStorage.getItem(THEME_STORAGE_KEY) || defaultThemeColor.key;
}

export function setStoredThemeKey(key: string): void {
    localStorage.setItem(THEME_STORAGE_KEY, key);
}

export function getThemeByKey(key: string): ThemeColor {
    return themeColors.find(t => t.key === key) || defaultThemeColor;
}

export function buildThemeConfig(themeColor: ThemeColor): ThemeConfig {
    return {
        token: {
            colorPrimary: themeColor.colorPrimary,
            borderRadius: 12,
            fontFamily: 'Inter, system-ui, sans-serif',
        },
        components: {
            Layout: {
                headerBg: 'rgba(255, 255, 255, 0.7)',
                siderBg: '#ffffff',
            },
            Menu: {
                itemBorderRadius: 12,
                itemSelectedBg: themeColor.itemSelectedBg,
                itemSelectedColor: themeColor.itemSelectedColor,
            },
        },
    };
}

export function updateThemeCSSVariables(themeColor: ThemeColor): void {
    const root = document.documentElement;
    root.style.setProperty('--theme-color-primary', themeColor.colorPrimary);
}
