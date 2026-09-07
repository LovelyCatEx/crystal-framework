/**
 * WebSocket message type definitions
 */

/**
 * WebSocket inbound message (client -> server)
 */
export interface WsInboundMessage {
  channel: string;
  action: string;
  payload?: Record<string, unknown>;
}

/**
 * WebSocket outbound message (server -> client)
 */
export interface WsOutboundMessage {
  channel: string;
  type: 'message' | 'error' | 'ack';
  payload: unknown;
}
