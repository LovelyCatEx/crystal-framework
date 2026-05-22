import type { DefaultTheme } from 'vitepress'

export const enThemeConfig: DefaultTheme.Config = {
  nav: [
    { text: 'Home', link: '/en' },
  ],
  sidebar: [
    {
      text: 'Develop',
      items: [
        { text: 'Develop Guidance', link: '/en/develop-guide' },
      ]
    },
    {
      text: 'Deploy',
      items: [
        { text: 'Deploy Guidance', link: '/en/deploy-guide' },
      ]
    }
  ],
  footer: {
    message: 'Released under the MIT License.',
    copyright: 'Copyright © 2025-2026 LovelyCat.'
  },
  search: {
    provider: 'local'
  },
}
