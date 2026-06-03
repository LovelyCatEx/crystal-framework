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

export interface GenericMapMoveEvent {
    center: GenericLatLngPoint;
    heading: number;
    pitch: number;
    range: number;
}

export interface GenericMapEvents {
    onClick?: (e: GenericMapEvent) => void;
    onDoubleClick?: (e: GenericMapEvent) => void;
    onMouseMove?: (e: GenericMapEvent) => void;
    onMouseEnter?: (e: GenericMapEvent) => void;
    onMouseLeave?: (e: GenericMapEvent) => void;
    onMouseDown?: (e: GenericMapEvent) => void;
    onMouseUp?: (e: GenericMapEvent) => void;
    onRightClick?: (e: GenericMapEvent) => void;
    onRightDoubleClick?: (e: GenericMapEvent) => void;
    onMoveStart?: (e: GenericMapMoveEvent) => void;
    onMove?: (e: GenericMapMoveEvent) => void;
    onMoveEnd?: (e: GenericMapMoveEvent) => void;
}

export type GenericMapCommonProps = HTMLProps<HTMLDivElement> & {
    initialProps?: GenericMapInitialProps;
    components?: GenericMapComponents;
    events?: GenericMapEvents;
};


export type GenericMapProps = ({
    mapType: GenericMapType.BAIDU;
    mapSettings: BaiduMapConfig;
} | {
    mapType: GenericMapType.AMAP;
    settings: unknown;
}) & GenericMapCommonProps;

export type GenericLatLngPoint = [number, number];

export interface GenericAddressPoi {
    title: string;
    address: string;
    point: GenericLatLngPoint;
    uid?: string;
}

export interface GenericAddressLookupResult {
    point: GenericLatLngPoint;
    address: string;
    surroundingPois: GenericAddressPoi[];
}

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
        searchAddressByPoint: (point: GenericLatLngPoint) => Promise<GenericAddressLookupResult | null>;
    }
}