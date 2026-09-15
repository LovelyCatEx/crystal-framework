/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

export interface SystemIntegratedInfoVO {
    maintenance: MaintenanceInfoVO;
    waterMark: WaterMarkInfo;
    enabledOAuthPlatforms: number[];
    disabledModules: string[];
}

export interface MaintenanceInfoVO {
    canAccess: boolean;
    maintenanceMode: boolean;
}

export interface WaterMarkInfo {
    enabled: boolean;
    type: 'SYSTEM_NAME' | 'USER_NAME' | 'CUSTOM';
    customValue: string;
    fontColor: string;
}
