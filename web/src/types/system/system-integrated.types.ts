export interface SystemIntegratedInfoVO {
    maintenance: MaintenanceInfoVO;
    waterMark: WaterMarkInfo;
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
