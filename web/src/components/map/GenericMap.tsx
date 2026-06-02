import {type GenericMapProps, type GenericMapRef, GenericMapType} from "@/components/map/generic-map-types.ts";
import {BaiduMap, type BaiduMapEvent, type BaiduMapRef} from "@/components/map/baidu/BaiduMap.tsx";
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
                }
            }
        }));

        return <BaiduMap
            ref={baiduMapRef}
            ak={mapSettings.ak}
            initialProps={initialProps}
            components={components}
            events={{
                onClick(e: BaiduMapEvent) {
                    events?.onClick?.({
                        latLng: [e.point[0], e.point[1]],
                    })
                },
                onMouseEnter(e: BaiduMapEvent) {
                    events?.onMouseEnter?.({
                        latLng: [e.point[0], e.point[1]],
                    })
                },
                onMouseLeave(e: BaiduMapEvent) {
                    events?.onMouseLeave?.({
                        latLng: [e.point[0], e.point[1]],
                    })
                },
                onMouseDown(e: BaiduMapEvent) {
                    events?.onMouseDown?.({
                        latLng: [e.point[0], e.point[1]],
                    })
                },
                onMouseUp(e: BaiduMapEvent) {
                    events?.onMouseUp?.({
                        latLng: [e.point[0], e.point[1]],
                    })
                }
            }}
            {...restConfig}
        />;
    } else {
        return <>Unsupported MapType: ${config.mapType.toString()}</>;
    }
});