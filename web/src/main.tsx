/*
 * Copyright (c) 2026 lovelycat
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

import {StrictMode} from 'react'
import {createRoot} from 'react-dom/client'
import './index.css'
import {bootstrapI18n} from './i18n'
import App from "./App.tsx";
import {BrowserRouter} from "react-router-dom";
import {ProtectedApp} from "@/ProtectedApp.tsx";
import {loadPlugins} from "@/plugin/loader.ts";

loadPlugins()

bootstrapI18n().then(() => {
  createRoot(document.getElementById('root')!).render(
    <StrictMode>
        <BrowserRouter>
            <ProtectedApp>
                <App />
            </ProtectedApp>
        </BrowserRouter>
    </StrictMode>
  )
})
