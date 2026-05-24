import type {DefaultTheme} from 'vitepress'

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
          text: 'Frontend',
          items: [
            { text: 'Create Plugin', link: '/en/develop/frontend/create-plugin' },
            { text: 'Add Page', link: '/en/develop/frontend/add-page' },
            { text: 'API Integration', link: '/en/develop/frontend/api-integration' },
          ]
        },
        {
          text: 'Database',
          items: [
            { text: 'Add Entity', link: '/en/develop/add-entity' },
            { text: 'Database Migration', link: '/en/develop/db-migration' },
          ]
        },
        {
          text: 'Controllers',
          items: [
            { text: 'ApiResponse', link: '/en/develop/controller/api-response' },
            { text: 'Generic Controller', link: '/en/develop/controller/generic-controller' },
            { text: 'StandardManagerController', link: '/en/develop/controller/standard-manager-controller' },
            { text: 'ReadOnly ManagerController', link: '/en/develop/controller/readonly-manager-controller' },
            { text: 'Exception Handling', link: '/en/develop/controller/exception-handling' },
          ]
        },
        { text: 'System Settings', link: '/en/develop/sdk/system-settings' },
        { text: 'System Permission', link: '/en/develop/sdk/system-permission' },
        { text: 'Tenant Permission', link: '/en/develop/sdk/tenant-permission' },
        { text: 'Mail Template', link: '/en/develop/sdk/mail-template' },
      ]
    },
    {
      text: 'Source Contribution',
      items: [
        { text: 'Quick Start', link: '/en/contribute/quick-start' },
        { text: 'Project Structure', link: '/en/contribute/structure-introduction' },
        {
          text: 'Database',
          items: [
            { text: 'Add Entity', link: '/en/contribute/add-entity' },
            { text: 'Database Migration', link: '/en/contribute/db-migration' },
          ]
        },
        {
          text: 'Controllers',
          items: [
            { text: 'ApiResponse', link: '/en/contribute/controller/api-response' },
            { text: 'Generic Controller', link: '/en/contribute/controller/generic-controller' },
            { text: 'StandardManagerController', link: '/en/contribute/controller/standard-manager-controller' },
            { text: 'ReadOnly ManagerController', link: '/en/contribute/controller/readonly-manager-controller' },
            { text: 'Exception Handling', link: '/en/contribute/controller/exception-handling' },
          ]
        },
        { text: 'System Settings', link: '/en/contribute/system-settings' },
        { text: 'System Permission', link: '/en/contribute/system-permission' },
        { text: 'Tenant Permission', link: '/en/contribute/tenant-permission' },
        { text: 'Mail Template', link: '/en/contribute/mail-template' },
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
