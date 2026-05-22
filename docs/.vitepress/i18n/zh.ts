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
        { text: '项目结构', link: '/structure-introduction' },
        { text: '快速开始', link: '/quick-start' },
      ]
    },
    {
      text: '部署指南',
      items: [
        { text: '部署指引', link: '/deploy-guide' },
      ]
    }
  ],
  footer: {
    message: '以 MIT License 发布',
    copyright: 'Copyright © 2025-2026 LovelyCat.'
  },
  search: {
    provider: 'local'
  },
}
