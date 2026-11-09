import {
  ApiError,
  getSalesInvoiceDetail,
  listSalesInvoices,
} from '../api/client.js';
import { renderSalesShell } from '../components/SalesShell.js';
import { escapeHtml, formatCurrency, formatDateTime } from '../utils/format.js';

function paymentLabel(method) {
  const map = { CASH: 'Tiền mặt', TRANSFER: 'Chuyển khoản', CARD: 'Thẻ' };
  return map[method] || method || 'Tiền mặt';
}

export async function renderOrderHistoryPage(root, auth) {
  renderSalesShell(root, auth, `
    <section class="admin-page">
      <header class="admin-page__heading">
        <div>
          <h2>Tra cứu hóa đơn & Lịch sử đơn bán hàng</h2>
          <p>Tìm kiếm hóa đơn theo mã hoặc khoảng thời gian và xem chi tiết từng dòng sản phẩm (FR-BIL-02..04).</p>
        </div>
        <a class="button button--primary" href="/sales/orders" data-link>+ Lập đơn & Hóa đơn mới</a>
      </header>

      <div class="form-alert" id="sales-invoice-error" role="alert" hidden></div>

      <form class="data-filter data-filter--dates" id="sales-invoice-filter">
        <input name="code" placeholder="Mã hóa đơn (INV-...) hoặc mã đơn (ORD-...)">
        <label>Từ ngày<input name="fromDate" type="date"></label>
        <label>Đến ngày<input name="toDate" type="date"></label>
        <button class="button button--ghost" type="submit">Tra cứu</button>
        <button class="button button--ghost" type="button" id="sales-invoice-reset">Tất cả</button>
      </form>

      <div class="data-panel">
        <div class="data-table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>Mã hóa đơn</th>
                <th>Ngày lập</th>
                <th>Mã đơn hàng</th>
                <th>Tổng tiền</th>
                <th>Thanh toán</th>
                <th>Trạng thái</th>
                <th></th>
              </tr>
            </thead>
            <tbody id="sales-invoice-rows">
              <tr><td class="empty-cell" colspan="7">Đang tải danh sách hóa đơn...</td></tr>
            </tbody>
          </table>
        </div>
        <p class="data-count" id="sales-invoice-count"></p>
      </div>

      <section class="invoice-detail" id="sales-invoice-detail" hidden></section>
    </section>
  `, { active: 'Lịch sử đơn' });

  const filter = root.querySelector('#sales-invoice-filter');
  const alertEl = root.querySelector('#sales-invoice-error');
  const detailEl = root.querySelector('#sales-invoice-detail');

  const showError = (error) => {
    alertEl.textContent = error instanceof ApiError ? error.message : String(error || 'Không thể xử lý yêu cầu.');
    alertEl.hidden = false;
  };

  const clearError = () => {
    alertEl.hidden = true;
  };

  const load = async () => {
    clearError();
    const code = filter.elements.code.value.trim();
    const fromDate = filter.elements.fromDate.value;
    const toDate = filter.elements.toDate.value;

    if (fromDate && toDate && fromDate > toDate) {
      showError('Từ ngày không được lớn hơn đến ngày.');
      return;
    }

    try {
      const data = await listSalesInvoices({ code, fromDate, toDate, size: 100 });
      const list = data.content || [];
      root.querySelector('#sales-invoice-count').textContent = `${(data.totalElements ?? list.length).toLocaleString('vi-VN')} hóa đơn`;
      root.querySelector('#sales-invoice-rows').innerHTML = list.length ? list.map((item) => `
        <tr>
          <td><strong>${escapeHtml(item.invoiceCode)}</strong></td>
          <td>${formatDateTime(item.invoiceDate)}</td>
          <td>${escapeHtml(item.orderCode)}</td>
          <td><strong>${formatCurrency(item.totalAmount)}</strong></td>
          <td>${escapeHtml(paymentLabel(item.paymentMethod))}</td>
          <td><span class="data-status is-active">${escapeHtml(item.status)}</span></td>
          <td><button class="table-action" type="button" data-sales-invoice-detail="${item.id}">Chi tiết</button></td>
        </tr>
      `).join('') : '<tr><td class="empty-cell" colspan="7">Không có hóa đơn phù hợp.</td></tr>';
    } catch (error) {
      showError(error);
    }
  };

  filter.addEventListener('submit', (event) => {
    event.preventDefault();
    load();
  });

  root.querySelector('#sales-invoice-reset').addEventListener('click', () => {
    filter.elements.code.value = '';
    filter.elements.fromDate.value = '';
    filter.elements.toDate.value = '';
    load();
  });

  root.addEventListener('click', async (event) => {
    const button = event.target.closest('[data-sales-invoice-detail]');
    if (!button) return;
    clearError();
    try {
      const item = await getSalesInvoiceDetail(button.dataset.salesInvoiceDetail);
      detailEl.hidden = false;
      detailEl.innerHTML = `
        <div class="invoice-detail__heading">
          <div>
            <p class="eyebrow">Chi tiết hóa đơn bán hàng (FR-BIL-04)</p>
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

  await load();
}
