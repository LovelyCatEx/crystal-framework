import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const apiDomain = env.VITE_API_BASE_URL || 'http://localhost:8080'

  console.log("================================================================")
  console.log("Mode", mode)
  console.log("Environment", {
    apiDomain: apiDomain
  })
  console.log("================================================================")

  return {
    plugins: [
      react({
        babel: {
          plugins: [['babel-plugin-react-compiler']],
        },
      }),
    ],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
    server: {
      proxy: {
        '/api': {
          target: `${apiDomain}/api/v1`,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, ''),
        },
        '/raw': {
          target: apiDomain,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/raw/, ''),
        },
      }
    }
  }
})
