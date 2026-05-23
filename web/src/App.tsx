import {ConfigProvider, theme} from "antd";
import {Route, Routes} from "react-router-dom";
import {RequireAuthComponent} from "./components/RequireAuthComponent.tsx";
import type {MaintenanceStatus} from "./components/MaintenanceGuard.tsx";
import {MaintenanceGuard} from "./components/MaintenanceGuard.tsx";
import {AuthorizationPage} from "./pages/auth/AuthorizationPage.tsx";
import {ManagerContainerPage} from "./pages/manager/ManagerContainerPage.tsx";
import {HomePage} from "./pages/home/HomePage.tsx";
import {TenantInvitationPage} from "@/pages/tenant/TenantInvitationPage.tsx";
import {NotFoundPage} from "@/pages/NotFoundPage.tsx";
import {useEffect, useState} from "react";
import {
    buildThemeConfig,
    getStoredThemeKey,
    getStoredThemeMode,
    getThemeByKey,
    THEME_MODE_STORAGE_KEY,
    updateThemeCSSVariables
} from "@/global/theme-config.ts";
import type {ThemeColor, ThemeMode} from "@/types/theme.types.ts";
import {SystemInitializePage} from "@/pages/SystemIntializePage.tsx";
import {SystemIntegratedProvider, useSystemIntegrated} from "@/contexts/SystemIntegratedContext.tsx";
import {pluginRegistry} from "@/plugin/registry.ts";
import './App.css';

function AppRoutes() {
    return (
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
            <Route path="/system-initialize" element={<SystemInitializePage />} />
            {pluginRegistry.topLevelRoutes.map(route => (
                <Route key={route.path} path={route.path} element={route.element} />
            ))}
            <Route path="*" element={<NotFoundPage />} />
        </Routes>
    );
}

function AppContent() {
    const {maintenanceInfo, isLoading, error} = useSystemIntegrated();

    // Calculate maintenance status based on integrated info
    const maintenanceStatus: MaintenanceStatus = (() => {
        if (isLoading) return 'loading';
        if (error) return 'disconnected';
        if (maintenanceInfo?.maintenanceMode && !maintenanceInfo?.canAccess) return 'maintenance';
        return 'pass';
    })();

    return (
        <MaintenanceGuard mode="fromData" status={maintenanceStatus}>
            <AppRoutes />
        </MaintenanceGuard>
    );
}

function App() {
  const [currentTheme, setCurrentTheme] = useState<ThemeColor>(() => {
    const storedKey = getStoredThemeKey();
    return getThemeByKey(storedKey);
  });

  const [themeMode, setThemeMode] = useState<ThemeMode>(() => {
    return getStoredThemeMode();
  });

  useEffect(() => {
    const handleStorageChange = (e: StorageEvent) => {
      if (e.key === 'app-theme-color-key' && e.newValue) {
        const theme = getThemeByKey(e.newValue);
        setCurrentTheme(theme);
        updateThemeCSSVariables(theme, themeMode);
      }
      if (e.key === THEME_MODE_STORAGE_KEY && e.newValue) {
        setThemeMode(e.newValue as ThemeMode);
      }
    };
    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  }, [themeMode]);

  useEffect(() => {
    updateThemeCSSVariables(currentTheme, themeMode);
  }, [currentTheme, themeMode]);

  const themeConfig = {
    ...buildThemeConfig(currentTheme, themeMode),
    algorithm: themeMode === 'dark' ? theme.darkAlgorithm : theme.defaultAlgorithm,
  };

  return (
      <ConfigProvider theme={themeConfig}>
          <SystemIntegratedProvider>
              <AppContent />
          </SystemIntegratedProvider>
      </ConfigProvider>
  )
}

export default App
