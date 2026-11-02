import {
  ApiError, listAccounts, listCategories, listInventory, listInvoices, listProducts, listSuppliers,
  verifyAdminAccess,
} from '../api/client.js';
import { renderShell } from '../components/AppShell.js';
import { displayName } from '../utils/userDisplay.js';

const modules = [
  { key: 'categories', label: 'Danh mục', path: '/admin/categories', note: 'Nhóm sản phẩm đang lưu' },
  { key: 'products', label: 'Sản phẩm', path: '/admin/products', note: 'Sản phẩm đang quản lý' },
  { key: 'suppliers', label: 'Nhà cung cấp', path: '/admin/suppliers', note: 'Nhà cung cấp đang lưu' },
  { key: 'inventory', label: 'Tồn kho', path: '/admin/inventory', note: 'Mặt hàng đang theo dõi' },
  { key: 'invoices', label: 'Hóa đơn', path: '/admin/invoices', note: 'Hóa đơn trong hệ thống' },
  { key: 'accounts', label: 'Tài khoản', path: '/admin/accounts', note: 'Người dùng hệ thống' },
];

function formatDate() {
  const value = new Intl.DateTimeFormat('vi-VN', {
    weekday: 'long', day: '2-digit', month: 'long', year: 'numeric',
  }).format(new Date());
  return value.charAt(0).toUpperCase() + value.slice(1);
}

function renderModules() {
  return modules.map((item) => `
    <a class="module-card" href="${item.path}" data-link>
      <p>${item.label}</p><strong id="dashboard-${item.key}">—</strong><small>${item.note}</small>
    </a>`).join('');
}

export async function renderAdminDashboardPage(root, auth) {
  renderShell(root, auth, `
    <section class="dashboard-hero">
      <div>
        <p class="eyebrow eyebrow--light">Dữ liệu vận hành</p>
        <h2>Xin chào, <span id="welcome-name"></span></h2>
        <p>Các chỉ số bên dưới được đọc trực tiếp từ API và cơ sở dữ liệu hiện tại.</p>
      </div>
      <time class="dashboard-date" id="dashboard-date"></time>
    </section>
    <section class="module-grid" aria-label="Tổng quan dữ liệu">
      ${renderModules()}
    </section>
    <section class="dashboard-panel dashboard-guidance">
      <div class="panel-heading"><div><h3>Phạm vi đã kết nối</h3><p>Không sử dụng dữ liệu mẫu hoặc số liệu ước tính</p></div></div>
      <p>Quản lý danh mục, sản phẩm, nhà cung cấp, tài khoản, tồn kho, hóa đơn và báo cáo quản trị đã được nối với backend. Không sử dụng dữ liệu giả hoặc chức năng ngoài phạm vi đã xác nhận.</p>
      <p><a class="button button--ghost" href="/admin/reports" data-link>Mở báo cáo quản trị</a></p>
    </section>
    <div class="form-alert" id="protected-error" role="alert" hidden></div>
  `, { active: 'Tổng quan' });

  root.querySelector('#welcome-name').textContent = displayName(auth);
  root.querySelector('#dashboard-date').textContent = formatDate();

  try {
    await verifyAdminAccess();
    const [categories, products, suppliers, inventory, invoices, accounts] = await Promise.all([
      listCategories(), listProducts({ size: 1 }), listSuppliers({ size: 1 }),
      listInventory({ size: 1 }), listInvoices({ size: 1 }), listAccounts({ size: 1 }),
    ]);
    root.querySelector('#dashboard-categories').textContent = categories.length.toLocaleString('vi-VN');
    root.querySelector('#dashboard-products').textContent = products.totalElements.toLocaleString('vi-VN');
    root.querySelector('#dashboard-suppliers').textContent = suppliers.totalElements.toLocaleString('vi-VN');
    root.querySelector('#dashboard-inventory').textContent = inventory.totalElements.toLocaleString('vi-VN');
    root.querySelector('#dashboard-invoices').textContent = invoices.totalElements.toLocaleString('vi-VN');
    root.querySelector('#dashboard-accounts').textContent = accounts.totalElements.toLocaleString('vi-VN');
  } catch (error) {
    const alert = root.querySelector('#protected-error');
    alert.textContent = error instanceof ApiError ? error.message : 'Không thể tải dữ liệu tổng quan.';
    alert.hidden = false;
  }
}
