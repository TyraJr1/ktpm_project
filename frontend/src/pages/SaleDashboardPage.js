import {
  ApiError,
  getSalesDashboard,
  getSalesInvoiceDetail,
  verifySalesAccess,
} from '../api/client.js';
import { renderSalesShell } from '../components/SalesShell.js';
import { escapeHtml, formatCurrency, formatDateTime } from '../utils/format.js';
import { displayName } from '../utils/userDisplay.js';

function paymentLabel(method) {
  const map = { CASH: 'Tiền mặt', TRANSFER: 'Chuyển khoản', CARD: 'Thẻ' };
  return map[method] || method || 'Tiền mặt';
}

function formatDate() {
  const value = new Intl.DateTimeFormat('vi-VN', {
    weekday: 'long', day: '2-digit', month: 'long', year: 'numeric',
  }).format(new Date());
  return value.charAt(0).toUpperCase() + value.slice(1);
}

export async function renderSalesDashboardPage(root, auth) {
  renderSalesShell(root, auth, `
    <section class="dashboard-hero">
      <div>
        <p class="eyebrow eyebrow--light">Không gian làm việc Nhân viên bán hàng</p>
        <h2>Xin chào, <span id="sales-welcome-name"></span></h2>
        <p>Theo dõi kết quả giao dịch trong ngày, lập đơn bán hàng kiêm xuất hóa đơn và quản lý hồ sơ khách hàng.</p>
      </div>
      <time class="dashboard-date" id="sales-dashboard-date"></time>
    </section>

    <div class="form-alert" id="sales-dash-error" role="alert" hidden></div>

    <section class="module-grid" aria-label="Tổng quan bán hàng hôm nay">
      <a class="module-card" href="/sales/orders" data-link>
        <p>Doanh thu hôm nay</p>
        <strong id="kpi-today-revenue">0 ₫</strong>
        <small>Tổng tiền hóa đơn hoàn tất trong ngày</small>
      </a>
      <a class="module-card" href="/sales/order-history" data-link>
        <p>Hóa đơn hôm nay</p>
        <strong id="kpi-today-invoices">0</strong>
        <small>Số giao dịch đã thanh toán & xuất hóa đơn</small>
      </a>
      <a class="module-card" href="/sales/orders" data-link>
        <p>Sản phẩm đang bán</p>
        <strong id="kpi-active-products">0</strong>
        <small>Mặt hàng ACTIVE sẵn sàng giao dịch tại quầy</small>
      </a>
      <a class="module-card" href="/sales/customers" data-link>
        <p>Khách hàng đã lưu</p>
        <strong id="kpi-total-customers">0</strong>
        <small>Hồ sơ khách hàng tích lũy lịch sử mua</small>
      </a>
    </section>

    <section class="admin-page" style="margin-top:20px;">
      <header class="admin-page__heading">
        <div>
          <h2>Hóa đơn vừa lập gần nhất</h2>
          <p>Tra cứu nhanh các giao dịch bán hàng vừa hoàn tất hoặc chuyển sang quầy POS để tạo đơn mới.</p>
        </div>
        <div style="display:flex;gap:8px;">
          <button class="button button--ghost" type="button" id="refresh-sales-dash">Làm mới</button>
          <a class="button button--primary" href="/sales/orders" data-link>+ Lập đơn & Hóa đơn mới</a>
        </div>
      </header>

      <div class="data-panel">
        <div class="data-table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>Mã hóa đơn</th>
                <th>Mã đơn hàng</th>
                <th>Thời gian lập</th>
                <th>Thanh toán</th>
                <th>Tổng tiền</th>
                <th>Trạng thái</th>
                <th></th>
              </tr>
            </thead>
            <tbody id="recent-invoices-tbody">
              <tr><td class="empty-cell" colspan="7">Đang tải dữ liệu...</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <section class="invoice-detail" id="dash-invoice-detail" hidden></section>
    </section>
  `, { active: 'Tổng quan' });

  root.querySelector('#sales-welcome-name').textContent = displayName(auth);
  root.querySelector('#sales-dashboard-date').textContent = formatDate();

  const alertEl = root.querySelector('#sales-dash-error');
  const tbodyEl = root.querySelector('#recent-invoices-tbody');
  const detailEl = root.querySelector('#dash-invoice-detail');

  const showError = (error) => {
    alertEl.textContent = error instanceof ApiError ? error.message : 'Không thể tải dữ liệu tổng quan.';
    alertEl.hidden = false;
  };

  const loadDashboard = async () => {
    alertEl.hidden = true;
    try {
      await verifySalesAccess();
      const data = await getSalesDashboard();
      root.querySelector('#kpi-today-revenue').textContent = formatCurrency(data.todayRevenue || 0);
      root.querySelector('#kpi-today-invoices').textContent = Number(data.todayInvoiceCount || 0).toLocaleString('vi-VN');
      root.querySelector('#kpi-active-products').textContent = Number(data.activeProductCount || 0).toLocaleString('vi-VN');
      root.querySelector('#kpi-total-customers').textContent = Number(data.totalCustomerCount || 0).toLocaleString('vi-VN');

      const invoices = data.recentInvoices || [];
      tbodyEl.innerHTML = invoices.length ? invoices.map((inv) => `
        <tr>
          <td><strong>${escapeHtml(inv.invoiceCode)}</strong></td>
          <td>${escapeHtml(inv.orderCode)}</td>
          <td>${formatDateTime(inv.invoiceDate)}</td>
          <td>${escapeHtml(paymentLabel(inv.paymentMethod))}</td>
          <td><strong>${formatCurrency(inv.totalAmount)}</strong></td>
          <td><span class="data-status is-active">${escapeHtml(inv.status)}</span></td>
          <td><button class="table-action" type="button" data-dash-invoice="${inv.id}">Chi tiết</button></td>
        </tr>
      `).join('') : '<tr><td class="empty-cell" colspan="7">Chưa có hóa đơn nào được lập.</td></tr>';
    } catch (error) {
      showError(error);
    }
  };

  root.querySelector('#refresh-sales-dash').addEventListener('click', loadDashboard);

  root.addEventListener('click', async (event) => {
    const btn = event.target.closest('[data-dash-invoice]');
    if (!btn) return;
    alertEl.hidden = true;
    try {
      const item = await getSalesInvoiceDetail(btn.dataset.dashInvoice);
      detailEl.hidden = false;
      detailEl.innerHTML = `
        <div class="invoice-detail__heading">
          <div>
            <p class="eyebrow">Chi tiết hóa đơn</p>
            <h3>${escapeHtml(item.invoiceCode)}</h3>
          </div>
          <strong>${formatCurrency(item.totalAmount)}</strong>
        </div>
        <dl class="invoice-meta">
          <div><dt>Đơn hàng</dt><dd>${escapeHtml(item.orderCode)}</dd></div>
          <div><dt>Khách hàng</dt><dd>${escapeHtml(item.customerName || 'Khách lẻ')}</dd></div>
          <div><dt>Nhân viên</dt><dd>${escapeHtml(item.createdBy || '—')}</dd></div>
          <div><dt>Thanh toán</dt><dd>${escapeHtml(paymentLabel(item.paymentMethod))}</dd></div>
          <div><dt>Ngày lập</dt><dd>${formatDateTime(item.invoiceDate)}</dd></div>
        </dl>
        <div class="data-table-wrap">
          <table class="data-table">
            <thead>
              <tr><th>Mã SP</th><th>Sản phẩm</th><th>Số lượng</th><th>Đơn giá</th><th>Thành tiền</th></tr>
            </thead>
            <tbody>
              ${(item.lines || []).map((line) => `
                <tr>
                  <td>${escapeHtml(line.productCode)}</td>
                  <td>${escapeHtml(line.productName)}</td>
                  <td>${line.quantity}</td>
                  <td>${formatCurrency(line.unitPrice)}</td>
                  <td><strong>${formatCurrency(line.lineTotal)}</strong></td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </div>
      `;
      detailEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
    } catch (error) {
      showError(error);
    }
  });

  await loadDashboard();
}
