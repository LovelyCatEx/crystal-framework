import {defineConfig, loadEnv} from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'
import {fileURLToPath} from 'url'
import copy from 'rollup-plugin-copy'

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
      copy({
        targets: [
          {src: 'node_modules/@baidumap/mapv-three/dist/assets', dest: 'public/mapvthree'},
        ],
        verbose: true,
        hook: 'buildStart',
      }),
    ],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
    build: {
      rollupOptions: {
        maxParallelFileOps: 10,
        output: {
          manualChunks(id: string) {
            console.log(`Chucking: ${id}`);
            if (id.includes('node_modules')) {
              if (id.includes('echarts') || id.includes('zrender')) {
                return 'echarts';
              } else if (id.includes('antd')) {
                return 'antd';
              } else if (id.includes('ant-design+icons-svg')) {
                return 'antd-icons';
              }

              return 'vendor';
            } else if (id.includes('src/')) {
              if (id.includes('src/i18n/locales')) {
                const match = id.match(/locales\/([^/]+)\./);
                const lang = match ? match[1] : 'unknown';
                return `lang-${lang}`;
              }

              return 'crystal-sources';
            }
          }
        }
      },
      sourcemap: false,
      minify: 'terser',
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
