import { ApiError, listSalesOrders, getSalesOrderDetail } from '../api/client.js';
import { renderSalesShell } from '../components/SalesShell.js';

let orders = [];

function renderTable(container) {
  if (orders.length === 0) {
    container.innerHTML = '<tr><td colspan="6" style="text-align:center;padding:1rem">Chưa có đơn hàng nào.</td></tr>';
    return;
  }
  container.innerHTML = orders.map((order) => `
    <tr>
      <td>#${order.id}</td>
      <td>${order.customerName || 'Khách lẻ'}</td>
      <td>${order.createdAt ? new Date(order.createdAt).toLocaleString('vi-VN') : '—'}</td>
      <td style="font-weight:600;color:var(--primary,#2563eb)">${(order.totalAmount ?? 0).toLocaleString('vi-VN')} đ</td>
      <td><span class="badge ${order.status === 'COMPLETED' ? 'badge--success' : 'badge--warning'}">${order.status || '—'}</span></td>
      <td><button class="button button--ghost" data-detail-id="${order.id}" type="button">Chi tiết</button></td>
    </tr>`).join('');
}

function openDetailModal(root, orderDetail) {
  let overlay = root.querySelector('#order-detail-overlay');
  if (overlay) overlay.remove();

  const items = orderDetail.items || [];
  const itemsHtml = items.length === 0
    ? '<p style="text-align:center;color:#888">Không có sản phẩm</p>'
    : items.map((item) => `
      <div style="display:flex;justify-content:space-between;padding:.5rem 0;border-bottom:1px solid #eee">
        <span>${item.productName || 'Sản phẩm'} (x${item.quantity})</span>
        <span>${((item.price ?? 0) * item.quantity).toLocaleString('vi-VN')} đ</span>
      </div>`).join('');

  overlay = document.createElement('div');
  overlay.id = 'order-detail-overlay';
  overlay.style.cssText = 'position:fixed;inset:0;background:rgba(0,0,0,.45);display:flex;align-items:center;justify-content:center;z-index:1000';
  overlay.innerHTML = `
    <div style="background:#fff;border-radius:8px;padding:1.5rem;width:30rem;max-width:95vw">
      <h2 style="margin:0 0 1rem">Chi tiết đơn #${orderDetail.id}</h2>
      <p style="margin-bottom:.25rem"><strong>Khách hàng:</strong> ${orderDetail.customerName || 'Khách lẻ'}</p>
      <p style="margin-bottom:1rem"><strong>SĐT:</strong> ${orderDetail.customerPhone || '—'}</p>
      <div style="border-top:1px solid #ddd;padding-top:.75rem;margin-bottom:.75rem;max-height:15rem;overflow-y:auto">${itemsHtml}</div>
      <div style="display:flex;justify-content:space-between;font-weight:700;font-size:1.1rem;margin-bottom:1rem">
        <span>Tổng cộng:</span>
        <span style="color:var(--primary,#2563eb)">${(orderDetail.totalAmount ?? 0).toLocaleString('vi-VN')} đ</span>
      </div>
      <div style="display:flex;justify-content:flex-end">
        <button class="button button--ghost" id="btn-close-detail" type="button">Đóng</button>
      </div>
    </div>`;

  root.appendChild(overlay);

  overlay.querySelector('#btn-close-detail').addEventListener('click', () => overlay.remove());
  overlay.addEventListener('click', (e) => { if (e.target === overlay) overlay.remove(); });
}

export async function renderOrderHistoryPage(root, auth) {
  orders = [];

  renderSalesShell(root, auth, `
    <div style="overflow-x:auto;background:#fff;border-radius:8px;box-shadow:0 1px 3px rgba(0,0,0,.1)">
      <table class="data-table" style="width:100%">
        <thead><tr><th>Mã đơn</th><th>Khách hàng</th><th>Ngày tạo</th><th>Tổng tiền</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
        <tbody id="orders-tbody"><tr><td colspan="6" style="text-align:center;padding:1rem">Đang tải…</td></tr></tbody>
      </table>
    </div>
    <div class="form-alert" id="page-error" role="alert" hidden></div>
  `, { active: 'Lịch sử đơn' });

  // Load orders
  try {
    const data = await listSalesOrders({});
    orders = data.content || data || [];
    const tbody = root.querySelector('#orders-tbody');
    if (tbody) renderTable(tbody);
  } catch (error) {
    const alert = root.querySelector('#page-error');
    alert.textContent = error instanceof ApiError ? error.message : 'Không thể tải lịch sử đơn hàng.';
    alert.hidden = false;
  }

  // Detail click (delegation)
  root.addEventListener('click', async (e) => {
    const detailBtn = e.target.closest('[data-detail-id]');
    if (!detailBtn) return;
    const id = Number(detailBtn.dataset.detailId);
    detailBtn.disabled = true;
    detailBtn.textContent = 'Đang tải…';
    try {
      const detail = await getSalesOrderDetail(id);
      openDetailModal(root, detail);
    } catch (error) {
      const alert = root.querySelector('#page-error');
      alert.textContent = error instanceof ApiError ? error.message : 'Không thể tải chi tiết đơn hàng.';
      alert.hidden = false;
    } finally {
      detailBtn.disabled = false;
      detailBtn.textContent = 'Chi tiết';
    }
  });
}
