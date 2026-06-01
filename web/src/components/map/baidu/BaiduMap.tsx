import {type HTMLProps, useEffect, useRef} from "react";
import * as mapvthree from '@baidumap/mapv-three';

export interface BaiduMapConfig extends HTMLProps<HTMLDivElement> {
    ak: string;
}

export function BaiduMap({ ak, ...restProps }: BaiduMapConfig) {
    mapvthree.BaiduMapConfig.ak = ak;

    const ref = useRef<HTMLDivElement | null>(null);

    useEffect(() => {
        const engine = new mapvthree.Engine(ref.current as HTMLDivElement, {
            widgets: {
                zoom: {
                    enabled: true, // 开启地图缩放控件
                },
                fullscreen: {
                    enabled: true, // 开启全屏控件
                },
                geoLocate: {
                    enabled: true, // 开启定位控件
                },
                exportImage: {
                    enabled: true, // 开启导出图片控件
                },
                compass: {
                    enabled: true, // 开启视角控件
                },
                mapInfo: {
                    enabled: true, // 开启地理坐标显示
                },
                logo: {
                    enabled: true,
                },
                scale: {
                    enabled: true,
                }
            },
            map: {
                provider: new mapvthree.BaiduVectorTileProvider(),
                center: [116.414, 39.915],
                heading: 40,
                pitch: 80,
                range: 2000,
            },
            rendering: {
                enableAnimationLoop: true,
            },
            event: {},
            selection: {},
        });

        return () => {
            engine.dispose();
        };
    }, []);

    return <div ref={ref} {...restProps} />
}