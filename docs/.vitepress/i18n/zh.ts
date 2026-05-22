import type { DefaultTheme } from 'vitepress'

export const zhThemeConfig: DefaultTheme.Config = {
  nav: [
    { text: '首页', link: '/' },
  ],
  sidebar: [
    {
      text: '开发指南',
      items: [
        { text: '开发指引', link: '/develop-guide' },
      ]
    },
    {
      text: '部署指南',
      items: [
        { text: '部署指引', link: '/deploy-guide' },
      ]
    }
  ]
}
