import {
  ApiError, createAccount, deleteAccount, getInventoryReport,
  getReportSummary, getTopProducts, listAccounts, updateAccount,
} from '../api/client.js';
import { renderShell } from '../components/AppShell.js';

const esc = (value) => String(value ?? '')
  .replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;')
  .replaceAll('"', '&quot;').replaceAll("'", '&#039;');
const money = (value) => new Intl.NumberFormat('vi-VN', {
  style: 'currency', currency: 'VND', maximumFractionDigits: 0,
}).format(Number(value || 0));

function pageLayout(title, description, body, action = '') {
  return `<section class="admin-page">
    <header class="admin-page__heading"><div><h2>${title}</h2><p>${description}</p></div>${action}</header>
    <div class="form-alert" id="page-error" role="alert" hidden></div>${body}
  </section>`;
}

function showError(root, error) {
  const alert = root.querySelector('#page-error');
  alert.textContent = error instanceof ApiError ? error.message : 'Không thể xử lý yêu cầu.';
  alert.hidden = false;
}

function clearError(root) {
  root.querySelector('#page-error').hidden = true;
}

const roleName = (role) => ({
  ADMIN: 'Quản trị viên', SALES_STAFF: 'Nhân viên bán hàng', WAREHOUSE_STAFF: 'Nhân viên kho',
}[role] || role);

function statusBadge(status) {
  const active = status === 'ACTIVE';
  return `<span class="data-status ${active ? 'is-active' : 'is-inactive'}">${active ? 'Hoạt động' : 'Ngừng hoạt động'}</span>`;
}

export async function renderAccountPage(root, auth) {
  renderShell(root, auth, pageLayout('Quản lý tài khoản',
    'Tạo, cập nhật, phân quyền và quản lý trạng thái người sử dụng hệ thống.', `
    <form class="data-editor" id="account-form" hidden>
      <input type="hidden" name="id">
      <div class="editor-grid editor-grid--three">
        <label>Tên đăng nhập<input name="username" maxlength="100" required></label>
        <label>Họ tên<input name="fullName" maxlength="150" required></label>
        <label>Vai trò<select name="role" required>
          <option value="ADMIN">Quản trị viên</option><option value="SALES_STAFF">Nhân viên bán hàng</option>
          <option value="WAREHOUSE_STAFF">Nhân viên kho</option></select></label>
        <label>Email<input name="email" type="email" maxlength="150"></label>
        <label>Điện thoại<input name="phone" maxlength="20"></label>
        <label>Trạng thái<select name="status"><option value="ACTIVE">Hoạt động</option><option value="INACTIVE">Ngừng hoạt động</option></select></label>
        <label class="editor-grid__wide">Mật khẩu <small id="password-help">Bắt buộc khi tạo mới; ít nhất 8 ký tự, có chữ hoa, chữ thường và số.</small>
          <input name="password" type="password" autocomplete="new-password"></label>
      </div>
      <div class="editor-actions"><button class="button button--ghost" type="button" data-cancel>Hủy</button><button class="button button--primary" type="submit">Lưu</button></div>
    </form>
    <form class="data-filter" id="account-filter"><input name="query" placeholder="Tìm theo tài khoản hoặc họ tên">
      <select name="role"><option value="">Tất cả vai trò</option><option value="ADMIN">Quản trị viên</option><option value="SALES_STAFF">Nhân viên bán hàng</option><option value="WAREHOUSE_STAFF">Nhân viên kho</option></select>
      <select name="status"><option value="">Tất cả trạng thái</option><option value="ACTIVE">Hoạt động</option><option value="INACTIVE">Ngừng hoạt động</option></select>
      <button class="button button--ghost">Tìm kiếm</button></form>
    <div class="data-panel"><div class="data-table-wrap"><table class="data-table"><thead><tr><th>Tài khoản</th><th>Họ tên</th><th>Liên hệ</th><th>Vai trò</th><th>Trạng thái</th><th></th></tr></thead><tbody id="account-rows"></tbody></table></div><p class="data-count" id="account-count"></p></div>
  `, '<button class="button button--primary" type="button" id="new-account">Thêm tài khoản</button>'), { active: 'Tài khoản' });

  const form = root.querySelector('#account-form');
  const filter = root.querySelector('#account-filter');
  let records = [];
  const load = async () => {
    clearError(root);
    try {
      const data = await listAccounts({ query: filter.elements.query.value, role: filter.elements.role.value,
        status: filter.elements.status.value, size: 100 });
      records = data.content;
      root.querySelector('#account-count').textContent = `${data.totalElements.toLocaleString('vi-VN')} tài khoản`;
      root.querySelector('#account-rows').innerHTML = records.length ? records.map((item) => `<tr>
        <td><strong>${esc(item.username)}</strong></td><td>${esc(item.fullName)}</td>
        <td>${esc(item.phone || '—')}<br><small>${esc(item.email || '')}</small></td>
        <td>${esc(roleName(item.role))}</td><td>${statusBadge(item.status)}</td>
        <td class="table-actions"><button class="table-action" data-account-edit="${item.id}">Sửa</button><button class="table-action table-action--danger" data-account-delete="${item.id}">Xóa</button></td></tr>`).join('')
        : '<tr><td class="empty-cell" colspan="6">Không có tài khoản phù hợp.</td></tr>';
    } catch (error) { showError(root, error); }
  };
  const open = (item = null) => {
    form.hidden = false;
    form.elements.id.value = item?.id || '';
    ['username', 'fullName', 'email', 'phone'].forEach((name) => { form.elements[name].value = item?.[name] || ''; });
    form.elements.role.value = item?.role || 'SALES_STAFF';
    form.elements.status.value = item?.status || 'ACTIVE';
    form.elements.password.value = '';
    form.elements.password.required = !item;
    root.querySelector('#password-help').textContent = item
      ? 'Để trống nếu không đổi mật khẩu.' : 'Bắt buộc khi tạo mới; ít nhất 8 ký tự, có chữ hoa, chữ thường và số.';
    form.elements.username.focus();
  };
  root.querySelector('#new-account').addEventListener('click', () => open());
  filter.addEventListener('submit', (event) => { event.preventDefault(); load(); });
  root.addEventListener('click', async (event) => {
    const edit = event.target.closest('[data-account-edit]');
    if (edit) open(records.find((item) => item.id === Number(edit.dataset.accountEdit)));
    const remove = event.target.closest('[data-account-delete]');
    if (remove && window.confirm('Bạn có chắc muốn xóa tài khoản này?')) {
      clearError(root);
      try { await deleteAccount(remove.dataset.accountDelete); await load(); } catch (error) { showError(root, error); }
    }
    if (event.target.closest('[data-cancel]')) form.hidden = true;
  });
  form.addEventListener('submit', async (event) => {
    event.preventDefault(); clearError(root);
    const id = form.elements.id.value;
    const body = { username: form.elements.username.value, fullName: form.elements.fullName.value,
      email: form.elements.email.value, phone: form.elements.phone.value, role: form.elements.role.value,
      status: form.elements.status.value,
      ...(form.elements.password.value ? { password: form.elements.password.value } : {}) };
    try {
      if (id) {
        await updateAccount(id, body);
      } else {
        await createAccount({ ...body, password: form.elements.password.value });
      }
      form.hidden = true; await load();
    } catch (error) { showError(root, error); }
  });
  await load();
}

function todayValue(date) {
  const offset = date.getTimezoneOffset() * 60000;
  return new Date(date.getTime() - offset).toISOString().slice(0, 10);
}

export async function renderReportPage(root, auth) {
  renderShell(root, auth, pageLayout('Báo cáo quản trị',
    'Doanh thu, hóa đơn và sản phẩm bán chạy chỉ tính các hóa đơn COMPLETED.', `
    <form class="data-filter data-filter--dates" id="report-filter">
      <label>Từ ngày<input name="fromDate" type="date" required></label><label>Đến ngày<input name="toDate" type="date" required></label>
      <button class="button button--primary">Xem báo cáo</button></form>
    <section class="report-summary" aria-label="Tổng hợp báo cáo">
      <article class="report-card"><span>Doanh thu</span><strong id="report-revenue">—</strong></article>
      <article class="report-card"><span>Số hóa đơn</span><strong id="report-invoices">—</strong></article>
      <article class="report-card"><span>Số mặt hàng</span><strong id="report-stock">—</strong></article>
    </section>
    <div class="report-grid">
      <section class="data-panel"><div class="panel-heading"><div><h3>Sản phẩm bán chạy</h3><p>Xếp theo tổng số lượng bán</p></div></div>
        <div class="data-table-wrap"><table class="data-table"><thead><tr><th>Hạng</th><th>Mã</th><th>Sản phẩm</th><th>Đã bán</th></tr></thead><tbody id="top-product-rows"></tbody></table></div></section>
      <section class="data-panel"><div class="panel-heading"><div><h3>Báo cáo tồn kho</h3><p>Số lượng hiện tại trong hệ thống</p></div></div>
        <div class="data-table-wrap"><table class="data-table"><thead><tr><th>Mã</th><th>Sản phẩm</th><th>Tồn</th></tr></thead><tbody id="report-inventory-rows"></tbody></table></div></section>
    </div>`), { active: 'Báo cáo' });

  const filter = root.querySelector('#report-filter');
  const now = new Date();
  filter.elements.toDate.value = todayValue(now);
  filter.elements.fromDate.value = todayValue(new Date(now.getFullYear(), now.getMonth(), 1));
  const load = async () => {
    clearError(root);
    try {
      const fromDate = filter.elements.fromDate.value;
      const toDate = filter.elements.toDate.value;
      const [summary, topProducts, inventory] = await Promise.all([
        getReportSummary(fromDate, toDate), getTopProducts(fromDate, toDate), getInventoryReport({ size: 100 }),
      ]);
      const remainingInventory = inventory.totalPages > 1
        ? await Promise.all(Array.from({ length: inventory.totalPages - 1 }, (_, index) =>
          getInventoryReport({ page: index + 1, size: 100 }))) : [];
      const inventoryItems = [inventory, ...remainingInventory].flatMap((page) => page.content);
      root.querySelector('#report-revenue').textContent = money(summary.totalRevenue);
      root.querySelector('#report-invoices').textContent = summary.invoiceCount.toLocaleString('vi-VN');
      root.querySelector('#report-stock').textContent = inventory.totalElements.toLocaleString('vi-VN');
      root.querySelector('#top-product-rows').innerHTML = topProducts.length ? topProducts.map((item, index) =>
        `<tr><td>${index + 1}</td><td><strong>${esc(item.productCode)}</strong></td><td>${esc(item.productName)}</td><td>${item.totalQuantity.toLocaleString('vi-VN')}</td></tr>`).join('')
        : '<tr><td class="empty-cell" colspan="4">Không có sản phẩm bán trong khoảng đã chọn.</td></tr>';
      root.querySelector('#report-inventory-rows').innerHTML = inventoryItems.length ? inventoryItems.map((item) =>
        `<tr><td><strong>${esc(item.productCode)}</strong></td><td>${esc(item.productName)}</td><td>${item.stockQuantity.toLocaleString('vi-VN')}</td></tr>`).join('')
        : '<tr><td class="empty-cell" colspan="3">Chưa có dữ liệu tồn kho.</td></tr>';
    } catch (error) { showError(root, error); }
  };
  filter.addEventListener('submit', (event) => { event.preventDefault(); load(); });
  await load();
}
