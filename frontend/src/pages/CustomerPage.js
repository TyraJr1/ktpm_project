import {
  ApiError,
  getCustomerPurchaseHistory,
  listCustomers,
  saveCustomer,
} from '../api/client.js';
import { renderSalesShell } from '../components/SalesShell.js';
import { escapeHtml, formatCurrency, formatDateTime } from '../utils/format.js';

function paymentLabel(method) {
  const map = { CASH: 'Tiền mặt', TRANSFER: 'Chuyển khoản', CARD: 'Thẻ' };
  return map[method] || method || 'Tiền mặt';
}

export async function renderCustomerPage(root, auth) {
  renderSalesShell(root, auth, `
    <section class="admin-page">
      <header class="admin-page__heading">
        <div>
          <h2>Quản lý khách hàng & Lịch sử mua hàng</h2>
          <p>Thêm mới, cập nhật thông tin liên hệ, tìm kiếm khách hàng và xem lịch sử đơn mua (FR-CUS-01..04).</p>
        </div>
        <button class="button button--primary" type="button" id="new-customer-btn">Thêm khách hàng</button>
      </header>

      <div class="form-alert" id="customer-error" role="alert" hidden></div>

      <form class="data-editor" id="customer-form" hidden>
        <input type="hidden" name="id">
        <div class="editor-grid">
          <label>Họ và tên khách hàng *<input name="customerName" maxlength="150" required placeholder="Nguyễn Văn An"></label>
          <label>Số điện thoại<input name="phone" maxlength="20" placeholder="0901234567"></label>
          <label>Email<input name="email" type="email" maxlength="150" placeholder="khachhang@email.com"></label>
          <label>Địa chỉ<input name="address" maxlength="255" placeholder="Số nhà, đường, quận/huyện..."></label>
        </div>
        <div class="editor-actions">
          <button class="button button--ghost" type="button" data-cancel-customer>Hủy</button>
          <button class="button button--primary" type="submit">Lưu thông tin</button>
        </div>
      </form>

      <form class="data-filter" id="customer-filter">
        <input name="query" placeholder="Tìm theo họ tên, số điện thoại hoặc email...">
        <button class="button button--ghost" type="submit">Tìm kiếm</button>
        <button class="button button--ghost" type="button" id="customer-filter-reset">Tất cả</button>
      </form>

      <div class="data-panel">
        <div class="data-table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>Mã KH</th>
                <th>Họ tên khách hàng</th>
                <th>Số điện thoại</th>
                <th>Email</th>
                <th>Địa chỉ</th>
                <th>Ngày tạo</th>
                <th></th>
              </tr>
            </thead>
            <tbody id="customer-rows">
              <tr><td class="empty-cell" colspan="7">Đang tải danh sách khách hàng...</td></tr>
            </tbody>
          </table>
        </div>
        <p class="data-count" id="customer-count"></p>
      </div>

      <section class="invoice-detail" id="customer-history-panel" hidden></section>
    </section>
  `, { active: 'Khách hàng' });

  const form = root.querySelector('#customer-form');
  const filter = root.querySelector('#customer-filter');
  const alertEl = root.querySelector('#customer-error');
  const historyPanel = root.querySelector('#customer-history-panel');
  let records = [];

  const showError = (error) => {
    alertEl.textContent = error instanceof ApiError ? error.message : 'Không thể xử lý yêu cầu.';
    alertEl.hidden = false;
  };

  const clearError = () => {
    alertEl.hidden = true;
  };

  const load = async () => {
    clearError();
    try {
      const data = await listCustomers({ query: filter.elements.query.value, size: 100 });
      records = data.content || [];
      root.querySelector('#customer-count').textContent = `${(data.totalElements ?? records.length).toLocaleString('vi-VN')} khách hàng`;
      root.querySelector('#customer-rows').innerHTML = records.length ? records.map((item) => `
        <tr>
          <td><strong>#${item.id}</strong></td>
          <td><strong>${escapeHtml(item.customerName)}</strong></td>
          <td>${escapeHtml(item.phone || '—')}</td>
          <td>${escapeHtml(item.email || '—')}</td>
          <td>${escapeHtml(item.address || '—')}</td>
          <td>${formatDateTime(item.createdAt)}</td>
          <td>
            <div style="display:flex;gap:8px;">
              <button class="table-action" type="button" data-customer-edit="${item.id}">Sửa</button>
              <button class="table-action" type="button" data-customer-history="${item.id}">Lịch sử mua</button>
            </div>
          </td>
        </tr>
      `).join('') : '<tr><td class="empty-cell" colspan="7">Không có khách hàng phù hợp.</td></tr>';
    } catch (error) {
      showError(error);
    }
  };

  const openEditor = (item = null) => {
    form.hidden = false;
    form.elements.id.value = item?.id || '';
    form.elements.customerName.value = item?.customerName || '';
    form.elements.phone.value = item?.phone || '';
    form.elements.email.value = item?.email || '';
    form.elements.address.value = item?.address || '';
    form.elements.customerName.focus();
  };

  const openHistory = async (customerId) => {
    clearError();
    const customer = records.find((c) => c.id === customerId);
    try {
      const orders = await getCustomerPurchaseHistory(customerId);
      historyPanel.hidden = false;
      historyPanel.innerHTML = `
        <div class="invoice-detail__heading">
          <div>
            <p class="eyebrow">Lịch sử mua hàng của khách hàng (FR-CUS-04)</p>
            <h3>${escapeHtml(customer ? customer.customerName : `Khách hàng #${customerId}`)}${customer?.phone ? ` — ${escapeHtml(customer.phone)}` : ''}</h3>
          </div>
          <strong>${orders.length} đơn mua hàng</strong>
        </div>
        ${!orders.length ? '<p class="empty-cell">Khách hàng này chưa có giao dịch mua hàng nào.</p>' : orders.map((order) => `
          <div style="border:1px solid #e2e8f0;border-radius:10px;padding:14px;margin-bottom:14px;">
            <dl class="invoice-meta" style="margin-bottom:10px;">
              <div><dt>Mã đơn hàng</dt><dd><strong>${escapeHtml(order.orderCode)}</strong></dd></div>
              <div><dt>Mã hóa đơn</dt><dd><strong>${escapeHtml(order.invoiceCode || '—')}</strong></dd></div>
              <div><dt>Ngày mua</dt><dd>${formatDateTime(order.orderDate)}</dd></div>
              <div><dt>Thanh toán</dt><dd>${escapeHtml(paymentLabel(order.paymentMethod))}</dd></div>
              <div><dt>Thu ngân</dt><dd>${escapeHtml(order.createdBy || '—')}</dd></div>
              <div><dt>Tổng tiền</dt><dd><strong>${formatCurrency(order.totalAmount)}</strong></dd></div>
            </dl>
            <div class="data-table-wrap">
              <table class="data-table">
                <thead>
                  <tr><th>Mã SP</th><th>Tên sản phẩm</th><th>Số lượng</th><th>Đơn giá</th><th>Thành tiền</th></tr>
                </thead>
                <tbody>
                  ${(order.items || []).map((line) => `
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
          </div>
        `).join('')}
      `;
      historyPanel.scrollIntoView({ behavior: 'smooth', block: 'start' });
    } catch (error) {
      showError(error);
    }
  };

  root.querySelector('#new-customer-btn').addEventListener('click', () => openEditor());

  root.addEventListener('click', (event) => {
    const editBtn = event.target.closest('[data-customer-edit]');
    if (editBtn) {
      openEditor(records.find((item) => item.id === Number(editBtn.dataset.customerEdit)));
      return;
    }
    const historyBtn = event.target.closest('[data-customer-history]');
    if (historyBtn) {
      openHistory(Number(historyBtn.dataset.customerHistory));
      return;
    }
    if (event.target.closest('[data-cancel-customer]')) {
      form.hidden = true;
    }
  });

  filter.addEventListener('submit', (event) => {
    event.preventDefault();
    load();
  });

  root.querySelector('#customer-filter-reset').addEventListener('click', () => {
    filter.elements.query.value = '';
    load();
  });

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    clearError();
    try {
      await saveCustomer(form.elements.id.value || null, {
        customerName: form.elements.customerName.value,
        phone: form.elements.phone.value,
        email: form.elements.email.value,
        address: form.elements.address.value,
      });
      form.hidden = true;
      await load();
    } catch (error) {
      showError(error);
    }
  });

  await load();
}
