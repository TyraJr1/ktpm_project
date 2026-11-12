import {
  ApiError,
  listPurchaseReceipts,
  listWarehouseSuppliers,
} from '../api/client.js';
import { renderWarehouseShell } from '../components/WarehouseShell.js';
import { escapeHtml, formatCurrency, formatDateTime } from '../utils/format.js';
import { navigate } from '../utils/router.js';

export async function renderWarehouseSupplierPage(root, auth) {
  renderWarehouseShell(root, auth, `
    <section class="admin-page">
      <header class="admin-page__heading">
        <div>
          <h2>Tra cứu Nhà cung cấp (UC-26 / FR-SUP-03)</h2>
          <p>Tra cứu thông tin liên hệ, trạng thái hợp tác của nhà cung cấp văn phòng phẩm và khởi tạo nhanh phiếu nhập kho đối với nhà cung cấp đang hoạt động (ACTIVE).</p>
        </div>
        <a class="button button--primary" href="/warehouse/receipts" data-link>+ Đi tới Lập phiếu nhập</a>
      </header>

      <div class="form-alert" id="wh-supplier-error" role="alert" hidden></div>

      <form class="data-filter" id="wh-supplier-filter" style="grid-template-columns:minmax(240px,1fr) 220px auto auto;">
        <input name="query" placeholder="Tìm theo mã nhà cung cấp (NCC...) hoặc tên nhà cung cấp">
        <select name="status">
          <option value="">Tất cả trạng thái</option>
          <option value="ACTIVE">Đang hợp tác (ACTIVE)</option>
          <option value="INACTIVE">Ngừng hợp tác (INACTIVE)</option>
        </select>
        <button class="button button--ghost" type="submit">Tìm kiếm</button>
        <button class="button button--ghost" type="button" id="wh-supplier-reset">Tất cả</button>
      </form>

      <div class="data-panel">
        <div class="data-table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>Mã NCC</th>
                <th>Tên nhà cung cấp</th>
                <th>Số điện thoại</th>
                <th>Email</th>
                <th>Địa chỉ</th>
                <th>Trạng thái</th>
                <th>Thao tác nghiệp vụ</th>
              </tr>
            </thead>
            <tbody id="wh-supplier-rows">
              <tr><td class="empty-cell" colspan="7">Đang tải danh sách nhà cung cấp...</td></tr>
            </tbody>
          </table>
        </div>
        <p class="data-count" id="wh-supplier-count"></p>
      </div>

      <section class="invoice-detail" id="wh-supplier-receipts-panel" hidden></section>
    </section>
  `, { active: 'Tra cứu nhà cung cấp' });

  const errorEl = root.querySelector('#wh-supplier-error');
  const filterForm = root.querySelector('#wh-supplier-filter');
  const rowsEl = root.querySelector('#wh-supplier-rows');
  const countEl = root.querySelector('#wh-supplier-count');
  const receiptsPanel = root.querySelector('#wh-supplier-receipts-panel');

  let suppliers = [];

  const showError = (err) => {
    errorEl.textContent = err instanceof ApiError ? err.message : String(err?.message || err || 'Không thể tải danh sách nhà cung cấp.');
    errorEl.hidden = false;
  };

  const clearError = () => {
    errorEl.hidden = true;
  };

  const loadSuppliers = async () => {
    clearError();
    try {
      const query = filterForm.elements.query.value.trim();
      const status = filterForm.elements.status.value;
      const data = await listWarehouseSuppliers({ query, status, size: 100 });
      suppliers = data.content || [];
      countEl.textContent = `${(data.totalElements ?? suppliers.length).toLocaleString('vi-VN')} nhà cung cấp`;

      rowsEl.innerHTML = suppliers.length ? suppliers.map((s) => `
        <tr>
          <td><strong>${escapeHtml(s.supplierCode)}</strong></td>
          <td><strong>${escapeHtml(s.supplierName)}</strong></td>
          <td>${escapeHtml(s.phone || '—')}</td>
          <td>${escapeHtml(s.email || '—')}</td>
          <td>${escapeHtml(s.address || '—')}</td>
          <td>
            <span class="data-status ${s.status === 'ACTIVE' ? 'is-active' : 'is-inactive'}">
              ${escapeHtml(s.status)}
            </span>
          </td>
          <td class="table-actions">
            <button class="table-action" type="button" data-supplier-history="${s.id}">
              Phiếu nhập đã lập
            </button>
            ${s.status === 'ACTIVE' ? `
              <button class="table-action" type="button" data-supplier-create-receipt="${s.id}">
                + Lập phiếu nhập
              </button>
            ` : '<small style="margin-left:6px;color:#94500f;">Ngừng nhập (FR-SUP-03)</small>'}
          </td>
        </tr>
      `).join('') : '<tr><td class="empty-cell" colspan="7">Không tìm thấy nhà cung cấp phù hợp.</td></tr>';
    } catch (error) {
      showError(error);
    }
  };

  const showSupplierReceipts = async (supplierId) => {
    clearError();
    const supplier = suppliers.find((s) => s.id === supplierId);
    if (!supplier) return;

    try {
      const data = await listPurchaseReceipts({ query: supplier.supplierName, size: 50 });
      const list = (data.content || []).filter((r) => r.supplierId === supplier.id);
      receiptsPanel.hidden = false;
      receiptsPanel.innerHTML = `
        <div class="invoice-detail__heading">
          <div>
            <p class="eyebrow">Lịch sử phiếu nhập kho theo nhà cung cấp</p>
            <h3>${escapeHtml(supplier.supplierCode)} — ${escapeHtml(supplier.supplierName)}</h3>
          </div>
          <strong>${list.length} phiếu nhập</strong>
        </div>
        <dl class="invoice-meta">
          <div><dt>Số điện thoại</dt><dd>${escapeHtml(supplier.phone || '—')}</dd></div>
          <div><dt>Email</dt><dd>${escapeHtml(supplier.email || '—')}</dd></div>
          <div><dt>Địa chỉ</dt><dd>${escapeHtml(supplier.address || '—')}</dd></div>
          <div><dt>Trạng thái hợp tác</dt><dd>${escapeHtml(supplier.status)}</dd></div>
        </dl>
        <div class="data-table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>Mã phiếu nhập</th>
                <th>Ngày lập</th>
                <th>Nhân viên lập</th>
                <th>Tổng chi phí</th>
                <th>Trạng thái</th>
              </tr>
            </thead>
            <tbody>
              ${list.length ? list.map((r) => `
                <tr>
                  <td><strong>${escapeHtml(r.receiptCode)}</strong></td>
                  <td>${formatDateTime(r.receiptDate)}</td>
                  <td>${escapeHtml(r.createdBy || '—')}</td>
                  <td><strong>${formatCurrency(r.totalCost ?? r.totalAmount ?? 0)}</strong></td>
                  <td>
                    <span class="data-status ${r.status === 'CONFIRMED' ? 'is-active' : 'is-inactive'}">
                      ${escapeHtml(r.status)}
                    </span>
                  </td>
                </tr>
              `).join('') : '<tr><td class="empty-cell" colspan="5">Chưa có phiếu nhập kho nào từ nhà cung cấp này.</td></tr>'}
            </tbody>
          </table>
        </div>
      `;
      receiptsPanel.scrollIntoView({ behavior: 'smooth', block: 'start' });
    } catch (error) {
      showError(error);
    }
  };

  filterForm.addEventListener('submit', (event) => {
    event.preventDefault();
    loadSuppliers();
  });

  root.querySelector('#wh-supplier-reset').addEventListener('click', () => {
    filterForm.elements.query.value = '';
    filterForm.elements.status.value = '';
    loadSuppliers();
  });

  root.addEventListener('click', (event) => {
    const createBtn = event.target.closest('[data-supplier-create-receipt]');
    if (createBtn) {
      sessionStorage.setItem('ktpm.warehouse.preselectSupplierId', String(createBtn.dataset.supplierCreateReceipt));
      navigate('/warehouse/receipts');
      return;
    }

    const historyBtn = event.target.closest('[data-supplier-history]');
    if (historyBtn) {
      showSupplierReceipts(Number(historyBtn.dataset.supplierHistory));
    }
  });

  await loadSuppliers();
}
