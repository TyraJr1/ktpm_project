import {
  ApiError, getInvoiceDetail, listCategories, listInventory, listInvoices, listProducts,
  listSuppliers, saveCategory, saveProduct, saveSupplier, updateProductStatus,
} from '../api/client.js';
import { renderShell } from '../components/AppShell.js';

const esc = (value) => String(value ?? '')
  .replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;')
  .replaceAll('"', '&quot;').replaceAll("'", '&#039;');
const money = (value) => new Intl.NumberFormat('vi-VN', {
  style: 'currency', currency: 'VND', maximumFractionDigits: 0,
}).format(Number(value || 0));
const dateTime = (value) => value ? new Intl.DateTimeFormat('vi-VN', {
  dateStyle: 'short', timeStyle: 'short',
}).format(new Date(value)) : '—';

function statusBadge(status) {
  const active = status === 'ACTIVE';
  return `<span class="data-status ${active ? 'is-active' : 'is-inactive'}">${active ? 'Hoạt động' : 'Ngừng hoạt động'}</span>`;
}

function pageLayout(title, description, body, action = '') {
  return `
    <section class="admin-page">
      <header class="admin-page__heading"><div><h2>${title}</h2><p>${description}</p></div>${action}</header>
      <div class="form-alert" id="page-error" role="alert" hidden></div>
      ${body}
    </section>`;
}

function showError(root, error) {
  const alert = root.querySelector('#page-error');
  alert.textContent = error instanceof ApiError ? error.message : 'Không thể xử lý yêu cầu.';
  alert.hidden = false;
}

function clearError(root) {
  const alert = root.querySelector('#page-error');
  if (alert) alert.hidden = true;
}

function editorActions() {
  return '<div class="editor-actions"><button class="button button--ghost" type="button" data-cancel>Hủy</button><button class="button button--primary" type="submit">Lưu</button></div>';
}

export async function renderCategoryPage(root, auth) {
  renderShell(root, auth, pageLayout('Danh mục sản phẩm', 'Quản lý danh mục đang lưu trong cơ sở dữ liệu.', `
    <form class="data-editor" id="category-form" hidden>
      <input type="hidden" name="id">
      <div class="editor-grid">
        <label>Tên danh mục<input name="categoryName" maxlength="150" required></label>
        <label>Trạng thái<select name="status"><option value="ACTIVE">Hoạt động</option><option value="INACTIVE">Ngừng hoạt động</option></select></label>
        <label class="editor-grid__wide">Mô tả<textarea name="description" maxlength="255" rows="3"></textarea></label>
      </div>${editorActions()}
    </form>
    <div class="data-panel"><div class="data-table-wrap"><table class="data-table"><thead><tr><th>Tên danh mục</th><th>Mô tả</th><th>Trạng thái</th><th></th></tr></thead><tbody id="category-rows"></tbody></table></div></div>
  `, '<button class="button button--primary" type="button" id="new-category">Thêm danh mục</button>'), { active: 'Danh mục' });

  const form = root.querySelector('#category-form');
  let records = [];
  const load = async () => {
    clearError(root);
    try {
      records = await listCategories();
      root.querySelector('#category-rows').innerHTML = records.length ? records.map((item) => `
        <tr><td><strong>${esc(item.categoryName)}</strong></td><td>${esc(item.description || '—')}</td><td>${statusBadge(item.status)}</td><td><button class="table-action" data-edit="${item.id}">Sửa</button></td></tr>`).join('')
        : '<tr><td class="empty-cell" colspan="4">Chưa có danh mục.</td></tr>';
    } catch (error) { showError(root, error); }
  };
  const open = (item = null) => {
    form.hidden = false;
    form.elements.id.value = item?.id || '';
    form.elements.categoryName.value = item?.categoryName || '';
    form.elements.description.value = item?.description || '';
    form.elements.status.value = item?.status || 'ACTIVE';
    form.elements.categoryName.focus();
  };
  root.querySelector('#new-category').addEventListener('click', () => open());
  root.addEventListener('click', (event) => {
    const button = event.target.closest('[data-edit]');
    if (button) open(records.find((item) => item.id === Number(button.dataset.edit)));
    if (event.target.closest('[data-cancel]')) form.hidden = true;
  });
  form.addEventListener('submit', async (event) => {
    event.preventDefault(); clearError(root);
    try {
      await saveCategory(form.elements.id.value || null, {
        categoryName: form.elements.categoryName.value,
        description: form.elements.description.value,
        status: form.elements.status.value,
      });
      form.hidden = true; await load();
    } catch (error) { showError(root, error); }
  });
  await load();
}

export async function renderProductPage(root, auth) {
  renderShell(root, auth, pageLayout('Sản phẩm', 'Tạo, cập nhật, tìm kiếm và thay đổi trạng thái sản phẩm.', `
    <form class="data-editor" id="product-form" hidden>
      <input type="hidden" name="id">
      <div class="editor-grid editor-grid--three">
        <label>Mã sản phẩm<input name="productCode" maxlength="50" required></label>
        <label>Tên sản phẩm<input name="productName" maxlength="200" required></label>
        <label>Danh mục<select name="categoryId" required></select></label>
        <label>Giá bán<input name="salePrice" type="number" min="0" step="0.01" required></label>
        <label>Trạng thái<select name="status"><option value="ACTIVE">Hoạt động</option><option value="INACTIVE">Ngừng hoạt động</option></select></label>
        <label class="editor-grid__wide">Mô tả<textarea name="description" maxlength="500" rows="3"></textarea></label>
      </div>${editorActions()}
    </form>
    <form class="data-filter" id="product-filter"><input name="query" placeholder="Tìm theo mã hoặc tên"><select name="status"><option value="">Tất cả trạng thái</option><option value="ACTIVE">Hoạt động</option><option value="INACTIVE">Ngừng hoạt động</option></select><button class="button button--ghost">Tìm kiếm</button></form>
    <div class="data-panel"><div class="data-table-wrap"><table class="data-table"><thead><tr><th>Mã</th><th>Sản phẩm</th><th>Danh mục</th><th>Giá bán</th><th>Tồn</th><th>Trạng thái</th><th></th></tr></thead><tbody id="product-rows"></tbody></table></div><p class="data-count" id="product-count"></p></div>
  `, '<button class="button button--primary" type="button" id="new-product">Thêm sản phẩm</button>'), { active: 'Sản phẩm' });

  const form = root.querySelector('#product-form');
  const filter = root.querySelector('#product-filter');
  let categories = [];
  let records = [];
  const load = async () => {
    clearError(root);
    try {
      const data = await listProducts({ query: filter.elements.query.value, status: filter.elements.status.value, size: 100 });
      records = data.content;
      root.querySelector('#product-count').textContent = `${data.totalElements.toLocaleString('vi-VN')} sản phẩm`;
      root.querySelector('#product-rows').innerHTML = records.length ? records.map((item) => `
        <tr><td><strong>${esc(item.productCode)}</strong></td><td>${esc(item.productName)}</td><td>${esc(item.categoryName)}</td><td>${money(item.salePrice)}</td><td>${item.stockQuantity}</td><td>${statusBadge(item.status)}</td><td class="table-actions"><button class="table-action" data-product-edit="${item.id}">Sửa</button><button class="table-action" data-product-status="${item.id}" data-status="${item.status}">${item.status === 'ACTIVE' ? 'Ngừng' : 'Kích hoạt'}</button></td></tr>`).join('')
        : '<tr><td class="empty-cell" colspan="7">Không có sản phẩm phù hợp.</td></tr>';
    } catch (error) { showError(root, error); }
  };
  const open = (item = null) => {
    form.hidden = false;
    form.elements.id.value = item?.id || '';
    form.elements.productCode.value = item?.productCode || '';
    form.elements.productName.value = item?.productName || '';
    form.elements.salePrice.value = item?.salePrice ?? '';
    form.elements.status.value = item?.status || 'ACTIVE';
    form.elements.description.value = item?.description || '';
    form.elements.categoryId.innerHTML = categories.filter((category) => category.status === 'ACTIVE' || category.id === item?.categoryId)
      .map((category) => `<option value="${category.id}">${esc(category.categoryName)}</option>`).join('');
    form.elements.categoryId.value = item?.categoryId || form.elements.categoryId.value;
    form.elements.productCode.focus();
  };
  try { categories = await listCategories(); } catch (error) { showError(root, error); }
  root.querySelector('#new-product').addEventListener('click', () => open());
  root.addEventListener('click', async (event) => {
    const edit = event.target.closest('[data-product-edit]');
    if (edit) open(records.find((item) => item.id === Number(edit.dataset.productEdit)));
    const status = event.target.closest('[data-product-status]');
    if (status) {
      try { await updateProductStatus(status.dataset.productStatus, status.dataset.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'); await load(); }
      catch (error) { showError(root, error); }
    }
    if (event.target.closest('[data-cancel]')) form.hidden = true;
  });
  filter.addEventListener('submit', (event) => { event.preventDefault(); load(); });
  form.addEventListener('submit', async (event) => {
    event.preventDefault(); clearError(root);
    try {
      await saveProduct(form.elements.id.value || null, {
        categoryId: Number(form.elements.categoryId.value), productCode: form.elements.productCode.value,
        productName: form.elements.productName.value, salePrice: Number(form.elements.salePrice.value),
        status: form.elements.status.value, description: form.elements.description.value,
      });
      form.hidden = true; await load();
    } catch (error) { showError(root, error); }
  });
  await load();
}

export async function renderSupplierPage(root, auth) {
  renderShell(root, auth, pageLayout('Nhà cung cấp', 'Quản lý và tra cứu nhà cung cấp đang lưu.', `
    <form class="data-editor" id="supplier-form" hidden><input type="hidden" name="id"><div class="editor-grid editor-grid--three">
      <label>Mã nhà cung cấp<input name="supplierCode" maxlength="50" required></label><label>Tên nhà cung cấp<input name="supplierName" maxlength="200" required></label><label>Trạng thái<select name="status"><option value="ACTIVE">Hoạt động</option><option value="INACTIVE">Ngừng hoạt động</option></select></label>
      <label>Điện thoại<input name="phone" maxlength="20"></label><label>Email<input name="email" type="email" maxlength="150"></label><label>Địa chỉ<input name="address" maxlength="255"></label>
    </div>${editorActions()}</form>
    <form class="data-filter" id="supplier-filter"><input name="query" placeholder="Tìm theo mã hoặc tên"><select name="status"><option value="">Tất cả trạng thái</option><option value="ACTIVE">Hoạt động</option><option value="INACTIVE">Ngừng hoạt động</option></select><button class="button button--ghost">Tìm kiếm</button></form>
    <div class="data-panel"><div class="data-table-wrap"><table class="data-table"><thead><tr><th>Mã</th><th>Nhà cung cấp</th><th>Liên hệ</th><th>Địa chỉ</th><th>Trạng thái</th><th></th></tr></thead><tbody id="supplier-rows"></tbody></table></div><p class="data-count" id="supplier-count"></p></div>
  `, '<button class="button button--primary" type="button" id="new-supplier">Thêm nhà cung cấp</button>'), { active: 'Nhà cung cấp' });
  const form = root.querySelector('#supplier-form'); const filter = root.querySelector('#supplier-filter'); let records = [];
  const load = async () => {
    clearError(root);
    try {
      const data = await listSuppliers({ query: filter.elements.query.value, status: filter.elements.status.value, size: 100 }); records = data.content;
      root.querySelector('#supplier-count').textContent = `${data.totalElements.toLocaleString('vi-VN')} nhà cung cấp`;
      root.querySelector('#supplier-rows').innerHTML = records.length ? records.map((item) => `<tr><td><strong>${esc(item.supplierCode)}</strong></td><td>${esc(item.supplierName)}</td><td>${esc(item.phone || '—')}<br><small>${esc(item.email || '')}</small></td><td>${esc(item.address || '—')}</td><td>${statusBadge(item.status)}</td><td><button class="table-action" data-supplier-edit="${item.id}">Sửa</button></td></tr>`).join('') : '<tr><td class="empty-cell" colspan="6">Không có nhà cung cấp phù hợp.</td></tr>';
    } catch (error) { showError(root, error); }
  };
  const open = (item = null) => { form.hidden = false; ['id', 'supplierCode', 'supplierName', 'phone', 'email', 'address'].forEach((name) => { form.elements[name].value = item?.[name] || ''; }); form.elements.status.value = item?.status || 'ACTIVE'; form.elements.supplierCode.focus(); };
  root.querySelector('#new-supplier').addEventListener('click', () => open());
  root.addEventListener('click', (event) => { const edit = event.target.closest('[data-supplier-edit]'); if (edit) open(records.find((item) => item.id === Number(edit.dataset.supplierEdit))); if (event.target.closest('[data-cancel]')) form.hidden = true; });
  filter.addEventListener('submit', (event) => { event.preventDefault(); load(); });
  form.addEventListener('submit', async (event) => { event.preventDefault(); clearError(root); try { await saveSupplier(form.elements.id.value || null, { supplierCode: form.elements.supplierCode.value, supplierName: form.elements.supplierName.value, phone: form.elements.phone.value, email: form.elements.email.value, address: form.elements.address.value, status: form.elements.status.value }); form.hidden = true; await load(); } catch (error) { showError(root, error); } });
  await load();
}

export async function renderInventoryPage(root, auth) {
  renderShell(root, auth, pageLayout('Tồn kho hiện tại', 'Dữ liệu đọc trực tiếp từ products.stock_quantity; trang này không điều chỉnh tồn.', `
    <form class="data-filter" id="inventory-filter"><input name="query" placeholder="Tìm theo mã hoặc tên sản phẩm"><select name="status"><option value="">Tất cả trạng thái</option><option value="ACTIVE">Hoạt động</option><option value="INACTIVE">Ngừng hoạt động</option></select><button class="button button--ghost">Tìm kiếm</button></form>
    <div class="data-panel"><div class="data-table-wrap"><table class="data-table"><thead><tr><th>Mã</th><th>Sản phẩm</th><th>Danh mục</th><th>Tồn hiện tại</th><th>Trạng thái</th></tr></thead><tbody id="inventory-rows"></tbody></table></div><p class="data-count" id="inventory-count"></p></div>
  `), { active: 'Tồn kho' });
  const filter = root.querySelector('#inventory-filter');
  const load = async () => { clearError(root); try { const data = await listInventory({ query: filter.elements.query.value, status: filter.elements.status.value, size: 100 }); root.querySelector('#inventory-count').textContent = `${data.totalElements.toLocaleString('vi-VN')} sản phẩm`; root.querySelector('#inventory-rows').innerHTML = data.content.length ? data.content.map((item) => `<tr><td><strong>${esc(item.productCode)}</strong></td><td>${esc(item.productName)}</td><td>${esc(item.categoryName)}</td><td><strong>${item.stockQuantity.toLocaleString('vi-VN')}</strong></td><td>${statusBadge(item.status)}</td></tr>`).join('') : '<tr><td class="empty-cell" colspan="5">Không có sản phẩm phù hợp.</td></tr>'; } catch (error) { showError(root, error); } };
  filter.addEventListener('submit', (event) => { event.preventDefault(); load(); }); await load();
}

export async function renderInvoicePage(root, auth) {
  renderShell(root, auth, pageLayout('Tra cứu hóa đơn', 'Tìm theo mã hoặc khoảng ngày và xem chi tiết từ đơn hàng.', `
    <form class="data-filter data-filter--dates" id="invoice-filter"><input name="code" placeholder="Mã hóa đơn"><label>Từ ngày<input name="fromDate" type="date"></label><label>Đến ngày<input name="toDate" type="date"></label><button class="button button--ghost">Tra cứu</button></form>
    <div class="data-panel"><div class="data-table-wrap"><table class="data-table"><thead><tr><th>Mã hóa đơn</th><th>Ngày lập</th><th>Mã đơn</th><th>Tổng tiền</th><th>Thanh toán</th><th>Trạng thái</th><th></th></tr></thead><tbody id="invoice-rows"></tbody></table></div><p class="data-count" id="invoice-count"></p></div>
    <section class="invoice-detail" id="invoice-detail" hidden></section>
  `), { active: 'Hóa đơn' });
  const filter = root.querySelector('#invoice-filter');
  const load = async () => { clearError(root); try { const data = await listInvoices({ code: filter.elements.code.value, fromDate: filter.elements.fromDate.value, toDate: filter.elements.toDate.value, size: 100 }); root.querySelector('#invoice-count').textContent = `${data.totalElements.toLocaleString('vi-VN')} hóa đơn`; root.querySelector('#invoice-rows').innerHTML = data.content.length ? data.content.map((item) => `<tr><td><strong>${esc(item.invoiceCode)}</strong></td><td>${dateTime(item.invoiceDate)}</td><td>${esc(item.orderCode)}</td><td>${money(item.totalAmount)}</td><td>${esc(item.paymentMethod || '—')}</td><td>${esc(item.status)}</td><td><button class="table-action" data-invoice-detail="${item.id}">Chi tiết</button></td></tr>`).join('') : '<tr><td class="empty-cell" colspan="7">Không có hóa đơn phù hợp.</td></tr>'; } catch (error) { showError(root, error); } };
  filter.addEventListener('submit', (event) => { event.preventDefault(); load(); });
  root.addEventListener('click', async (event) => { const button = event.target.closest('[data-invoice-detail]'); if (!button) return; clearError(root); try { const item = await getInvoiceDetail(button.dataset.invoiceDetail); const detail = root.querySelector('#invoice-detail'); detail.hidden = false; detail.innerHTML = `<div class="invoice-detail__heading"><div><p class="eyebrow">Chi tiết hóa đơn</p><h3>${esc(item.invoiceCode)}</h3></div><strong>${money(item.totalAmount)}</strong></div><dl class="invoice-meta"><div><dt>Đơn hàng</dt><dd>${esc(item.orderCode)}</dd></div><div><dt>Khách hàng</dt><dd>${esc(item.customerName)}</dd></div><div><dt>Nhân viên</dt><dd>${esc(item.createdBy)}</dd></div><div><dt>Ngày lập</dt><dd>${dateTime(item.invoiceDate)}</dd></div></dl><div class="data-table-wrap"><table class="data-table"><thead><tr><th>Mã</th><th>Sản phẩm</th><th>Số lượng</th><th>Đơn giá</th><th>Thành tiền</th></tr></thead><tbody>${item.lines.map((line) => `<tr><td>${esc(line.productCode)}</td><td>${esc(line.productName)}</td><td>${line.quantity}</td><td>${money(line.unitPrice)}</td><td>${money(line.lineTotal)}</td></tr>`).join('')}</tbody></table></div>`; detail.scrollIntoView({ behavior: 'smooth', block: 'start' }); } catch (error) { showError(root, error); } });
  await load();
}
