/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

export type PageAnimationType = 'none' | 'fade' | 'slide-left' | 'slide-right' | 'slide-up' | 'scale';

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
