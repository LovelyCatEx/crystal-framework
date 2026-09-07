export const WEBSOCKET_CHANNELS = {
  CRYSTAL_FRAMEWORK: 'crystal-framework',
  CRYSTAL_FRAMEWORK_PUB: 'crystal-framework-pub',
} as const;

export type WebSocketChannel = typeof WEBSOCKET_CHANNELS[keyof typeof WEBSOCKET_CHANNELS];

export const BUILTIN_WEBSOCKET_CHANNELS: readonly string[] = Object.values(WEBSOCKET_CHANNELS);
