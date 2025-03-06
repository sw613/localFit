import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],

  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,   // 백엔드 서버에서 올바른 호스트 헤더를 받을 수 있도록 함
        secure: false,        // HTTPS가 아닌 경우 false 설정

      },
    },
  },
})
