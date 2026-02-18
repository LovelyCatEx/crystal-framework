import {ConfigProvider} from "antd";
import {Route, Routes} from "react-router-dom";
import {RequireAuthComponent} from "./components/RequireAuthComponent.tsx";
import {AuthorizationPage} from "./pages/auth/AuthorizationPage.tsx";
import {ManagerContainerPage} from "./pages/manager/ManagerContainerPage.tsx";

function App() {
  return (
      <ConfigProvider
          theme={{
              token: {
                  colorPrimary: '#FF8DA1',
                  borderRadius: 12,
                  fontFamily: 'Inter, system-ui, sans-serif',
              },
              components: {
                  Layout: {
                      headerBg: 'rgba(255, 255, 255, 0.7)',
                      siderBg: '#ffffff',
                  },
                  Menu: {
                      itemBorderRadius: 12,
                      itemSelectedBg: 'rgba(255,240,243,0.8)',
                      itemSelectedColor: '#FF8DA1',
                  },
              },
          }}
      >
          <Routes>
              <Route path="/" element={<div>Nothing Here</div>} />
              <Route
                  path="/manager/*"
                  element={
                      <RequireAuthComponent>
                          <ManagerContainerPage parentPath="/manager" />
                      </RequireAuthComponent>
                  }
              />
              <Route path="/auth/*" element={<AuthorizationPage parentPath="/auth" />} />
          </Routes>
      </ConfigProvider>
  )
}

export default App
