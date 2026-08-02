import {useEffect, useState} from "react";
import {Modal, Typography} from "antd";
import i18n from "@/i18n";
import type {RateLimitContext} from "@/types/common/forbidden.types.ts";

const {Text} = Typography;

/**
 * Live countdown body for the rate-limit modal. Ticks retryAfterSeconds down
 * once per second and invokes onFinished when it reaches zero so the caller can
 * auto-close the modal. Purely informational — the server lockout is the real
 * gate, this only tells the user roughly how long to wait.
 */
function RateLimitCountdown({initialSeconds, serverMessage, onFinished}: {
    initialSeconds: number;
    serverMessage?: string;
    onFinished: () => void;
}) {
    const [remaining, setRemaining] = useState(initialSeconds);

    useEffect(() => {
        if (remaining <= 0) {
            onFinished();
            return;
        }
        const timer = setInterval(() => setRemaining((s) => s - 1), 1000);
        return () => clearInterval(timer);
    }, [remaining, onFinished]);

    return (
        <div style={{marginTop: 8}}>
            <Text>{i18n.t('api.rateLimitModal.retryAfter', {seconds: remaining})}</Text>
            {serverMessage && (
                <div style={{marginTop: 8}}>
                    <Text type="secondary" style={{whiteSpace: 'pre-wrap'}}>{serverMessage}</Text>
                </div>
            )}
        </div>
    );
}

/**
 * Dialog shown when the login rate limiter rejects a request (429 +
 * RateLimitContext). Displays a live countdown of the remaining wait derived
 * from context.retryAfterSeconds and auto-closes when it hits zero.
 */
export function showRateLimitModal(context: RateLimitContext, serverMessage?: string) {
    const initialSeconds = Number(context.retryAfterSeconds);
    const instance = Modal.warning({
        title: i18n.t('api.rateLimitModal.title'),
        content: (
            <RateLimitCountdown
                initialSeconds={initialSeconds}
                serverMessage={serverMessage}
                onFinished={() => instance.destroy()}
            />
        ),
        width: 460,
    });
}
