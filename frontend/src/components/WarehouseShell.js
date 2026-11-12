import { clearAuth, routeForRole } from '../auth/authStore.js';
import { logout } from '../api/client.js';
import { navigate } from '../utils/router.js';
import { displayName, displayRole, initialsFor } from '../utils/userDisplay.js';

const menuItems = [
  { label: 'Tổng quan kho', icon: 'dashboard', path: '/warehouse/dashboard' },
  { label: 'Tồn kho & Điều chỉnh', icon: 'boxes', path: '/warehouse/inventory' },
  { label: 'Nhập hàng (Phiếu nhập)', icon: 'inbound', path: '/warehouse/receipts' },
  { label: 'Lịch sử biến động', icon: 'history', path: '/warehouse/transactions' },
  { label: 'Tra cứu nhà cung cấp', icon: 'truck', path: '/warehouse/suppliers' },
];

const icons = {
  dashboard: '<path d="M3 3h7v7H3zM14 3h7v7h-7zM3 14h7v7H3zM14 14h7v7h-7z"/>',
  boxes: '<path d="M12 3l8 4.5v9L12 21l-8-4.5v-9L12 3zM12 12l8-4.5M12 12v9M12 12L4 7.5"/>',
  inbound: '<path d="M6 3h12v18l-3-2-3 2-3-2-3 2zM12 7v7M9 11l3 3 3-3"/>',
  history: '<path d="M3 12a9 9 0 1 0 3-6.7L3 8M3 3v5h5M12 7v5l3 3"/>',
  truck: '<path d="M1 3h15v13H1zM16 8h4l3 3v5h-7zM5.5 18.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5ZM18.5 18.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5Z"/>',
};

function icon(name) {
  return `<svg aria-hidden="true" viewBox="0 0 24 24">${icons[name]}</svg>`;
}

export function renderWarehouseShell(root, auth, contentHtml, options = {}) {
  const { active = 'Tổng quan kho', eyebrow = 'Nhân Viên Kho' } = options;
  root.innerHTML = `
    <div class="app-layout">
      <aside class="sidebar">
        <a class="brand brand--sidebar" href="${routeForRole(auth.role)}" data-link>
          <span class="brand__mark">VP</span>
          <span><strong>Văn Phòng Phẩm</strong><small>Phân hệ quản lý kho hàng</small></span>
        </a>
        <nav class="sidebar__nav" aria-label="Điều hướng kho hàng">
          ${menuItems.map((item) => `<a class="nav-item ${active === item.label ? 'is-active' : ''}" href="${item.path}" data-link>${icon(item.icon)}<span>${item.label}</span></a>`).join('')}
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
    </div>`;

  root.querySelector('#current-user-name').textContent = displayName(auth);
  root.querySelector('#current-user-role').textContent = displayRole(auth.role);
  root.querySelector('#user-avatar').textContent = initialsFor(auth);

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
