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
            widgets: {},
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