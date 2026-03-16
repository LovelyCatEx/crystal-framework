export type ThemeMode = 'light' | 'dark';

export interface ThemeColor {
    key: string;
    name: string;
    colorPrimary: string;
    itemSelectedBg: string;
    itemSelectedColor: string;
}

export interface ThemeConfig {
    token: {
        colorPrimary: string;
        borderRadius: number;
        fontFamily: string;
    };
    components: {
        Layout: {
            headerBg: string;
            siderBg: string;
        };
        Menu: {
            itemBorderRadius: number;
            itemSelectedBg: string;
            itemSelectedColor: string;
        };
    };
}
