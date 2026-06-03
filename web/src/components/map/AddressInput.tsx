import {type CSSProperties, type ReactNode, useEffect, useRef, useState} from "react";
import {Form, Input, type InputProps, message, theme, Tooltip} from "antd";
import type {FormItemProps} from "antd";
import {EnvironmentOutlined} from "@ant-design/icons";
import {useTranslation} from "react-i18next";
import {AddressPicker, type AddressPickerItem, type AddressPickerRef} from "@/components/map/AddressPicker.tsx";
import {GenericMapType} from "@/components/map/generic-map-types.ts";
import {currentEnvironment} from "@/config/env.ts";

const {useToken} = theme;

export interface AddressInputProps {
    formItemProps: Omit<FormItemProps, 'children'>;
    inputProps?: Omit<InputProps, 'suffix'>;
    panelHeight?: number | string;
    panelStyle?: CSSProperties;
    pinIcon?: ReactNode;
}

const DEFAULT_CENTER: [number, number] = [116.414, 39.915];
const DEFAULT_RANGE = 2000;

export function AddressInput(props: AddressInputProps) {
    const {formItemProps, inputProps, panelHeight = 480, panelStyle, pinIcon} = props;
    const {t} = useTranslation();
    const {token} = useToken();
    const form = Form.useFormInstance();
    const [open, setOpen] = useState(false);
    const pickerRef = useRef<AddressPickerRef | null>(null);
    const fieldName = formItemProps.name;

    const baiduAk = currentEnvironment.map.baidu.ak;

    // Fire once per open. Using a callback ref instead would re-trigger on
    // every AddressPicker re-render because its useImperativeHandle has no
    // deps and produces a fresh handle each render, which React reports to
    // callback refs as null + newInstance — causing infinite lookups.
    useEffect(() => {
        if (!open) return;
        let cancelled = false;
        void (async () => {
            const currentAddress = fieldName
                ? (form.getFieldValue(fieldName) as string | undefined) ?? ''
                : '';
            if (!currentAddress.trim() || cancelled) return;
            const mapRef = pickerRef.current?.getMapRef();
            if (!mapRef) return;
            // GenericMap.tools.addressToLatLngPoint declares Promise<GenericLatLngPoint | null>
            // but actually forwards Baidu's raw Promise<{point, address, ...}>; pull .point ourselves.
            const point = await mapRef.tools.addressToLatLngPoint(currentAddress);
            if (cancelled || !point) return;
            mapRef.moveCameraTo(point, {
                onComplete: () => {
                    if (!cancelled) pickerRef.current?.lookupAt(point);
                },
            });
        })();
        return () => { cancelled = true; };
    }, [open, fieldName, form]);

    const toggle = () => {
        if (!baiduAk) {
            void message.warning(t('components.addressPicker.akMissing'));
            return;
        }
        setOpen(prev => !prev);
    };

    const handleSelect = (item: AddressPickerItem) => {
        if (fieldName) form.setFieldValue(fieldName, item.address || item.title);
    };

    return (
        <>
            <Form.Item {...formItemProps}>
                <Input
                    {...inputProps}
                    suffix={
                        <Tooltip title={t('components.addressPicker.triggerTooltip')}>
                            <EnvironmentOutlined
                                className="cursor-pointer"
                                style={{color: open ? token.colorPrimary : '#9ca3af', fontSize: 18}}
                                onClick={toggle}
                            />
                        </Tooltip>
                    }
                />
            </Form.Item>
            {open && baiduAk && (
                <div
                    className="rounded-lg border border-slate-200 overflow-hidden mb-6"
                    style={{height: panelHeight, ...panelStyle}}
                >
                    <AddressPicker
                        ref={pickerRef}
                        mapType={GenericMapType.BAIDU}
                        mapSettings={{ak: baiduAk}}
                        initialProps={{
                            center: DEFAULT_CENTER,
                            heading: 0,
                            pitch: 0,
                            range: DEFAULT_RANGE,
                        }}
                        emptyText={t('components.addressPicker.emptyNearby')}
                        loadingText={t('components.addressPicker.loading')}
                        pinIcon={pinIcon}
                        onAddressSelect={handleSelect}
                    />
                </div>
            )}
        </>
    );
}
