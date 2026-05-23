import type {DefaultTheme} from 'vitepress'

export const zhThemeConfig: DefaultTheme.Config = {
  nav: [
    { text: '首页', link: '/' },
  ],
  sidebar: [
    {
      text: '二次开发',
      items: [
        { text: '快速开始', link: '/develop/quick-start' },
        { text: '开发指引', link: '/develop/develop-guide' },
        {
          text: '数据库',
          items: [
            { text: '添加实体类', link: '/develop/add-entity' },
            { text: '数据库迁移', link: '/develop/db-migration' },
          ]
        },
        { text: '系统设置项', link: '/develop/sdk/system-settings' },
        { text: '系统权限', link: '/develop/sdk/system-permission' },
        { text: '租户权限', link: '/develop/sdk/tenant-permission' },
        { text: '邮件模板', link: '/develop/sdk/mail-template' },
      ]
    },
    {
      text: '源码贡献',
      items: [
        { text: '快速开始', link: '/contribute/quick-start' },
        { text: '项目结构', link: '/contribute/structure-introduction' },
        {
          text: '数据库',
          items: [
            { text: '添加实体类', link: '/contribute/add-entity' },
            { text: '数据库迁移', link: '/contribute/db-migration' },
          ]
        },
        { text: '系统设置项', link: '/contribute/system-settings' },
        { text: '系统权限', link: '/contribute/system-permission' },
        { text: '租户权限', link: '/contribute/tenant-permission' },
        { text: '邮件模板', link: '/contribute/mail-template' },
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
