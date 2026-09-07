import { useEffect, useRef, useState } from 'react';
import { getAccessToken } from '@/utils/token';

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

  // Update channelName ref
  useEffect(() => {
    channelNameRef.current = channelName;
  }, [channelName]);

  useEffect(() => {
    // Build WebSocket URL
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.host;

    let url = `${protocol}//${host}/api/v1/ws`;

    // Anonymous mode does not pass token
    if (!options?.anonymous) {
      const token = getAccessToken();
      if (token) {
        url += `?token=${encodeURIComponent(token)}`;
      }
    }

    const ws = new WebSocket(url);

    ws.onopen = () => {
      setConnected(true);
      options?.onConnected?.();

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
      options?.onDisconnected?.();
    };

    ws.onerror = (error) => {
      console.error('WebSocket error:', error);
      options?.onError?.(error);
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
  }, [channelName, options?.anonymous]);

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
