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
        { text: 'Project Structure', link: '/en/structure-introduction' },
        { text: 'Quick Start', link: '/en/quick-start' },
        {
          text: 'SDK Documentation',
          items: [
            { text: 'Package Scanner', link: '/en/sdk/package-scanner' },
            { text: 'System Settings', link: '/en/sdk/system-settings' },
            { text: 'System Permission', link: '/en/sdk/system-permission' },
            { text: 'Tenant Permission', link: '/en/sdk/tenant-permission' },
            { text: 'Mail Template', link: '/en/sdk/mail-template' },
          ]
        },
        {
          text: 'Contribution Docs',
          items: [

          ]
        },
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
