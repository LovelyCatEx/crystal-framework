import {
    type GenericAddressPoi,
    type GenericMapProps,
    type GenericMapRef,
    GenericMapType,
} from "@/components/map/generic-map-types.ts";
import {BaiduMap, type BaiduMapEvent, type BaiduMapMoveEvent, type BaiduMapRef} from "@/components/map/baidu/BaiduMap.tsx";
import {forwardRef, useImperativeHandle, useRef} from "react";

export const GenericMap = forwardRef<GenericMapRef, GenericMapProps>(
    (config, ref) => {
    if (config.mapType === GenericMapType.BAIDU) {
        const { mapType, mapSettings, initialProps, components, events, ...restConfig } = config;

        const baiduMapRef = useRef<BaiduMapRef | null>(null);

        useImperativeHandle(ref, () => ({
            moveCameraTo(point, options) {
                baiduMapRef.current?.moveCameraTo?.(point, options);
            },
            tools: {
                addressToLatLngPoint(address) {
                    return baiduMapRef.current!.tools.addressToLatLngPoint(address);
                },
                latLngPointToAddress(point) {
                    return new Promise(async (resolve, reject) => {
                        try {
                            const result = await baiduMapRef.current!.tools.latLngPointToAddress(point);
                            resolve(result.address as string);
                        } catch {
                            reject(null);
                        }
                    });
                },
                async searchAddressByPoint(point) {
                    try {
                        const result = await baiduMapRef.current!.tools.latLngPointToAddress(point);
                        const resultPoint = result.point as [number, number] | undefined;
                        if ('surroundingPois' in result) {
                            const pois: GenericAddressPoi[] = ((result.surroundingPois ?? []) as Array<{
                                title?: string;
                                address?: string;
                                point?: [number, number];
                                uid?: string;
                            }>).map(p => ({
                                title: p.title ?? '',
                                address: p.address ?? '',
                                point: p.point ?? point,
                                uid: p.uid,
                            }));

                            return {
                                point: resultPoint ?? point,
                                address: (result.address as string) ?? '',
                                surroundingPois: pois,
                            };
                        } else {
                            return {
                                point: resultPoint ?? point,
                                address: (result.address as string) ?? '',
                                surroundingPois: [],
                            };
                        }
                    } catch {
                        return null;
                    }
                }
            }
        }));

        const toGenericEvent = (e: BaiduMapEvent) => ({latLng: [e.point[0], e.point[1]] as [number, number]});
        const toGenericMoveEvent = (e: BaiduMapMoveEvent) => ({
            center: e.center,
            heading: e.heading,
            pitch: e.pitch,
            range: e.range,
        });

        return <BaiduMap
            ref={baiduMapRef}
            ak={mapSettings.ak}
            initialProps={initialProps}
            components={components}
            events={{
                onClick: e => events?.onClick?.(toGenericEvent(e)),
                onDoubleClick: e => events?.onDoubleClick?.(toGenericEvent(e)),
                onMouseMove: e => events?.onMouseMove?.(toGenericEvent(e)),
                onMouseEnter: e => events?.onMouseEnter?.(toGenericEvent(e)),
                onMouseLeave: e => events?.onMouseLeave?.(toGenericEvent(e)),
                onMouseDown: e => events?.onMouseDown?.(toGenericEvent(e)),
                onMouseUp: e => events?.onMouseUp?.(toGenericEvent(e)),
                onRightClick: e => events?.onRightClick?.(toGenericEvent(e)),
                onRightDoubleClick: e => events?.onRightDoubleClick?.(toGenericEvent(e)),
                onMoveStart: e => events?.onMoveStart?.(toGenericMoveEvent(e)),
                onMove: e => events?.onMove?.(toGenericMoveEvent(e)),
                onMoveEnd: e => events?.onMoveEnd?.(toGenericMoveEvent(e)),
            }}
            {...restConfig}
        />;
    } else {
        return <>Unsupported MapType: ${config.mapType.toString()}</>;
    }
});