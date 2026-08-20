import {useCallback, useLayoutEffect, useRef, useState} from "react";

/**
 * Measures the available height from an element's top edge to the bottom of the viewport, so a panel
 * can fill the remaining space without hard-coding header / tab-bar offsets. The element's
 * `getBoundingClientRect().top` already accounts for whatever sits above it (fixed header, optional
 * tab bar, page padding), so the result stays correct when those toggle or resize.
 *
 * Returns a ref to attach to the container and the computed height in px (0 until first measure).
 * [bottomGap] leaves a little breathing room below (default 0).
 */
export function useFillViewportHeight(bottomGap: number = 0) {
    const ref = useRef<HTMLDivElement>(null);
    const [height, setHeight] = useState(0);

    const measure = useCallback(() => {
        const el = ref.current;
        if (!el) return;
        const top = el.getBoundingClientRect().top;
        setHeight(Math.max(0, window.innerHeight - top - bottomGap));
    }, [bottomGap]);

    useLayoutEffect(() => {
        measure();
        window.addEventListener('resize', measure);
        // The tab bar can toggle / the sider can collapse without a window resize; a ResizeObserver on
        // the document body catches those layout shifts too.
        const observer = new ResizeObserver(measure);
        observer.observe(document.body);
        return () => {
            window.removeEventListener('resize', measure);
            observer.disconnect();
        };
    }, [measure]);

    return {ref, height};
}
