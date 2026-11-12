import {
  ApiError,
  confirmPurchaseReceipt,
  getPurchaseReceiptDetail,
  getWarehouseDashboard,
  verifyWarehouseAccess,
} from '../api/client.js';
import { renderWarehouseShell } from '../components/WarehouseShell.js';
import { escapeHtml, formatCurrency, formatDateTime } from '../utils/format.js';
import { displayName } from '../utils/userDisplay.js';

function receiptStatusBadge(status) {
  if (status === 'CONFIRMED') {
    return '<span class="data-status is-active">CONFIRMED (Đã nhập kho)</span>';
  }
  return '<span class="data-status is-inactive">PENDING (Chờ xác nhận)</span>';
}

function txTypeLabel(type) {
  const map = {
    INBOUND: 'Nhập kho (INBOUND)',
    OUTBOUND: 'Xuất kho (OUTBOUND)',
    ADJUSTMENT: 'Kiểm kê/Điều chỉnh (ADJUSTMENT)',
    SALE: 'Xuất bán hàng (SALE)',
  };
  return map[type] || type || '—';
}

function txTypeClass(type) {
  if (type === 'INBOUND') return 'is-active';
  if (type === 'OUTBOUND' || type === 'SALE') return 'is-inactive';
  return '';
}

function formatDate() {
  const value = new Intl.DateTimeFormat('vi-VN', {
    weekday: 'long', day: '2-digit', month: 'long', year: 'numeric',
  }).format(new Date());
  return value.charAt(0).toUpperCase() + value.slice(1);
}

export async function renderWarehouseDashboardPage(root, auth) {
  renderWarehouseShell(root, auth, `
    <section class="dashboard-hero">
      <div>
        <p class="eyebrow eyebrow--light">Không gian làm việc Nhân viên kho</p>
        <h2>Xin chào, <span id="wh-welcome-name"></span></h2>
        <p>Giám sát tồn kho thời gian thực, lập và xác nhận phiếu nhập hàng từ nhà cung cấp, ghi nhận xuất kho, kiểm kê điều chỉnh chênh lệch và đối soát lịch sử biến động tồn kho.</p>
      </div>
      <time class="dashboard-date" id="wh-dashboard-date"></time>
    </section>

    <div class="form-alert" id="wh-dash-error" role="alert" hidden></div>
    <div class="form-alert" id="wh-dash-success" role="status" hidden style="background:#f0fdf4;border-color:#86efac;color:#166534;"></div>

    <section class="module-grid" aria-label="Tổng quan vận hành kho">
      <a class="module-card" href="/warehouse/receipts" data-link>
        <p>Giá trị nhập kho hôm nay</p>
        <strong id="kpi-wh-inbound-amount">0 ₫</strong>
        <small id="kpi-wh-confirmed-today">0 phiếu nhập đã xác nhận trong ngày</small>
      </a>
      <a class="module-card" href="/warehouse/receipts" data-link>
        <p>Phiếu nhập chờ xác nhận</p>
        <strong id="kpi-wh-pending-receipts">0</strong>
        <small>Phiếu trạng thái PENDING chưa cộng vào tồn kho</small>
      </a>
      <a class="module-card" href="/warehouse/inventory" data-link>
        <p>Cảnh báo tồn kho thấp</p>
        <strong id="kpi-wh-low-stock">0</strong>
        <small id="kpi-wh-out-of-stock">0 mặt hàng đã hết hàng (Tồn = 0)</small>
      </a>
      <a class="module-card" href="/warehouse/inventory" data-link>
        <p>Sản phẩm đang lưu kho</p>
        <strong id="kpi-wh-active-products">0</strong>
        <small>Mặt hàng ACTIVE đang quản lý số lượng tồn</small>
      </a>
      <a class="module-card" href="/warehouse/transactions" data-link>
        <p>Biến động kho hôm nay</p>
        <strong id="kpi-wh-today-tx">0</strong>
        <small>Bản ghi INBOUND / OUTBOUND / ADJUSTMENT / SALE</small>
      </a>
      <a class="module-card" href="/warehouse/suppliers" data-link>
        <p>Nhà cung cấp hoạt động</p>
        <strong id="kpi-wh-suppliers">0</strong>
        <small>Đối tác cung ứng ACTIVE sẵn sàng lập phiếu nhập</small>
      </a>
    </section>

    <section class="admin-page" style="margin-top:22px;">
      <header class="admin-page__heading">
        <div>
          <h2>Phiếu nhập kho gần nhất (UC-21 & UC-22)</h2>
          <p>Theo dõi các phiếu nhập vừa tạo, xem chi tiết mặt hàng hoặc xác nhận nhập kho để cộng tồn kho chính thức.</p>
        </div>
        <div style="display:flex;gap:8px;flex-wrap:wrap;">
          <button class="button button--ghost" type="button" id="refresh-wh-dash">Làm mới</button>
          <a class="button button--ghost" href="/warehouse/inventory" data-link>Kiểm kê & Xuất kho</a>
          <a class="button button--primary" href="/warehouse/receipts" data-link>+ Tạo phiếu nhập kho</a>
        </div>
      </header>

      <div class="data-panel">
        <div class="data-table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>Mã phiếu nhập</th>
                <th>Nhà cung cấp</th>
                <th>Ngày lập</th>
                <th>Người lập</th>
                <th>Tổng chi phí</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody id="wh-recent-receipts-tbody">
              <tr><td class="empty-cell" colspan="7">Đang tải dữ liệu phiếu nhập...</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <section class="invoice-detail" id="wh-dash-receipt-detail" hidden></section>
    </section>

    <section class="admin-page" style="margin-top:24px;">
      <header class="admin-page__heading">
        <div>
          <h2>Nhật ký biến động tồn kho gần đây (UC-25)</h2>
          <p>Mọi thao tác nhập kho, xuất kho, kiểm kê điều chỉnh và xuất bán POS đều được ghi vết tự động vào sổ cái tồn kho.</p>
        </div>
        <a class="button button--ghost" href="/warehouse/transactions" data-link>Xem toàn bộ lịch sử</a>
      </header>

      <div class="data-panel">
        <div class="data-table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>Thời gian</th>
                <th>Loại giao dịch</th>
                <th>Mã & Tên sản phẩm</th>
                <th>Số lượng biến động</th>
                <th>Chứng từ tham chiếu</th>
                <th>Người thực hiện</th>
                <th>Ghi chú / Lý do</th>
              </tr>
            </thead>
            <tbody id="wh-recent-tx-tbody">
              <tr><td class="empty-cell" colspan="7">Đang tải lịch sử biến động...</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>
  `, { active: 'Tổng quan kho' });

  root.querySelector('#wh-welcome-name').textContent = displayName(auth);
  root.querySelector('#wh-dashboard-date').textContent = formatDate();

  const errorEl = root.querySelector('#wh-dash-error');
  const successEl = root.querySelector('#wh-dash-success');
  const receiptsTbody = root.querySelector('#wh-recent-receipts-tbody');
  const txTbody = root.querySelector('#wh-recent-tx-tbody');
  const detailEl = root.querySelector('#wh-dash-receipt-detail');

  const showError = (err) => {
    successEl.hidden = true;
    errorEl.textContent = err instanceof ApiError ? err.message : String(err?.message || err || 'Không thể tải dữ liệu kho.');
    errorEl.hidden = false;
  };

  const showSuccess = (msg) => {
    errorEl.hidden = true;
    successEl.textContent = msg;
    successEl.hidden = false;
  };

  const clearAlerts = () => {
    errorEl.hidden = true;
    successEl.hidden = true;
  };

  const renderReceiptDetail = (item) => {
    detailEl.hidden = false;
    detailEl.innerHTML = `
      <div class="invoice-detail__heading">
        <div>
          <p class="eyebrow">Chi tiết phiếu nhập kho (${escapeHtml(item.status)})</p>
          <h3>${escapeHtml(item.receiptCode)} — ${escapeHtml(item.supplierName)}</h3>
        </div>
        <div style="display:flex;align-items:center;gap:12px;">
          <strong>${formatCurrency(item.totalAmount)}</strong>
          ${item.status === 'PENDING' ? `
            <button class="button button--primary" type="button" data-dash-confirm-receipt="${item.id}">
              Xác nhận nhập kho & Cộng tồn
            </button>
          ` : ''}
        </div>
      </div>
      <dl class="invoice-meta">
        <div><dt>Mã nhà cung cấp</dt><dd>${escapeHtml(item.supplierCode || '—')}</dd></div>
        <div><dt>Tên nhà cung cấp</dt><dd>${escapeHtml(item.supplierName || '—')}</dd></div>
        <div><dt>Người lập phiếu</dt><dd>${escapeHtml(item.createdBy || '—')}</dd></div>
        <div><dt>Thời gian lập</dt><dd>${formatDateTime(item.receiptDate)}</dd></div>
      </dl>
      ${item.note ? `<p style="margin:0 0 12px;"><strong>Ghi chú:</strong> ${escapeHtml(item.note)}</p>` : ''}
      <div class="data-table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>Mã SP</th>
              <th>Tên sản phẩm</th>
              <th>Số lượng nhập</th>
              <th>Đơn giá nhập</th>
              <th>Thành tiền</th>
            </tr>
          </thead>
          <tbody>
            ${(item.lines || []).map((line) => `
              <tr>
                <td><strong>${escapeHtml(line.productCode)}</strong></td>
                <td>${escapeHtml(line.productName)}</td>
                <td><strong>+${line.quantity}</strong></td>
                <td>${formatCurrency(line.purchasePrice)}</td>
                <td><strong>${formatCurrency(line.lineTotal)}</strong></td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    `;
    detailEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  const loadDashboard = async () => {
    try {
      await verifyWarehouseAccess();
      const data = await getWarehouseDashboard();

      root.querySelector('#kpi-wh-inbound-amount').textContent = formatCurrency(data.todayInboundAmount || 0);
      root.querySelector('#kpi-wh-confirmed-today').textContent = `${Number(data.todayConfirmedReceiptCount || 0).toLocaleString('vi-VN')} phiếu nhập đã xác nhận trong ngày`;
      root.querySelector('#kpi-wh-pending-receipts').textContent = Number(data.pendingReceiptCount || 0).toLocaleString('vi-VN');
      root.querySelector('#kpi-wh-low-stock').textContent = `${Number(data.lowStockCount || 0).toLocaleString('vi-VN')} SP (<= 10)`;
      root.querySelector('#kpi-wh-out-of-stock').textContent = `${Number(data.outOfStockCount || 0).toLocaleString('vi-VN')} mặt hàng đã hết hàng (Tồn = 0)`;
      root.querySelector('#kpi-wh-active-products').textContent = Number(data.totalActiveProducts || 0).toLocaleString('vi-VN');
      root.querySelector('#kpi-wh-today-tx').textContent = Number(data.todayTransactionCount || 0).toLocaleString('vi-VN');
      root.querySelector('#kpi-wh-suppliers').textContent = Number(data.activeSupplierCount || 0).toLocaleString('vi-VN');

      const receipts = data.recentReceipts || [];
      receiptsTbody.innerHTML = receipts.length ? receipts.map((r) => `
        <tr>
          <td><strong>${escapeHtml(r.receiptCode)}</strong></td>
          <td>${escapeHtml(r.supplierName)} <small>(${escapeHtml(r.supplierCode || '')})</small></td>
          <td>${formatDateTime(r.receiptDate)}</td>
          <td>${escapeHtml(r.createdBy || '—')}</td>
          <td><strong>${formatCurrency(r.totalAmount)}</strong></td>
          <td>${receiptStatusBadge(r.status)}</td>
          <td class="table-actions">
            <button class="table-action" type="button" data-dash-view-receipt="${r.id}">Chi tiết</button>
            ${r.status === 'PENDING' ? `
              <button class="table-action" type="button" data-dash-confirm-receipt="${r.id}">Xác nhận nhập</button>
            ` : ''}
          </td>
        </tr>
      `).join('') : '<tr><td class="empty-cell" colspan="7">Chưa có phiếu nhập kho nào.</td></tr>';

      const txs = data.recentTransactions || [];
      txTbody.innerHTML = txs.length ? txs.map((tx) => {
        const sign = tx.transactionType === 'INBOUND' ? `+${tx.quantity}`
          : (tx.transactionType === 'OUTBOUND' || tx.transactionType === 'SALE') ? `-${tx.quantity}`
            : `±${tx.quantity}`;
        return `
          <tr>
            <td>${formatDateTime(tx.transactionDate)}</td>
            <td><span class="data-status ${txTypeClass(tx.transactionType)}">${escapeHtml(txTypeLabel(tx.transactionType))}</span></td>
            <td><strong>${escapeHtml(tx.productCode)}</strong> — ${escapeHtml(tx.productName)}</td>
            <td><strong>${escapeHtml(sign)}</strong></td>
            <td>${escapeHtml(tx.referenceType || '—')}${tx.referenceCode ? ` #${escapeHtml(tx.referenceCode)}` : ''}</td>
            <td>${escapeHtml(tx.createdBy || '—')}</td>
            <td>${escapeHtml(tx.note || '—')}</td>
          </tr>
        `;
      }).join('') : '<tr><td class="empty-cell" colspan="7">Chưa có giao dịch biến động tồn kho nào.</td></tr>';
    } catch (error) {
      showError(error);
    }
  };

  root.querySelector('#refresh-wh-dash').addEventListener('click', () => {
    clearAlerts();
    loadDashboard();
  });

  root.addEventListener('click', async (event) => {
    const viewBtn = event.target.closest('[data-dash-view-receipt]');
    if (viewBtn) {
      clearAlerts();
      try {
        const detail = await getPurchaseReceiptDetail(viewBtn.dataset.dashViewReceipt);
        renderReceiptDetail(detail);
      } catch (error) {
        showError(error);
      }
      return;
    }

    const confirmBtn = event.target.closest('[data-dash-confirm-receipt]');
    if (confirmBtn) {
      clearAlerts();
      confirmBtn.disabled = true;
      try {
        const updated = await confirmPurchaseReceipt(confirmBtn.dataset.dashConfirmReceipt);
        showSuccess(`Đã xác nhận phiếu nhập ${updated.receiptCode}, cộng tồn kho và ghi lịch sử INBOUND thành công!`);
        renderReceiptDetail(updated);
        await loadDashboard();
      } catch (error) {
        showError(error);
      } finally {
        confirmBtn.disabled = false;
      }
    }
  });

  await loadDashboard();
}
