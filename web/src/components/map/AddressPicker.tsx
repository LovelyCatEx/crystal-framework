import {forwardRef, type ReactNode, useCallback, useEffect, useImperativeHandle, useRef, useState} from "react";
import {List, Spin, theme} from "antd";
import {EnvironmentFilled} from "@ant-design/icons";
import {GenericMap} from "@/components/map/GenericMap.tsx";
import type {
    GenericAddressPoi,
    GenericLatLngPoint,
    GenericMapMoveEvent,
    GenericMapProps,
    GenericMapRef,
} from "@/components/map/generic-map-types.ts";

const {useToken} = theme;

export type AddressPickerItem = GenericAddressPoi;

export type AddressPickerProps = GenericMapProps & {
    onAddressSelect?: (item: AddressPickerItem) => void;
    pinIcon?: ReactNode;
    mapHeight?: number | string;
    listHeight?: number | string;
    emptyText?: string;
    loadingText?: string;
};

export interface AddressPickerRef {
    getMapRef: () => GenericMapRef | null;
    refresh: () => void;
    lookupAt: (point: GenericLatLngPoint) => void;
}

export const AddressPicker = forwardRef<AddressPickerRef, AddressPickerProps>((props, ref) => {
    const {token} = useToken();
    const {
        onAddressSelect,
        pinIcon,
        mapHeight = '60%',
        listHeight,
        emptyText,
        loadingText,
        events,
        style,
        className,
        ...mapProps
    } = props;

    const mapRef = useRef<GenericMapRef | null>(null);
    const [items, setItems] = useState<AddressPickerItem[]>([]);
    const [loading, setLoading] = useState(false);
    const requestSeqRef = useRef(0);

    const lookup = useCallback(async (point: GenericLatLngPoint) => {
        const seq = ++requestSeqRef.current;
        setLoading(true);
        try {
            const result = await mapRef.current?.tools.searchAddressByPoint(point);
            if (seq !== requestSeqRef.current) return;
            setItems(result?.surroundingPois ?? []);
        } finally {
            if (seq === requestSeqRef.current) setLoading(false);
        }
    }, []);

    useEffect(() => {
        const initialCenter = props.initialProps?.center;
        if (initialCenter) {
            void lookup(initialCenter);
        }
    }, []);

    useImperativeHandle(ref, () => ({
        getMapRef: () => mapRef.current,
        refresh: () => {
            const center = props.initialProps?.center;
            if (center) void lookup(center);
        },
        lookupAt: (point) => void lookup(point),
    }));

    const handleItemClick = (item: AddressPickerItem) => {
        mapRef.current?.moveCameraTo(item.point, { duration: 500 });
        void lookup(item.point);
        onAddressSelect?.(item);
    };

    return (
        <div
            className={className}
            style={{display: 'flex', flexDirection: 'column', width: '100%', height: '100%', ...style}}
        >
            <div style={{position: 'relative', height: mapHeight, width: '100%'}}>
                <GenericMap
                    {...mapProps as GenericMapProps}
                    ref={mapRef}
                    style={{width: '100%', height: '100%'}}
                    events={{
                        ...events,
                        onMoveEnd: (e: GenericMapMoveEvent) => {
                            events?.onMoveEnd?.(e);
                            void lookup(e.center);
                        },
                    }}
                />
                <div
                    style={{
                        position: 'absolute',
                        left: '50%',
                        top: '50%',
                        transform: 'translate(-50%, -100%)',
                        pointerEvents: 'none',
                        fontSize: 24,
                        color: token.colorPrimary,
                        lineHeight: 1,
                        zIndex: 999
                    }}
                >
                    {pinIcon ?? <EnvironmentFilled/>}
                </div>
            </div>
            <div style={{flex: 1, height: listHeight, overflowY: 'auto', borderTop: '1px solid #f0f0f0'}}>
                <List<AddressPickerItem>
                    locale={{emptyText: emptyText ?? 'No nearby addresses'}}
                    loading={loading ? {tip: loadingText, indicator: <Spin/>} : false}
                    dataSource={items}
                    renderItem={(item, index) => (
                        <List.Item
                            key={item.uid ?? `${item.title}-${index}`}
                            onClick={() => handleItemClick(item)}
                            style={{cursor: 'pointer', padding: '12px 16px'}}
                        >
                            <List.Item.Meta
                                avatar={<EnvironmentFilled style={{color: token.colorPrimary, fontSize: 18}}/>}
                                title={item.title || item.address}
                                description={item.address}
                            />
                        </List.Item>
                    )}
                />
            </div>
        </div>
    );
});
