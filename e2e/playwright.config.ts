import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  timeout: 30000,
  retries: 2,
  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:4001',
  },
  reporter: [['junit', { outputFile: 'test-results/results.xml' }]],
});
