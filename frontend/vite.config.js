import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const appName = env.VITE_APP_NAME || 'SprintStack'
  return {
    plugins: [react(), {
      name: 'html-app-name',
      transformIndexHtml: {
        order: 'pre',
        handler(html) { return html.replaceAll('%VITE_APP_NAME%', appName) },
      },
    }],
    server: { port: 5173 },
  }
})
