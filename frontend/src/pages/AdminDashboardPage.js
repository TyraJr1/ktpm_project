import { ApiError, verifyAdminAccess } from '../api/client.js';
import { renderShell, showComingSoon } from '../components/AppShell.js';
import { dashboardPreviewData } from '../data/dashboardPreviewData.js';
import { displayRole } from '../utils/userDisplay.js';

const kpiIcons = {
  revenue: '<path d="M4 19V9M10 19V5M16 19v-7M3 19h18M17 6l3 3-3 3M20 9h-6"/>',
  orders: '<path d="M6 3h12l2 4v14H4V7zM4 7h16M9 11h6"/>',
  products: '<path d="m4 7 8-4 8 4-8 4zM4 7v10l8 4 8-4V7M12 11v10"/>',
  stock: '<path d="M12 9v4M12 17h.01M10.3 3.9 2.5 17.4A2 2 0 0 0 4.2 20h15.6a2 2 0 0 0 1.7-2.6L13.7 3.9a2 2 0 0 0-3.4 0Z"/>',
};

function formatDate() {
  const value = new Intl.DateTimeFormat('vi-VN', {
    weekday: 'long', day: '2-digit', month: 'long', year: 'numeric',
  }).format(new Date());
  return value.charAt(0).toUpperCase() + value.slice(1);
}

function renderKpis() {
  return dashboardPreviewData.kpis.map((item) => `
    <article class="kpi-card kpi-card--${item.key}">
      <div class="kpi-card__icon"><svg aria-hidden="true" viewBox="0 0 24 24">${kpiIcons[item.key]}</svg></div>
      <div><p>${item.label}</p><strong>${item.value}</strong><small>${item.secondary}</small></div>
    </article>`).join('');
}

function renderRevenueChart() {
  const max = Math.max(...dashboardPreviewData.weeklyRevenue.map(({ value }) => value));
  return dashboardPreviewData.weeklyRevenue.map(({ day, value }) => `
    <div class="chart-column">
      <span class="chart-column__value">${value.toLocaleString('vi-VN')}M</span>
      <div class="chart-column__track"><span style="height:${Math.round((value / max) * 100)}%"></span></div>
      <strong>${day}</strong>
    </div>`).join('');
}

export async function renderAdminDashboardPage(root, auth) {
  renderShell(root, auth, `
    <section class="dashboard-hero">
      <div>
        <p class="eyebrow eyebrow--light">Không gian quản lý</p>
        <h2>Xin chào, <span id="welcome-name"></span></h2>
        <p>Theo dõi nhanh hoạt động bán hàng và tình hình vận hành cửa hàng.</p>
      </div>
      <time class="dashboard-date" id="dashboard-date"></time>
    </section>
    <section class="kpi-grid" aria-label="Chỉ số tổng quan">
      ${renderKpis()}
    </section>
    <section class="dashboard-content">
      <article class="dashboard-panel revenue-panel">
        <div class="panel-heading"><div><h3>Doanh thu 7 ngày</h3><p>Tổng hợp theo ngày trong tuần</p></div><span>Triệu đồng</span></div>
        <div class="revenue-chart" role="img" aria-label="Biểu đồ doanh thu bảy ngày">${renderRevenueChart()}</div>
      </article>
      <article class="dashboard-panel top-products-panel">
        <div class="panel-heading"><div><h3>Sản phẩm bán chạy</h3><p>Số lượng bán trong tuần</p></div></div>
        <ol class="product-list">
          ${dashboardPreviewData.topProducts.map((item, index) => `<li><span class="list-rank">${String(index + 1).padStart(2, '0')}</span><span>${item.name}</span><strong>${item.quantity}</strong></li>`).join('')}
        </ol>
      </article>
      <article class="dashboard-panel stock-panel">
        <div class="panel-heading"><div><h3>Cảnh báo tồn kho</h3><p>Các mặt hàng cần theo dõi</p></div></div>
        <div class="stock-list">
          ${dashboardPreviewData.lowStock.map((item) => `<div><span>${item.name}</span><strong>${item.quantity} còn lại</strong></div>`).join('')}
        </div>
      </article>
      <article class="dashboard-panel quick-panel">
        <div class="panel-heading"><div><h3>Truy cập nhanh</h3><p>Các tác vụ thường dùng</p></div></div>
        <div class="quick-actions">
          ${dashboardPreviewData.quickActions.map((label) => `<button type="button" data-quick-action><span>+</span>${label}</button>`).join('')}
        </div>
      </article>
    </section>
    <div class="form-alert" id="protected-error" role="alert" hidden></div>
  `, { active: 'Tổng quan' });

  root.querySelector('#welcome-name').textContent = displayRole(auth.role);
  root.querySelector('#dashboard-date').textContent = formatDate();
  root.querySelectorAll('[data-quick-action]').forEach((button) => button.addEventListener('click', () => showComingSoon(root)));

  try {
    await verifyAdminAccess();
  } catch (error) {
    if (error instanceof ApiError && error.status === 403) {
      const alert = root.querySelector('#protected-error');
      alert.textContent = 'Bạn không có quyền truy cập chức năng này.';
      alert.hidden = false;
    }
  }
}
