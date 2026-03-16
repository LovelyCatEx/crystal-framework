import {ConfigProvider} from "antd";
import {Route, Routes} from "react-router-dom";
import {RequireAuthComponent} from "./components/RequireAuthComponent.tsx";
import {AuthorizationPage} from "./pages/auth/AuthorizationPage.tsx";
import {ManagerContainerPage} from "./pages/manager/ManagerContainerPage.tsx";
import {HomePage} from "./pages/home/HomePage.tsx";
import {TenantInvitationPage} from "@/pages/tenant/TenantInvitationPage.tsx";
import {NotFoundPage} from "@/pages/NotFoundPage.tsx";
import {useState, useEffect} from "react";
import {getStoredThemeKey, getThemeByKey, buildThemeConfig, updateThemeCSSVariables} from "@/global/theme-config.ts";
import type {ThemeColor} from "@/types/theme.types.ts";

function App() {
  const [currentTheme, setCurrentTheme] = useState<ThemeColor>(() => {
    const storedKey = getStoredThemeKey();
    return getThemeByKey(storedKey);
  });

  useEffect(() => {
    const handleStorageChange = (e: StorageEvent) => {
      if (e.key === 'app-theme-color-key' && e.newValue) {
        const theme = getThemeByKey(e.newValue);
        setCurrentTheme(theme);
        updateThemeCSSVariables(theme);
      }
    };
    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  }, []);

  useEffect(() => {
    updateThemeCSSVariables(currentTheme);
  }, [currentTheme]);

  const themeConfig = buildThemeConfig(currentTheme);

  return (
      <ConfigProvider theme={themeConfig}>
          <Routes>
              <Route path="/" element={<HomePage />} />
              <Route
                  path="/manager/*"
                  element={
                      <RequireAuthComponent>
                          <ManagerContainerPage parentPath="/manager" />
                      </RequireAuthComponent>
                  }
              />
              <Route path="/auth/*" element={<AuthorizationPage parentPath="/auth" />} />
              <Route path="/tenant/invitation" element={<TenantInvitationPage />} />
              <Route path="*" element={<NotFoundPage />} />
          </Routes>
      </ConfigProvider>
  )
}

export default App
