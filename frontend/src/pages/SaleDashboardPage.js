import { ApiError, getSalesDashboard } from '../api/client.js';
import { renderSalesShell } from '../components/SalesShell.js';
import { displayName } from '../utils/userDisplay.js';

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
        <p class="eyebrow eyebrow--light">Tổng quan bán hàng</p>
        <h2>Xin chào, <span id="welcome-name"></span></h2>
        <p>Dữ liệu bán hàng được cập nhật trực tiếp từ hệ thống.</p>
      </div>
      <time class="dashboard-date" id="dashboard-date"></time>
    </section>
    <section class="module-grid" aria-label="Chỉ số bán hàng">
      <div class="module-card">
        <p>Doanh thu hôm nay</p><strong id="stat-daily-sales">—</strong><small>VNĐ</small>
      </div>
      <div class="module-card">
        <p>Đơn hàng chờ xử lý</p><strong id="stat-pending-orders">—</strong><small>Đơn hàng</small>
      </div>
    </section>
    <section class="dashboard-panel" style="margin-top:1.5rem">
      <div class="panel-heading"><div><h3>Đơn hàng gần đây</h3></div></div>
      <div style="overflow-x:auto">
        <table class="data-table" style="width:100%">
          <thead><tr><th>Mã đơn</th><th>Khách hàng</th><th>Tổng tiền</th><th>Trạng thái</th></tr></thead>
          <tbody id="recent-orders-body"><tr><td colspan="4" style="text-align:center;padding:1rem">Đang tải…</td></tr></tbody>
        </table>
      </div>
    </section>
    <div class="form-alert" id="dashboard-error" role="alert" hidden></div>
  `, { active: 'Tổng quan' });

  root.querySelector('#welcome-name').textContent = displayName(auth);
  root.querySelector('#dashboard-date').textContent = formatDate();

  try {
    const data = await getSalesDashboard();
    const stats = data.stats || data;
    const recentOrders = data.recentOrders || [];

    root.querySelector('#stat-daily-sales').textContent =
      (stats.dailySales ?? 0).toLocaleString('vi-VN');
    root.querySelector('#stat-pending-orders').textContent =
      (stats.pendingOrders ?? 0).toLocaleString('vi-VN');

    const tbody = root.querySelector('#recent-orders-body');
    if (recentOrders.length === 0) {
      tbody.innerHTML = '<tr><td colspan="4" style="text-align:center;padding:1rem">Chưa có đơn hàng nào.</td></tr>';
    } else {
      tbody.innerHTML = recentOrders.map((order) => `
        <tr>
          <td>#${order.id}</td>
          <td>${order.customerName || 'Khách lẻ'}</td>
          <td>${(order.totalAmount ?? 0).toLocaleString('vi-VN')} đ</td>
          <td><span class="badge ${order.status === 'COMPLETED' ? 'badge--success' : 'badge--warning'}">${order.status}</span></td>
        </tr>`).join('');
    }
  } catch (error) {
    const alert = root.querySelector('#dashboard-error');
    alert.textContent = error instanceof ApiError ? error.message : 'Không thể tải dữ liệu tổng quan.';
    alert.hidden = false;
  }
}
