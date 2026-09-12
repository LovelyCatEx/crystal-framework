import { useEffect, useRef, useState } from 'react';
import { getUserAuthentication } from '@/utils/token.utils';

/**
 * WebSocket message structure
 */
export interface WsMessage {
  channel: string;
  type: 'message' | 'error' | 'ack';
  payload: unknown;
}

/**
 * Hook options
 */
export interface UseWebSocketConnectionOptions {
  /**
   * Whether to use anonymous mode (no token)
   * @default false
   */
  anonymous?: boolean;

  /**
   * Whether to enable the connection
   * @default true
   */
  enabled?: boolean;

  /**
   * Callback when connected
   */
  onConnected?: () => void;

  /**
   * Callback when disconnected
   */
  onDisconnected?: () => void;

  /**
   * Error callback
   */
  onError?: (error: Event) => void;
}

/**
 * WebSocket connection hook
 *
 * @param channelName Channel name (corresponds to backend Handler's channelName)
 * @param options Hook options
 * @returns Connection state, latest message, send function
 *
 * @example
 * // Authenticated user
 * const { connected, lastMessage, send } = useWebSocketConnection('authenticated-chat');
 *
 * @example
 * // Anonymous access
 * const { connected, lastMessage, send } = useWebSocketConnection('public-announcement', { anonymous: true });
 */
export function useWebSocketConnection(
  channelName: string,
  options?: UseWebSocketConnectionOptions
) {
  const [connected, setConnected] = useState(false);
  const [lastMessage, setLastMessage] = useState<WsMessage | null>(null);
  const wsRef = useRef<WebSocket | null>(null);
  const channelNameRef = useRef(channelName);

  // Extract stable values from options
  const enabled = options?.enabled;
  const anonymous = options?.anonymous;

  // Use refs for callbacks to avoid re-creating WebSocket on callback changes
  const onConnectedRef = useRef(options?.onConnected);
  const onDisconnectedRef = useRef(options?.onDisconnected);
  const onErrorRef = useRef(options?.onError);

  // Update refs when callbacks change
  useEffect(() => {
    onConnectedRef.current = options?.onConnected;
    onDisconnectedRef.current = options?.onDisconnected;
    onErrorRef.current = options?.onError;
  }, [options?.onConnected, options?.onDisconnected, options?.onError]);

  // Update channelName ref
  useEffect(() => {
    channelNameRef.current = channelName;
  }, [channelName]);

  useEffect(() => {
    // Only connect if enabled
    if (enabled === false) {
      return;
    }

    // Build WebSocket URL
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.host;

    let url = `${protocol}//${host}/ws`;

    // Anonymous mode does not pass token
    if (!anonymous) {
      const auth = getUserAuthentication();
      if (auth?.token) {
        url += `?token=${encodeURIComponent(auth.token)}`;
      }
    }

    const ws = new WebSocket(url);

    ws.onopen = () => {
      setConnected(true);
      onConnectedRef.current?.();

      // Auto subscribe to channel
      ws.send(JSON.stringify({
        channel: channelNameRef.current,
        action: 'subscribe'
      }));
    };

    ws.onmessage = (event) => {
      try {
        const msg: WsMessage = JSON.parse(event.data);

        // Only handle messages from current channel
        if (msg.channel === channelNameRef.current || msg.channel === 'system') {
          setLastMessage(msg);
        }
      } catch (error) {
        console.error('Failed to parse WebSocket message:', error);
      }
    };

    ws.onclose = () => {
      setConnected(false);
      onDisconnectedRef.current?.();
    };

    ws.onerror = (error) => {
      console.error('WebSocket error:', error);
      onErrorRef.current?.(error);
      setConnected(false);
    };

    wsRef.current = ws;

    return () => {
      // Unsubscribe
      if (ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({
          channel: channelNameRef.current,
          action: 'unsubscribe'
        }));
      }
      ws.close();
    };
  }, [channelName, enabled, anonymous]);

  /**
   * Send message to current channel
   *
   * @param payload Business data
   */
  const send = (payload: unknown) => {
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      wsRef.current.send(JSON.stringify({
        channel: channelNameRef.current,
        action: 'send',
        payload
      }));
    } else {
      console.warn('WebSocket is not connected');
    }
  };

  return {
    connected,
    lastMessage,
    send
  };
}
