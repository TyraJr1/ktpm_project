import { chromium } from 'playwright-core';
import { mkdir } from 'node:fs/promises';

const browser = await chromium.launch({
  executablePath: process.env.BROWSER_PATH
    || 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe',
  headless: true,
});

const context = await browser.newContext({
  viewport: { width: 1440, height: 1000 },
  deviceScaleFactor: 1,
});
const page = await context.newPage();

await mkdir('test-results', { recursive: true });
await page.goto('http://127.0.0.1:5173/login');
await page.screenshot({ path: 'test-results/task008-login-desktop.png', fullPage: true });

await page.evaluate(() => {
  localStorage.setItem('ktpm.auth.v1', JSON.stringify({
    accessToken: 'visual-check',
    tokenType: 'Bearer',
    userId: 1,
    username: 'admin',
    fullName: 'Quản trị viên',
    role: 'ADMIN',
  }));
});

await page.route('http://localhost:8080/**', async (route) => {
  const url = new URL(route.request().url());
  let body;
  if (url.pathname.endsWith('/categories')) body = [{ id: 1 }];
  else if (url.pathname.endsWith('/ping')) body = { message: 'ok' };
  else body = { content: [], totalElements: 12, totalPages: 1, size: 10, number: 0 };

  await route.fulfill({
    status: 200,
    contentType: 'application/json',
    body: JSON.stringify(body),
  });
});

await page.goto('http://127.0.0.1:5173/admin/dashboard');
await page.waitForTimeout(500);
await page.screenshot({ path: 'test-results/task008-dashboard-desktop.png', fullPage: true });

await page.goto('http://127.0.0.1:5173/admin/accounts');
await page.waitForTimeout(400);
await page.screenshot({ path: 'test-results/task008-accounts-desktop.png', fullPage: true });

await page.setViewportSize({ width: 390, height: 844 });
await page.goto('http://127.0.0.1:5173/admin/dashboard');
await page.waitForTimeout(300);
await page.screenshot({ path: 'test-results/task008-dashboard-mobile.png', fullPage: true });

await browser.close();
console.log('TASK-008 visual screenshots created.');
