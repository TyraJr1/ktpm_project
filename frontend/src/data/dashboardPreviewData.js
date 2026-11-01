// Presentation-only dashboard preview data. Replace with reporting API when implemented.
export const dashboardPreviewData = {
  kpis: [
    { key: 'revenue', label: 'Doanh thu hôm nay', value: '2.450.000 ₫', secondary: '+8,4% so với hôm qua' },
    { key: 'orders', label: 'Đơn hàng hôm nay', value: '18', secondary: 'Hoạt động bán hàng trong ngày' },
    { key: 'products', label: 'Tổng sản phẩm', value: '126', secondary: 'Sản phẩm đang quản lý' },
    { key: 'stock', label: 'Sắp hết hàng', value: '7', secondary: 'Cần kiểm tra tồn kho' },
  ],
  weeklyRevenue: [
    { day: 'T2', value: 1.8 }, { day: 'T3', value: 2.1 }, { day: 'T4', value: 1.6 },
    { day: 'T5', value: 2.7 }, { day: 'T6', value: 3.1 }, { day: 'T7', value: 3.8 }, { day: 'CN', value: 2.45 },
  ],
  topProducts: [
    { name: 'Bút bi Thiên Long TL-027', quantity: 42 }, { name: 'Giấy A4 Double A', quantity: 28 },
    { name: 'Vở Campus 200 trang', quantity: 21 }, { name: 'Bút highlight Stabilo', quantity: 17 },
    { name: 'File hồ sơ A4', quantity: 14 },
  ],
  lowStock: [
    { name: 'Bút lông bảng', quantity: 4 }, { name: 'Giấy note 3x3', quantity: 6 },
    { name: 'Mực dấu đỏ', quantity: 3 }, { name: 'Bìa nút A4', quantity: 5 },
  ],
  quickActions: ['Thêm sản phẩm', 'Nhập hàng', 'Tạo đơn hàng', 'Xem báo cáo'],
};
