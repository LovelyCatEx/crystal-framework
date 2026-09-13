import {useEffect, useState} from "react";

export type DeviceType = "mobile" | "tablet" | "desktop";

/** Tailwind `md` breakpoint (768px) — below this is mobile, matching ManagerContainerPage's `md:hidden`. */
const MOBILE_MEDIA_QUERY = "(max-width: 767px)";
/** Tailwind `lg` breakpoint (1024px) — tablet sits between `md` and `lg`. */
const DESKTOP_MEDIA_QUERY = "(min-width: 1024px)";

function resolveDeviceType(): DeviceType {
    if (typeof window === "undefined") return "desktop";
    if (window.matchMedia(MOBILE_MEDIA_QUERY).matches) return "mobile";
    if (window.matchMedia(DESKTOP_MEDIA_QUERY).matches) return "desktop";
    return "tablet";
}

/**
 * Reports the current viewport device class: `mobile` (< 768px), `tablet` (768–1023px) or
 * `desktop` (≥ 1024px). The initial value is resolved synchronously so the first render is already
 * correct (no desktop flash on phones).
 */
export function useDeviceType(): DeviceType {
    const [deviceType, setDeviceType] = useState<DeviceType>(() => resolveDeviceType());

    useEffect(() => {
        const mobileQuery = window.matchMedia(MOBILE_MEDIA_QUERY);
        const desktopQuery = window.matchMedia(DESKTOP_MEDIA_QUERY);
        const handleChange = () => setDeviceType(resolveDeviceType());
        mobileQuery.addEventListener("change", handleChange);
        desktopQuery.addEventListener("change", handleChange);
        return () => {
            mobileQuery.removeEventListener("change", handleChange);
            desktopQuery.removeEventListener("change", handleChange);
        };
    }, []);

    return deviceType;
}
