import {defineConfig} from 'vitepress'
import {enThemeConfig, zhThemeConfig} from './i18n'

export default defineConfig({
  title: "CrystalFramework Docs",
  description: "A modern front-end and back-end framework based on kotlin-springboot4 webflux r2dbc.",
  base: '/crystal-framework/',
  lastUpdated: true,
  themeConfig: {
    logo: '/logo.svg',
    socialLinks: [
      { icon: 'github', link: 'https://github.com/LovelyCatEx/crystal-framework' }
    ],
    editLink: {
      pattern: 'https://github.com/LovelyCatEx/crystal-framework/edit/develop/docs/:path'
    }
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
    hostname: 'https://lovelycatex.github.io/crystal-framework'
  },
  markdown: {
    lineNumbers: true
  }
})
