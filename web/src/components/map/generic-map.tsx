import {type GenericMapProps, GenericMapType} from "@/components/map/generic-map-types.ts";
import {BaiduMap} from "@/components/map/baidu/BaiduMap.tsx";

export function GenericMap(config: GenericMapProps) {
    if (config.mapType === GenericMapType.BAIDU) {

        return <BaiduMap ak={config.mapSettings.ak} />;
    } else if (config.mapType === GenericMapType.AMAP) {
        return <></>;
    } else {
        return <></>;
    }

}