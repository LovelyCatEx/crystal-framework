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

export interface BaiduMapMoveEvent {
    center: [number, number];
    heading: number;
    pitch: number;
    range: number;
}

export interface BaiduMapEvents {
    onClick?: (e: BaiduMapEvent) => void;
    onDoubleClick?: (e: BaiduMapEvent) => void;
    onMouseMove?: (e: BaiduMapEvent) => void;
    onMouseEnter?: (e: BaiduMapEvent) => void;
    onMouseLeave?: (e: BaiduMapEvent) => void;
    onMouseDown?: (e: BaiduMapEvent) => void;
    onMouseUp?: (e: BaiduMapEvent) => void;
    onRightClick?: (e: BaiduMapEvent) => void;
    onRightDoubleClick?: (e: BaiduMapEvent) => void;
    onMoveStart?: (e: BaiduMapMoveEvent) => void;
    onMove?: (e: BaiduMapMoveEvent) => void;
    onMoveEnd?: (e: BaiduMapMoveEvent) => void;
}

export interface BaiduMapMarker {
    location: GenericLatLngPoint;
    icon: string;
    width: number;
    height: number;
    draggable: boolean;
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

        // mapvthree exposes no public start/end events for camera change,
        // so we detect movement by polling the camera state per frame.
        // Only mouse-drag motion is reported: programmatic flyTo, wheel zoom
        // and keyboard panning are treated as silent state syncs.
        let pointerDown = false;

        map.addEventListener('click', (e: BaiduMapEvent) => {
            events?.onClick?.(e);
        });
        map.addEventListener('dblclick', (e: BaiduMapEvent) => {
            events?.onDoubleClick?.(e);
        });
        map.addEventListener('mousemove', (e: BaiduMapEvent) => {
            events?.onMouseMove?.(e);
        });
        map.addEventListener('mouseenter', (e: BaiduMapEvent) => {
            events?.onMouseEnter?.(e);
        });
        map.addEventListener('mouseleave', (e: BaiduMapEvent) => {
            events?.onMouseLeave?.(e);
        });
        map.addEventListener('pointerdown', (e: BaiduMapEvent) => {
            // Open the drag-detection window. Sync the baseline so the first
            // RAF after press doesn't compare against a stale snapshot from
            // a programmatic flyTo or wheel zoom that happened earlier.
            pointerDown = true;
            lastSnapshot = buildMoveEvent();
            idleFrames = 0;
            events?.onMouseDown?.(e);
        });
        map.addEventListener('pointerup', (e: BaiduMapEvent) => {
            // Close the window. moveEnd will fire after IDLE_FRAMES_TO_END
            // consecutive still frames, only if a moveStart was emitted.
            pointerDown = false;
            events?.onMouseUp?.(e);
        });
        map.addEventListener('rightclick', (e: BaiduMapEvent) => {
            events?.onRightClick?.(e);
        });
        map.addEventListener('rightdblclick', (e: BaiduMapEvent) => {
            events?.onRightDoubleClick?.(e);
        });

        const buildMoveEvent = (): BaiduMapMoveEvent => {
            const center = map.getCenter();
            return {
                center: [center[0], center[1]],
                heading: map.getHeading(),
                pitch: map.getPitch(),
                range: map.getRange(),
            };
        };

        // Floating-point tolerance for camera-state diffing.
        const EPSILON = 1e-6;
        // Frames of zero-delta needed before treating motion as ended (~100ms at 60fps).
        // Absorbs micro pauses between drag steps and inertia tail jitter.
        const IDLE_FRAMES_TO_END = 6;
        let lastSnapshot = buildMoveEvent();
        let idleFrames = 0;
        let isMoving = false;
        let rafId = 0;

        const tick = () => {
            const current = buildMoveEvent();
            const changed =
                Math.abs(current.center[0] - lastSnapshot.center[0]) > EPSILON ||
                Math.abs(current.center[1] - lastSnapshot.center[1]) > EPSILON ||
                Math.abs(current.heading - lastSnapshot.heading) > EPSILON ||
                Math.abs(current.pitch - lastSnapshot.pitch) > EPSILON ||
                Math.abs(current.range - lastSnapshot.range) > EPSILON;

            if (!pointerDown && !isMoving) {
                // Outside the drag window: keep the baseline current so that
                // programmatic flyTo / wheel / keyboard changes are ignored
                // and the next press starts from a fresh snapshot.
                if (changed) lastSnapshot = current;
            } else if (changed) {
                if (!isMoving) {
                    isMoving = true;
                    events?.onMoveStart?.(current);
                }
                events?.onMove?.(current);
                idleFrames = 0;
                lastSnapshot = current;
            } else if (isMoving && !pointerDown) {
                // Pointer released and camera settled — confirm moveEnd after
                // IDLE_FRAMES_TO_END to absorb inertia tail jitter.
                idleFrames++;
                if (idleFrames >= IDLE_FRAMES_TO_END) {
                    isMoving = false;
                    idleFrames = 0;
                    events?.onMoveEnd?.(current);
                }
            }

            rafId = requestAnimationFrame(tick);
        };
        rafId = requestAnimationFrame(tick);

        return () => {
            cancelAnimationFrame(rafId);
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
            async addressToLatLngPoint(address) {
                try {
                    const result: { x: number, y: number, z: number } = await geocoder.getPoint(address, () => {});
                    if (result) {
                        return [result.x, result.y]  as GenericLatLngPoint;
                    } else {
                        return null;
                    }
                } catch {
                    return null;
                }
            },
            latLngPointToAddress(point) {
                return geocoder.getLocation(point, () => {});
            }
        }
    }));

    return <div ref={divRef} {...restProps} />
});