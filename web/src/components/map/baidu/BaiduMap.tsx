import {forwardRef, type HTMLProps, useEffect, useImperativeHandle, useRef} from "react";
import * as mapvthree from '@baidumap/mapv-three';
import type {GenericLatLngPoint} from "@/components/map/generic-map-types.ts";
import type {Engine} from "@baidumap/mapv-three";

export interface BaiduMapComponents {
    zoom?: boolean;
    fullScreen?: boolean;
    geoLocate?: boolean;
    exportImage?: boolean;
    compass?: boolean;
    mapInfo?: boolean;
    logo?: boolean;
    scale?: boolean;
}

export interface BaiduMapInitialProps {
    center?: GenericLatLngPoint;
    heading?: number;
    pitch?: number;
    range?: number;
}

export interface BaiduMapEvent {
    point: [number, number, number];
}

export interface BaiduMapEvents {
    onClick?: (e: BaiduMapEvent) => void;
    onMouseEnter?: (e: BaiduMapEvent) => void;
    onMouseLeave?: (e: BaiduMapEvent) => void;
    onMouseDown?: (e: BaiduMapEvent) => void;
    onMouseUp?: (e: BaiduMapEvent) => void;
}

export interface BaiduMapConfig extends HTMLProps<HTMLDivElement> {
    ak: string;
    initialProps?: BaiduMapInitialProps;
    components?: BaiduMapComponents;
    events?: BaiduMapEvents;
}

export interface BaiduMapRef {
    getEngine: () => Engine;
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
        latLngPointToAddress: (point: GenericLatLngPoint) => ReturnType<mapvthree.services.Geocoder["getLocation"]>;
    }
}

export const BaiduMap = forwardRef<BaiduMapRef, BaiduMapConfig>(
    ({ ak, initialProps, components, events, ...restProps }: BaiduMapConfig, ref)=> {
    mapvthree.BaiduMapConfig.ak = ak;

    const resolvedInitialProps = initialProps ?? {
        center: [116.414, 39.915],
        heading: 0,
        pitch: 0,
        range: 2000,
    }

    const divRef = useRef<HTMLDivElement | null>(null);
    const engineRef = useRef<mapvthree.Engine | null>(null);

    useEffect(() => {
        const engine = new mapvthree.Engine(divRef.current as HTMLDivElement, {
            widgets: {
                zoom: {
                    enabled: components?.zoom !== false,
                },
                fullscreen: {
                    enabled: components?.fullScreen !== false,
                },
                geoLocate: {
                    enabled: components?.geoLocate !== false,
                },
                exportImage: {
                    enabled: components?.exportImage !== false,
                },
                compass: {
                    enabled: components?.compass !== false,
                },
                mapInfo: {
                    enabled: components?.mapInfo !== false,
                },
                logo: {
                    enabled: components?.logo !== false,
                },
                scale: {
                    enabled: components?.scale !== false,
                }
            },
            map: {
                provider: new mapvthree.BaiduVectorTileProvider(),
                ...resolvedInitialProps
            },
            rendering: {
                enableAnimationLoop: true,
            },
            event: {},
            selection: {},
        });

        engineRef.current = engine;

        const map = engine.map;

        map.addEventListener('click', (e: BaiduMapEvent) => {
            events?.onClick?.(e);
        });
        map.addEventListener('mouseenter', (e: BaiduMapEvent) => {
            events?.onMouseEnter?.(e);
        });
        map.addEventListener('mouseleave', (e: BaiduMapEvent) => {
            events?.onMouseLeave?.(e);
        });
        map.addEventListener('pointerdown', (e: BaiduMapEvent) => {
            events?.onMouseDown?.(e);
        });
        map.addEventListener('pointerup', (e: BaiduMapEvent) => {
            events?.onMouseUp?.(e);
        });

        return () => {
            engine.dispose();
        };
    }, []);

    const geocoder = new mapvthree.services.Geocoder();

    useImperativeHandle(ref, () => ({
        getEngine() {
            return engineRef.current!;
        },
        moveCameraTo(point, options) {
            this.getEngine().map.flyTo(
                point,
                {
                    heading: options?.heading ?? this.getEngine().map.getHeading(),
                    pitch: options?.pitch ?? this.getEngine().map.getPitch(),
                    range: options?.range ?? this.getEngine().map.getRange(),
                    duration: options?.duration ?? 0,
                    cancel() {},
                    complete() {
                        options?.onComplete?.();
                    },
                }
            );
        },
        tools: {
            addressToLatLngPoint(address) {
                return geocoder.getPoint(address, () => {});
            },
            latLngPointToAddress(point) {
                return geocoder.getLocation(point, () => {});
            }
        }
    }));

    return <div ref={divRef} {...restProps} />
});