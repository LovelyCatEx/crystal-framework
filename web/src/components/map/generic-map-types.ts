import type {BaiduMapConfig} from "@/components/map/baidu/BaiduMap.tsx";
import type {HTMLProps} from "react";

export enum GenericMapType {
    BAIDU = "BAIDU",
    AMAP = "AMAP",
}

export interface GenericMapComponents {
    zoom?: boolean;
    fullScreen?: boolean;
    geoLocate?: boolean;
    exportImage?: boolean;
    compass?: boolean;
    mapInfo?: boolean;
    logo?: boolean;
    scale?: boolean;
}

export interface GenericMapInitialProps {
    center?: GenericLatLngPoint;
    heading?: number;
    pitch?: number;
    range?: number;
}

export interface GenericMapEvent {
    latLng: GenericLatLngPoint;
}

export interface GenericMapEvents {
    onClick?: (e: GenericMapEvent) => void;
    onMouseEnter?: (e: GenericMapEvent) => void;
    onMouseLeave?: (e: GenericMapEvent) => void;
    onMouseDown?: (e: GenericMapEvent) => void;
    onMouseUp?: (e: GenericMapEvent) => void;
}

export type GenericMapCommonProps = {
    initialProps?: GenericMapInitialProps;
    components?: GenericMapComponents;
    events?: GenericMapEvents;
};


export type GenericMapProps = HTMLProps<HTMLDivElement> & ({
    mapType: GenericMapType.BAIDU;
    mapSettings: BaiduMapConfig;
} | {
    mapType: GenericMapType.AMAP;
    settings: unknown;
}) & GenericMapCommonProps;

export type GenericLatLngPoint = [number, number];

export interface GenericMapRef {
    moveCameraTo: (
        target: GenericLatLngPoint,
        options?: {
            duration?: number;
            heading?: number;
            pitch?: number;
            range?: number;
            onComplete?: () => void;
        }
    ) => void;
    tools: {
        addressToLatLngPoint: (address: string) => Promise<GenericLatLngPoint | null>;
        latLngPointToAddress: (point: GenericLatLngPoint) => Promise<string | null>;
    }
}