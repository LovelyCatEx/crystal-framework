import type { DefaultTheme } from 'vitepress'

export const enThemeConfig: DefaultTheme.Config = {
  nav: [
    { text: 'Home', link: '/en' },
  ],
  sidebar: [
    {
      text: 'Plugin Dev',
      items: [
        { text: 'Quick Start', link: '/en/develop/quick-start' },
        { text: 'Development Guide', link: '/en/develop/develop-guide' },
        {
          text: 'SDK Documentation',
          items: [
            { text: 'System Settings', link: '/en/develop/sdk/system-settings' },
            { text: 'System Permission', link: '/en/develop/sdk/system-permission' },
            { text: 'Tenant Permission', link: '/en/develop/sdk/tenant-permission' },
            { text: 'Mail Template', link: '/en/develop/sdk/mail-template' },
          ]
        },
      ]
    },
    {
      text: 'Source Contribution',
      items: [
        { text: 'Quick Start', link: '/en/contribute/quick-start' },
        { text: 'Project Structure', link: '/en/contribute/structure-introduction' },
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
