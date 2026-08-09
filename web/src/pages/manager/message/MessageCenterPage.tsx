import {NotificationCenter} from "@/components/notification/NotificationCenter.tsx";
import {useFillViewportHeight} from "@/compositions/use-fill-viewport-height.ts";

/**
 * Public "Message Center" page: the standalone, full-page home of the notification center
 * (announcements + conversations). The same {@link NotificationCenter} also renders inside the
 * header bell modal; this page is the menu-driven entry point under the personal section.
 *
 * The center is intentionally frameless so it embeds cleanly here; the header bell supplies its own
 * frame via the Modal.
 */
export default function MessageCenterPage() {
    // `-m-6` cancels the shared `.p-6` page padding for this page only, so the center sits flush to
    // the content area. Height is measured from this div's top to the viewport bottom, so it fills
    // whatever remains after the header / optional tab bar — no hard-coded offsets.
    const {ref, height} = useFillViewportHeight();
    return (
        <div ref={ref} className="-m-6" style={{height: height || undefined, minHeight: 360}}>
            <NotificationCenter fillParent/>
        </div>
    );
}
