import { defineConfig } from 'vitepress'
import { zhThemeConfig, enThemeConfig } from './i18n'

export default defineConfig({
  title: "CrystalFramework Docs",
  description: "A modern front-end and back-end framework based on kotlin-springboot4 webflux r2dbc.",
  themeConfig: {
    socialLinks: [
      { icon: 'github', link: 'https://github.com/LovelyCatEx/crystal-framework' }
    ]
  },
  locales: {
    root: {
      label: '简体中文',
      lang: 'zh-cn',
      themeConfig: zhThemeConfig
    },
    en: {
      label: 'English',
      lang: 'en',
      link: '/en',
      themeConfig: enThemeConfig
    }
  },
  sitemap: {
    hostname: 'http://localhost:5174'
  }
})
