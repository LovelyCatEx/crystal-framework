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
  ]
}
