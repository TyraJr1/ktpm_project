import { ApiError, verifySalesAccess, verifyWarehouseAccess } from '../api/client.js';
import { renderShell } from '../components/AppShell.js';
import { displayRole } from '../utils/userDisplay.js';

const roleConfig = {
  SALES_STAFF: {
    title: 'Khu vực Nhân viên bán hàng',
    description: 'Không gian làm việc dành cho đội ngũ bán hàng.',
    verify: verifySalesAccess,
  },
  WAREHOUSE_STAFF: {
    title: 'Khu vực Nhân viên kho',
    description: 'Không gian làm việc dành cho đội ngũ kho.',
    verify: verifyWarehouseAccess,
  },
};

export async function renderRolePlaceholderPage(root, auth) {
  const config = roleConfig[auth.role];
  renderShell(root, auth, `
    <section class="placeholder-card">
      <span class="placeholder-card__badge" id="role-label"></span>
      <h2>${config.title}</h2>
      <p>${config.description}</p>
      <div class="placeholder-card__line"></div>
      <small>Chức năng đang được hoàn thiện.</small>
    </section>
    <div class="form-alert" id="protected-error" role="alert" hidden></div>
  `, { active: config.title, eyebrow: 'Khu vực làm việc' });
  root.querySelector('#role-label').textContent = displayRole(auth.role);

  try {
    await config.verify();
  } catch (error) {
    if (error instanceof ApiError && error.status === 403) {
      const alert = root.querySelector('#protected-error');
      alert.textContent = 'Bạn không có quyền truy cập chức năng này.';
      alert.hidden = false;
    }
  }
}

export function renderForbiddenPage(root, auth) {
  root.innerHTML = `
    <main class="state-page">
      <section class="state-card">
        <span class="state-card__code">403</span>
        <p class="eyebrow">Truy cập bị từ chối</p>
        <h1>Bạn không có quyền truy cập chức năng này.</h1>
        <p>Vai trò hiện tại của bạn là <strong id="forbidden-role"></strong>.</p>
        <a class="button button--primary" href="/" data-home-link>Quay về khu vực của tôi</a>
      </section>
    </main>`;
  root.querySelector('#forbidden-role').textContent = displayRole(auth.role);
}
