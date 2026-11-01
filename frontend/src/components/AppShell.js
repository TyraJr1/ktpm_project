import { clearAuth, routeForRole } from '../auth/authStore.js';
import { logout } from '../api/client.js';
import { navigate } from '../utils/router.js';
import { displayName, displayRole, initialsFor } from '../utils/userDisplay.js';

const menuItems = [
  ['Tổng quan', 'dashboard'],
  ['Danh mục', 'folder'],
  ['Sản phẩm', 'box'],
  ['Nhà cung cấp', 'truck'],
  ['Tồn kho', 'inventory'],
  ['Tài khoản', 'users'],
  ['Báo cáo', 'chart'],
];

const icons = {
  dashboard: '<path d="M3 3h7v7H3zM14 3h7v7h-7zM3 14h7v7H3zM14 14h7v7h-7z"/>',
  folder: '<path d="M3 6.5h7l2 2h9v10.5H3z"/>',
  box: '<path d="m4 7 8-4 8 4-8 4zM4 7v10l8 4 8-4V7M12 11v10"/>',
  truck: '<path d="M3 5h11v11H3zM14 9h4l3 4v3h-7zM7 19a2 2 0 1 0 0-4 2 2 0 0 0 0 4ZM18 19a2 2 0 1 0 0-4 2 2 0 0 0 0 4Z"/>',
  inventory: '<path d="M4 4h16v5H4zM5 9v11h14V9M9 13h6"/>',
  users: '<path d="M16 20v-2a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v2M9.5 10a4 4 0 1 0 0-8 4 4 0 0 0 0 8ZM17 11a4 4 0 0 1 4 4v2"/>',
  chart: '<path d="M4 20V10M10 20V4M16 20v-7M22 20H2"/>',
};

function icon(name) {
  return `<svg aria-hidden="true" viewBox="0 0 24 24">${icons[name]}</svg>`;
}

export function showComingSoon(root) {
  const toast = root.querySelector('#app-toast');
  if (!toast) return;
  toast.textContent = 'Chức năng đang được hoàn thiện.';
  toast.hidden = false;
  window.clearTimeout(showComingSoon.timeoutId);
  showComingSoon.timeoutId = window.setTimeout(() => { toast.hidden = true; }, 2800);
}

export function renderShell(root, auth, contentHtml, options = {}) {
  const { active = 'Tổng quan', eyebrow = 'Văn Phòng Phẩm' } = options;
  root.innerHTML = `
    <div class="app-layout">
      <aside class="sidebar">
        <a class="brand brand--sidebar" href="${routeForRole(auth.role)}" data-link>
          <span class="brand__mark">VP</span>
          <span><strong>Văn Phòng Phẩm</strong><small>Hệ thống quản lý bán hàng</small></span>
        </a>
        <nav class="sidebar__nav" aria-label="Điều hướng chính">
          ${menuItems.map(([label, iconName], index) => index === 0
            ? `<a class="nav-item ${active === label ? 'is-active' : ''}" href="/admin/dashboard" data-link>${icon(iconName)}<span>${label}</span></a>`
            : `<button class="nav-item nav-item--pending" type="button" data-coming-soon>${icon(iconName)}<span>${label}</span></button>`).join('')}
        </nav>
        <div class="sidebar__footer">
          <button class="nav-item nav-item--logout" id="sidebar-logout" type="button">
            <svg aria-hidden="true" viewBox="0 0 24 24"><path d="M10 17l5-5-5-5M15 12H3M14 3h7v18h-7"/></svg>
            <span>Đăng xuất</span>
          </button>
        </div>
      </aside>
      <section class="workspace">
        <header class="topbar">
          <div><p class="eyebrow">${eyebrow}</p><h1>${active}</h1></div>
          <div class="topbar__user">
            <span class="user-avatar" id="user-avatar" aria-hidden="true"></span>
            <span class="user-copy"><strong id="current-user-name"></strong><small id="current-user-role"></small></span>
            <button class="button button--ghost" id="header-logout" type="button">Đăng xuất</button>
          </div>
        </header>
        <main class="workspace__content">${contentHtml}</main>
      </section>
      <div class="app-toast" id="app-toast" role="status" aria-live="polite" hidden></div>
    </div>`;

  root.querySelector('#current-user-name').textContent = displayName(auth);
  root.querySelector('#current-user-role').textContent = displayRole(auth.role);
  root.querySelector('#user-avatar').textContent = initialsFor(auth);
  root.querySelectorAll('[data-coming-soon]').forEach((button) => {
    button.addEventListener('click', () => showComingSoon(root));
  });

  const handleLogout = async (button) => {
    button.disabled = true;
    button.textContent = 'Đang đăng xuất…';
    try {
      await logout();
    } catch {
      // Stateless logout always clears local state, even if the token has expired.
    } finally {
      clearAuth();
      navigate('/login', { replace: true });
    }
  };

  root.querySelector('#header-logout').addEventListener('click', (event) => handleLogout(event.currentTarget));
  root.querySelector('#sidebar-logout').addEventListener('click', (event) => handleLogout(event.currentTarget));
}
