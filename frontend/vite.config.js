import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  // 配置API代理，解决跨域问题
  server: {
    host: '0.0.0.0', // 监听所有网络接口，允许通过本地IP访问
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 不需要重写路径，因为后端控制器确实使用了/api前缀
        rewrite: (path) => path
      }
    }
  }
})
