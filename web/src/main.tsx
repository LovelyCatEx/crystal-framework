import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import './index.css'
import './i18n'
import App from "./App.tsx";
import {BrowserRouter} from "react-router-dom";
import {registerAllKotlinModules} from "kotlin-ts";
import {ProtectedApp} from "@/ProtectedApp.tsx";

registerAllKotlinModules()

createRoot(document.getElementById('root')!).render(
  <StrictMode>
      <BrowserRouter>
          <ProtectedApp>
              <App />
          </ProtectedApp>
      </BrowserRouter>
  </StrictMode>,
)
