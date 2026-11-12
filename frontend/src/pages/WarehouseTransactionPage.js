import {
  ApiError,
  listStockTransactions,
  listWarehouseProducts,
} from '../api/client.js';
import { renderWarehouseShell } from '../components/WarehouseShell.js';
import { escapeHtml, formatDateTime } from '../utils/format.js';

function txTypeLabel(type) {
  const map = {
    INBOUND: 'Nhập kho (INBOUND)',
    OUTBOUND: 'Xuất kho (OUTBOUND)',
    ADJUSTMENT: 'Kiểm kê / Điều chỉnh (ADJUSTMENT)',
    SALE: 'Xuất bán hàng POS (SALE)',
  };
  return map[type] || type || '—';
}

function txTypeBadge(type) {
  if (type === 'INBOUND') {
    return `<span class="data-status is-active">${escapeHtml(txTypeLabel(type))}</span>`;
  }
  if (type === 'OUTBOUND' || type === 'SALE') {
    return `<span class="data-status is-inactive">${escapeHtml(txTypeLabel(type))}</span>`;
  }
  return `<span class="data-status" style="background:#eef2ff;color:#4338ca;border-color:#c7d2fe;">${escapeHtml(txTypeLabel(type))}</span>`;
}

export async function renderWarehouseTransactionPage(root, auth) {
  renderWarehouseShell(root, auth, `
    <section class="admin-page">
      <header class="admin-page__heading">
        <div>
          <h2>Lịch sử Biến động Tồn kho (UC-25 / FR-INV-05)</h2>
          <p>Tra cứu sổ cái giao dịch kho (stock_transactions) theo sản phẩm, loại giao dịch (INBOUND / OUTBOUND / ADJUSTMENT / SALE) và khoảng thời gian.</p>
        </div>
        <div style="display:flex;gap:8px;">
          <a class="button button--ghost" href="/warehouse/inventory" data-link>Kiểm kê & Xuất kho</a>
          <a class="button button--primary" href="/warehouse/receipts" data-link>+ Lập phiếu nhập</a>
        </div>
      </header>

      <div class="form-alert" id="wh-tx-error" role="alert" hidden></div>

      <form class="data-filter" id="wh-tx-filter" style="grid-template-columns:minmax(200px,1fr) 210px 190px 150px 150px auto auto;">
        <input name="query" placeholder="Tìm mã SP, tên SP hoặc ghi chú...">
        <select name="productId" id="wh-tx-product-select">
          <option value="">Tất cả sản phẩm</option>
        </select>
        <select name="transactionType">
          <option value="">Mọi loại biến động</option>
          <option value="INBOUND">INBOUND (Nhập kho)</option>
          <option value="OUTBOUND">OUTBOUND (Xuất kho)</option>
          <option value="ADJUSTMENT">ADJUSTMENT (Kiểm kê)</option>
          <option value="SALE">SALE (Xuất bán POS)</option>
        </select>
        <label>Từ ngày<input name="fromDate" type="date"></label>
        <label>Đến ngày<input name="toDate" type="date"></label>
        <button class="button button--ghost" type="submit">Tra cứu</button>
        <button class="button button--ghost" type="button" id="wh-tx-reset">Đặt lại</button>
      </form>

      <div class="data-panel">
        <div class="data-table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>Mã GD</th>
                <th>Thời gian ghi nhận</th>
                <th>Loại biến động</th>
                <th>Mã SP</th>
                <th>Tên sản phẩm</th>
                <th>Số lượng biến động</th>
                <th>Chứng từ tham chiếu</th>
                <th>Người thực hiện</th>
                <th>Lý do / Ghi chú</th>
              </tr>
            </thead>
            <tbody id="wh-tx-rows">
              <tr><td class="empty-cell" colspan="9">Đang tải lịch sử biến động tồn kho...</td></tr>
            </tbody>
          </table>
        </div>
        <p class="data-count" id="wh-tx-count"></p>
      </div>
    </section>
  `, { active: 'Lịch sử biến động' });

  const errorEl = root.querySelector('#wh-tx-error');
  const filterForm = root.querySelector('#wh-tx-filter');
  const productSelect = root.querySelector('#wh-tx-product-select');
  const rowsEl = root.querySelector('#wh-tx-rows');
  const countEl = root.querySelector('#wh-tx-count');

  const showError = (err) => {
    errorEl.textContent = err instanceof ApiError ? err.message : String(err?.message || err || 'Không thể tải lịch sử biến động.');
    errorEl.hidden = false;
  };

  const clearError = () => {
    errorEl.hidden = true;
  };

  const loadProductsForFilter = async () => {
    try {
      const data = await listWarehouseProducts({ size: 200 });
      const list = data.content || [];
      productSelect.innerHTML = `
        <option value="">Tất cả sản phẩm</option>
        ${list.map((p) => `
          <option value="${p.id}">${escapeHtml(p.productCode)} — ${escapeHtml(p.productName)}</option>
        `).join('')}
      `;
    } catch {
      // Non-blocking filter enhancement
    }
  };

  const loadTransactions = async () => {
    clearError();
    const query = filterForm.elements.query.value.trim();
    const productId = filterForm.elements.productId.value;
    const transactionType = filterForm.elements.transactionType.value;
    const fromDate = filterForm.elements.fromDate.value;
    const toDate = filterForm.elements.toDate.value;

    if (fromDate && toDate && fromDate > toDate) {
      showError('Từ ngày không được lớn hơn đến ngày.');
      return;
    }

    try {
      const data = await listStockTransactions({
        query,
        productId,
        transactionType,
        fromDate,
        toDate,
        size: 100,
      });
      const list = data.content || [];
      countEl.textContent = `${(data.totalElements ?? list.length).toLocaleString('vi-VN')} bản ghi biến động tồn kho`;

      rowsEl.innerHTML = list.length ? list.map((tx) => {
        const change = Number(tx.quantityChange ?? tx.quantity ?? 0);
        const sign = change > 0 ? `+${change}` : String(change);
        const color = change > 0 ? '#087b72'
          : change < 0 ? '#b42318'
            : '#4338ca';
        const stockTransition = (tx.quantityBefore !== undefined && tx.quantityAfter !== undefined)
          ? `<br><small>Tồn: ${tx.quantityBefore} → ${tx.quantityAfter}</small>`
          : '';
        let refHtml = '—';
        if (tx.receiptCode) {
          refHtml = `Phiếu nhập<br><small><strong>#${escapeHtml(tx.receiptCode)}</strong></small>`;
        } else if (tx.orderCode) {
          refHtml = `Đơn bán hàng<br><small><strong>#${escapeHtml(tx.orderCode)}</strong></small>`;
        } else if (tx.transactionType === 'OUTBOUND') {
          refHtml = 'Xuất kho nội bộ';
        } else if (tx.transactionType === 'ADJUSTMENT') {
          refHtml = 'Kiểm kê kho';
        }
        return `
          <tr>
            <td><strong>#${tx.id}</strong></td>
            <td>${formatDateTime(tx.transactionDate)}</td>
            <td>${txTypeBadge(tx.transactionType)}</td>
            <td><strong>${escapeHtml(tx.productCode)}</strong></td>
            <td>${escapeHtml(tx.productName)}</td>
            <td><strong style="color:${color};font-size:0.9rem;">${escapeHtml(sign)}</strong>${stockTransition}</td>
            <td>${refHtml}</td>
            <td>${escapeHtml(tx.createdBy || '—')}</td>
            <td>${escapeHtml(tx.reason || tx.note || '—')}</td>
          </tr>
        `;
      }).join('') : '<tr><td class="empty-cell" colspan="9">Không có giao dịch biến động phù hợp với bộ lọc.</td></tr>';
    } catch (error) {
      showError(error);
    }
  };

  filterForm.addEventListener('submit', (event) => {
    event.preventDefault();
    loadTransactions();
  });

  root.querySelector('#wh-tx-reset').addEventListener('click', () => {
    filterForm.elements.query.value = '';
    filterForm.elements.productId.value = '';
    filterForm.elements.transactionType.value = '';
    filterForm.elements.fromDate.value = '';
    filterForm.elements.toDate.value = '';
    loadTransactions();
  });

  await Promise.all([loadProductsForFilter(), loadTransactions()]);
}
