import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    minify: 'esbuild',
    rollupOptions: {
      output: {
        manualChunks: {
          vue: ['vue', 'vue-router'],
          element: ['element-plus'],
          icons: ['@element-plus/icons-vue', 'lucide-vue-next'],
          axios: ['axios']
        }
      }
    }
  },
  server: {
    host: '127.0.0.1',
    port: 5174,
    proxy: {
      '/campus': {
        target: 'http://127.0.0.1:8084',
        changeOrigin: true
      },
      '/login': {
        target: 'http://127.0.0.1:8084',
        changeOrigin: true,
        bypass(req) {
          if (req.method === 'GET') {
            return '/index.html';
          }
        }
      },
      '/logout': {
        target: 'http://127.0.0.1:8084',
        changeOrigin: true
      },
      '/mail': {
        target: 'http://127.0.0.1:8084',
        changeOrigin: true
      },
      '/user': {
        target: 'http://127.0.0.1:8084',
        changeOrigin: true
      },
      '/img': {
        target: 'http://127.0.0.1:8084',
        changeOrigin: true
      }
    }
  }
});
