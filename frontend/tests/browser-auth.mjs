import { chromium } from 'playwright-core';
import { mkdir } from 'node:fs/promises';

const baseUrl = process.env.FRONTEND_URL || 'http://localhost:5173';
const password = process.env.DEMO_PASSWORD;
const executablePath = process.env.BROWSER_PATH
  || 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';

if (!password) {
  throw new Error('DEMO_PASSWORD is required for browser integration tests.');
}

const browser = await chromium.launch({ executablePath, headless: true });
const context = await browser.newContext({ baseURL: baseUrl });
const page = await context.newPage();
const failures = [];
const results = [];
await mkdir('test-results', { recursive: true });

page.on('pageerror', (error) => failures.push(`Page error: ${error.message}`));
page.on('console', (message) => {
  const text = message.text();
  const expectedAuthFailure = text.includes('Failed to load resource') && (text.includes('401') || text.includes('403'));
  if (message.type() === 'error' && !expectedAuthFailure) failures.push(`Console error: ${text}`);
});

function pass(name) {
  results.push(`PASS - ${name}`);
}

async function expect(condition, message) {
  if (!condition) throw new Error(message);
}

async function resetAuth() {
  await page.goto('/login');
  await page.evaluate(() => localStorage.clear());
  await page.reload();
}

async function login(username) {
  await page.goto('/login');
  await page.getByLabel('Tên đăng nhập').fill(username);
  await page.getByLabel('Mật khẩu', { exact: true }).fill(password);
  const responsePromise = page.waitForResponse((response) => (
    response.url().endsWith('/api/auth/login') && response.request().method() === 'POST'
  ));
  await page.getByRole('button', { name: 'Đăng nhập' }).click();
  const response = await responsePromise;
  await expect(response.status() === 200, `${username} login returned ${response.status()}`);
  return response;
}

try {
  await resetAuth();
  await expect(await page.getByRole('heading', { name: 'Đăng nhập hệ thống' }).isVisible(), 'Login page is not visible.');
  await page.screenshot({ path: 'test-results/login-page.png', fullPage: true });
  pass('Login page opens');

  await page.getByRole('button', { name: 'Đăng nhập' }).click();
  await expect(await page.getByText('Tên đăng nhập không được để trống.').isVisible(), 'Username validation missing.');
  await expect(await page.getByText('Mật khẩu không được để trống.').isVisible(), 'Password validation missing.');
  pass('Required-field validation');

  await page.getByLabel('Tên đăng nhập').fill('admin01');
  await page.getByLabel('Mật khẩu', { exact: true }).fill('wrong-password');
  const wrongResponse = page.waitForResponse((response) => response.url().endsWith('/api/auth/login'));
  await page.getByRole('button', { name: 'Đăng nhập' }).click();
  await expect((await wrongResponse).status() === 401, 'Wrong-password response is not 401.');
  await page.getByText('Tên đăng nhập hoặc mật khẩu không chính xác.').waitFor({ state: 'visible' });
  pass('Wrong password displays an error');

  await login('admin01');
  await page.waitForURL('**/admin/dashboard');
  await expect(await page.getByText('Xin chào, Quản trị viên').isVisible(), 'Professional Admin greeting missing.');
  await expect(await page.getByText('Quản trị viên', { exact: true }).first().isVisible(), 'Friendly Admin role missing.');
  await expect(await page.getByText('2.450.000 ₫').isVisible(), 'Revenue KPI missing.');
  await expect(await page.getByRole('heading', { name: 'Doanh thu 7 ngày' }).isVisible(), 'Revenue chart missing.');
  await expect(await page.getByRole('heading', { name: 'Sản phẩm bán chạy' }).isVisible(), 'Top products missing.');
  await expect(await page.getByRole('heading', { name: 'Cảnh báo tồn kho' }).isVisible(), 'Low-stock panel missing.');
  await expect(!(await page.locator('body').innerText()).includes('Quản trị viên Demo'), 'Demo user name is visible.');
  await page.getByRole('button', { name: 'Sản phẩm', exact: true }).click();
  await expect(await page.getByText('Chức năng đang được hoàn thiện.').isVisible(), 'Pending menu toast missing.');
  await page.getByRole('button', { name: 'Thêm sản phẩm' }).click();
  await expect(await page.getByText('Chức năng đang được hoàn thiện.').isVisible(), 'Quick-action toast missing.');
  await page.screenshot({ path: 'test-results/admin-dashboard.png', fullPage: true });
  const storedAdmin = await page.evaluate(() => localStorage.getItem('ktpm.auth.v1'));
  await expect(storedAdmin?.includes('accessToken'), 'JWT is not persisted.');
  await expect(!storedAdmin.includes(password) && !storedAdmin.includes('password'), 'Password leaked into auth storage.');
  pass('Admin login receives JWT and opens Dashboard');

  await page.reload();
  await page.waitForURL('**/admin/dashboard');
  await expect(await page.getByText('Xin chào,').isVisible(), 'Dashboard did not survive refresh.');
  pass('Refresh restores auth state');

  const logoutResponse = page.waitForResponse((response) => response.url().endsWith('/api/auth/logout'));
  await page.locator('#header-logout').click();
  await expect((await logoutResponse).status() === 200, 'Logout endpoint did not return 200.');
  await page.waitForURL('**/login');
  await expect((await page.evaluate(() => localStorage.getItem('ktpm.auth.v1'))) === null, 'Auth state remains after logout.');
  await page.goto('/admin/dashboard');
  await page.waitForURL('**/login');
  pass('Logout clears state and route protection blocks Dashboard');

  await login('sales01');
  await page.waitForURL('**/sales');
  await expect(await page.getByText('Không gian làm việc dành cho đội ngũ bán hàng.').isVisible(), 'Sales workspace missing.');
  await page.goto('/admin/dashboard');
  await expect(await page.getByText('Bạn không có quyền truy cập chức năng này.').isVisible(), 'Sales route guard message missing.');
  pass('SALES_STAFF placeholder and Admin route protection');

  await page.evaluate(() => {
    const state = JSON.parse(localStorage.getItem('ktpm.auth.v1'));
    state.role = 'ADMIN';
    localStorage.setItem('ktpm.auth.v1', JSON.stringify(state));
  });
  const forbiddenResponse = page.waitForResponse((response) => (
    response.url().endsWith('/api/admin/ping') && response.request().method() === 'GET'
  ));
  await page.goto('/admin/dashboard');
  await expect((await forbiddenResponse).status() === 403, 'Backend did not return 403 for sales token.');
  await expect(await page.locator('#protected-error').getByText('Bạn không có quyền truy cập chức năng này.').isVisible(), 'Frontend did not display backend 403.');
  await expect((await page.evaluate(() => localStorage.getItem('ktpm.auth.v1'))) !== null, '403 incorrectly cleared auth state.');
  pass('Backend 403 is displayed without clearing auth state');

  await resetAuth();
  await login('warehouse01');
  await page.waitForURL('**/warehouse');
  await expect(await page.getByText('Không gian làm việc dành cho đội ngũ kho.').isVisible(), 'Warehouse workspace missing.');
  await page.goto('/admin/dashboard');
  await expect(await page.getByText('Bạn không có quyền truy cập chức năng này.').isVisible(), 'Warehouse route guard message missing.');
  pass('WAREHOUSE_STAFF placeholder and Admin route protection');

  await resetAuth();
  await login('admin01');
  await page.waitForURL('**/admin/dashboard');
  await page.evaluate(() => {
    const state = JSON.parse(localStorage.getItem('ktpm.auth.v1'));
    state.accessToken = 'invalid.jwt.token';
    localStorage.setItem('ktpm.auth.v1', JSON.stringify(state));
  });
  const unauthorizedResponse = page.waitForResponse((response) => (
    response.url().endsWith('/api/admin/ping') && response.request().method() === 'GET'
  ));
  await page.goto('/admin/dashboard');
  await expect((await unauthorizedResponse).status() === 401, 'Backend did not return 401 for invalid token.');
  await page.waitForURL('**/login');
  await expect((await page.evaluate(() => localStorage.getItem('ktpm.auth.v1'))) === null, '401 did not clear auth state.');
  pass('401 clears auth state and redirects to Login');

  if (failures.length) throw new Error(failures.join('\n'));
  console.log(results.join('\n'));
  console.log(`PASS - ${results.length} browser integration scenarios`);
} finally {
  await browser.close();
}
