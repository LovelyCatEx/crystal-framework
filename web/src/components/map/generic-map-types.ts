import type {BaiduMapConfig} from "@/components/map/baidu/BaiduMap.tsx";
import type {HTMLProps} from "react";

export enum GenericMapType {
    BAIDU,
    AMAP
}

export interface MapComponents {
    zoom?: boolean;
    fullScreen?: boolean;
    geoLocate?: boolean;
    exportImage?: boolean;
    compass?: boolean;
    mapInfo?: boolean;
    logo?: boolean;
    scale?: boolean;
}

export type GenericMapProps = HTMLProps<HTMLDivElement> & ({
    mapType: GenericMapType.BAIDU;
    mapSettings: BaiduMapConfig;
    components: MapComponents;
} | {
    mapType: GenericMapType.AMAP;
    settings: unknown;
    components: MapComponents;
});