import i18n from 'i18next';
import {initReactI18next} from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

type LangEntry = { loader: () => Promise<unknown>; exportKey: string };

const langRegistry: Record<string, LangEntry> = {
  'zh-CN': { loader: () => import('./locales/zh-CN'), exportKey: 'zhCN' },
  'en-US': { loader: () => import('./locales/en-US'), exportKey: 'enUS' },
};

const loadedLanguages = new Set<string>();

export async function loadLanguageBundle(lang: string): Promise<void> {
  if (loadedLanguages.has(lang)) return;

  const entry = langRegistry[lang];
  if (!entry) {
    console.warn(`Unknown language "${lang}", falling back to zh-CN`);
    return loadLanguageBundle('zh-CN');
  }

  const module = await entry.loader();
  const translations = (module as Record<string, unknown>)[entry.exportKey];
  if (translations) {
    i18n.addResourceBundle(lang, 'translation', translations, false, true);
    loadedLanguages.add(lang);
  }
}

export async function bootstrapI18n(): Promise<typeof i18n> {
  await i18n
    .use(LanguageDetector)
    .use(initReactI18next)
    .init({
      fallbackLng: 'zh-CN',
      interpolation: {
        escapeValue: false
      }
    });

  const detected = i18n.language || 'zh-CN';
  const lang = langRegistry[detected] ? detected : 'zh-CN';
  await loadLanguageBundle(lang);
  if (lang !== 'zh-CN') {
    await loadLanguageBundle('zh-CN');
  }
  await i18n.changeLanguage(lang);

  return i18n;
}

export default i18n;
