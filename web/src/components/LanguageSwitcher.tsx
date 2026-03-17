import { useEffect, useState } from 'react';
import { Dropdown, Button } from 'antd';
import { GlobalOutlined, CheckOutlined } from '@ant-design/icons';
import i18n from '@/i18n';

interface Language {
  key: string;
  label: string;
  flag: string;
}

const languages: Language[] = [
  { key: 'zh-CN', label: '简体中文', flag: '🇨🇳' },
  { key: 'en-US', label: 'English', flag: '🇺🇸' }
];

const LANGUAGE_STORAGE_KEY = 'i18nextLng';

export function LanguageSwitcher() {
  const [currentLang, setCurrentLang] = useState<string>(i18n.language || 'zh-CN');

  useEffect(() => {
    const storedLang = localStorage.getItem(LANGUAGE_STORAGE_KEY);
    if (storedLang && languages.some(lang => lang.key === storedLang)) {
      setCurrentLang(storedLang);
      if (i18n.language !== storedLang) {
        i18n.changeLanguage(storedLang);
      }
    }
  }, []);

  const handleLanguageChange = (langKey: string) => {
    setCurrentLang(langKey);
    i18n.changeLanguage(langKey);
    localStorage.setItem(LANGUAGE_STORAGE_KEY, langKey);
  };

  const currentLanguage = languages.find(lang => lang.key === currentLang) || languages[0];

  const items = languages.map(lang => ({
    key: lang.key,
    label: (
      <div className="flex items-center justify-between gap-4 min-w-[120px]">
        <span className="flex items-center gap-2">
          <span className="text-lg">{lang.flag}</span>
          <span>{lang.label}</span>
        </span>
        {currentLang === lang.key && (
          <CheckOutlined className="text-primary text-sm" />
        )}
      </div>
    ),
    onClick: () => handleLanguageChange(lang.key)
  }));

  return (
    <Dropdown
      menu={{ items }}
      placement="bottomRight"
      trigger={['click']}
      overlayClassName="language-switcher-dropdown"
    >
      <Button
        type="text"
        icon={<GlobalOutlined />}
      >
        {currentLanguage.label}
      </Button>
    </Dropdown>
  );
}
