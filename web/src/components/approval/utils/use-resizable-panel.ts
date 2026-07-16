import {useCallback, useRef, useState} from "react";
import type React from "react";

const DEFAULT_MIN_PERCENT = 20;
const DEFAULT_MAX_PERCENT = 60;

export interface ResizablePanelBounds {
    minPercent?: number;
    maxPercent?: number;
}

/**
 * Manages a horizontally resizable panel whose width is a percentage of the
 * viewport. Returns the current width (percent) plus a mousedown handler that
 * the caller attaches to a drag handle element. All mousemove/mouseup
 * listeners are internal.
 */
export function useResizablePanel(initialPercent: number, bounds: ResizablePanelBounds = {}) {
    const {minPercent = DEFAULT_MIN_PERCENT, maxPercent = DEFAULT_MAX_PERCENT} = bounds;
    const [width, setWidth] = useState(initialPercent);
    const isResizing = useRef(false);

    const handleResizeStart = useCallback((e: React.MouseEvent) => {
        e.preventDefault();
        isResizing.current = true;
        const startX = e.clientX;
        const startWidth = width;

        const onMouseMove = (ev: MouseEvent) => {
            if (!isResizing.current) return;
            const containerWidth = document.body.clientWidth;
            const delta = startX - ev.clientX;
            const newWidth = Math.max(minPercent, Math.min(maxPercent, startWidth + (delta / containerWidth) * 100));
            setWidth(newWidth);
        };

        const onMouseUp = () => {
            isResizing.current = false;
            document.removeEventListener('mousemove', onMouseMove);
            document.removeEventListener('mouseup', onMouseUp);
        };

        document.addEventListener('mousemove', onMouseMove);
        document.addEventListener('mouseup', onMouseUp);
    }, [width, minPercent, maxPercent]);

    return {width, handleResizeStart};
}
