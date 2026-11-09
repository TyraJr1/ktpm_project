import {
  ApiError,
  createSalesOrder,
  listCustomers,
  listSalesProducts,
  saveCustomer,
} from '../api/client.js';
import { renderSalesShell } from '../components/SalesShell.js';
import { escapeHtml, formatCurrency, formatDateTime } from '../utils/format.js';

function paymentLabel(method) {
  const map = { CASH: 'Tiền mặt', TRANSFER: 'Chuyển khoản', CARD: 'Thẻ' };
  return map[method] || method || 'Tiền mặt';
}

export async function renderSalesOrderPage(root, auth) {
  renderSalesShell(root, auth, `
    <section class="admin-page">
      <header class="admin-page__heading">
        <div>
          <h2>Quầy bán hàng & Lập hóa đơn (POS)</h2>
          <p>Chọn sản phẩm đang kinh doanh, kiểm tra tồn kho thực tế (BR-01), tính tự động thành tiền/tổng tiền và xác nhận xuất hóa đơn kiêm trừ kho (Bảng 3.10 & 3.11).</p>
        </div>
        <button class="button button--ghost" type="button" id="clear-pos-order">Làm mới đơn hàng</button>
      </header>

      <div class="form-alert" id="pos-error" role="alert" hidden></div>
      <div class="form-alert" id="pos-success" role="status" hidden style="background:#f0fdf4;border-color:#86efac;color:#166534;"></div>

      <div style="display:grid;grid-template-columns:1.2fr 1fr;gap:20px;align-items:start;">
        <div>
          <form class="data-filter" id="pos-product-filter">
            <input name="query" placeholder="Tìm sản phẩm theo mã (SP...) hoặc tên văn phòng phẩm">
            <button class="button button--ghost" type="submit">Tìm kiếm</button>
            <button class="button button--ghost" type="button" id="pos-product-reset">Tất cả</button>
          </form>

          <div class="data-panel">
            <div class="data-table-wrap" style="max-height:540px;overflow:auto;">
              <table class="data-table">
                <thead>
                  <tr>
                    <th>Mã</th>
                    <th>Sản phẩm</th>
                    <th>Danh mục</th>
                    <th>Giá bán</th>
                    <th>Tồn kho</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody id="pos-product-rows">
                  <tr><td class="empty-cell" colspan="6">Đang tải danh sách sản phẩm...</td></tr>
                </tbody>
              </table>
            </div>
            <p class="data-count" id="pos-product-count"></p>
          </div>
        </div>

        <div class="data-panel" style="padding:20px;">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
            <h3 style="margin:0;">Thông tin khách hàng & Giỏ hàng</h3>
            <button class="table-action" type="button" id="toggle-pos-new-customer">+ Thêm khách hàng mới</button>
          </div>

          <div style="margin-bottom:14px;">
            <label style="display:block;font-weight:600;margin-bottom:6px;font-size:0.9rem;">
              Khách hàng giao dịch
              <select id="pos-customer-select" style="width:100%;margin-top:4px;padding:10px;border-radius:8px;border:1px solid #cbd5e1;">
                <option value="">-- Khách lẻ (Không lưu thông tin cá nhân) --</option>
              </select>
            </label>
          </div>

          <form class="data-editor" id="pos-quick-customer-form" hidden style="margin-bottom:16px;">
            <div class="editor-grid">
              <label>Tên khách hàng *<input name="customerName" maxlength="150" required placeholder="Nguyễn Văn A"></label>
              <label>Số điện thoại<input name="phone" maxlength="20" placeholder="0901234567"></label>
              <label>Email<input name="email" type="email" maxlength="150" placeholder="khach@email.com"></label>
              <label>Địa chỉ<input name="address" maxlength="255" placeholder="Quận/Huyện, TP..."></label>
            </div>
            <div class="editor-actions">
              <button class="button button--ghost" type="button" id="cancel-pos-new-customer">Hủy</button>
              <button class="button button--primary" type="submit">Lưu & Chọn khách</button>
            </div>
          </form>

          <div class="data-table-wrap" style="max-height:300px;overflow:auto;margin-bottom:16px;">
            <table class="data-table">
              <thead>
                <tr>
                  <th>Sản phẩm</th>
                  <th>Đơn giá</th>
                  <th>Số lượng</th>
                  <th>Thành tiền</th>
                  <th></th>
                </tr>
              </thead>
              <tbody id="pos-cart-rows">
                <tr><td class="empty-cell" colspan="5">Chưa có sản phẩm trong đơn hàng.</td></tr>
              </tbody>
            </table>
          </div>

          <div class="editor-grid" style="margin-bottom:16px;">
            <label>Phương thức thanh toán
              <select id="pos-payment-method">
                <option value="CASH">Tiền mặt (CASH)</option>
                <option value="TRANSFER">Chuyển khoản (TRANSFER)</option>
                <option value="CARD">Thẻ ngân hàng (CARD)</option>
              </select>
            </label>
            <label>Ghi chú đơn hàng
              <input id="pos-order-note" maxlength="1000" placeholder="Ghi chú giao dịch (tùy chọn)">
            </label>
          </div>

          <div style="padding:14px 16px;border-radius:10px;background:#f0fdf4;border:1px solid #86efac;display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
            <div>
              <strong style="display:block;color:#166534;">Tổng tiền hóa đơn (tự động tính)</strong>
              <small id="pos-cart-summary" style="color:#15803d;">0 mặt hàng / 0 sản phẩm</small>
            </div>
            <strong id="pos-cart-total" style="font-size:1.45rem;color:#14532d;">0 ₫</strong>
          </div>

          <button class="button button--primary" type="button" id="submit-pos-order" style="width:100%;justify-content:center;padding:12px;">
            Xác nhận giao dịch & Xuất hóa đơn
          </button>
        </div>
      </div>

      <section class="invoice-detail" id="pos-created-invoice" hidden></section>
    </section>
  `, { active: 'Bán hàng POS' });

  const errorEl = root.querySelector('#pos-error');
  const successEl = root.querySelector('#pos-success');
  const productRowsEl = root.querySelector('#pos-product-rows');
  const productCountEl = root.querySelector('#pos-product-count');
  const cartRowsEl = root.querySelector('#pos-cart-rows');
  const cartSummaryEl = root.querySelector('#pos-cart-summary');
  const cartTotalEl = root.querySelector('#pos-cart-total');
  const customerSelectEl = root.querySelector('#pos-customer-select');
  const quickCustomerForm = root.querySelector('#pos-quick-customer-form');
  const createdInvoiceEl = root.querySelector('#pos-created-invoice');
  const submitBtn = root.querySelector('#submit-pos-order');

  let products = [];
  let customers = [];
  let cart = [];
  let selectedCustomerId = '';

  const showError = (err) => {
    successEl.hidden = true;
    errorEl.textContent = err instanceof ApiError ? err.message : String(err?.message || err || 'Không thể xử lý yêu cầu.');
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

  const renderCustomerOptions = () => {
    customerSelectEl.innerHTML = `
      <option value="">-- Khách lẻ (Không lưu thông tin cá nhân) --</option>
      ${customers.map((c) => `
        <option value="${c.id}" ${String(c.id) === String(selectedCustomerId) ? 'selected' : ''}>
          ${escapeHtml(c.customerName)}${c.phone ? ` — ${escapeHtml(c.phone)}` : ''}
        </option>
      `).join('')}
    `;
  };

  const renderProducts = () => {
    if (!products.length) {
      productRowsEl.innerHTML = '<tr><td class="empty-cell" colspan="6">Không có sản phẩm phù hợp.</td></tr>';
      return;
    }
    productRowsEl.innerHTML = products.map((item) => {
      const inCart = cart.find((c) => c.productId === item.id);
      const remaining = (item.stockQuantity ?? 0) - (inCart ? inCart.quantity : 0);
      const outOfStock = (item.stockQuantity ?? 0) <= 0;
      return `
        <tr>
          <td><strong>${escapeHtml(item.productCode)}</strong></td>
          <td>${escapeHtml(item.productName)}</td>
          <td>${escapeHtml(item.categoryName || '—')}</td>
          <td><strong>${formatCurrency(item.salePrice)}</strong></td>
          <td>
            <span class="data-status ${outOfStock ? 'is-inactive' : 'is-active'}">
              ${outOfStock ? 'Hết hàng (0)' : `Tồn: ${item.stockQuantity}`}
            </span>
          </td>
          <td>
            <button
              class="table-action"
              type="button"
              data-pos-add="${item.id}"
              ${outOfStock || remaining <= 0 ? 'disabled' : ''}
            >
              ${outOfStock ? 'Hết hàng' : remaining <= 0 ? 'Đã chọn tối đa' : '+ Chọn'}
            </button>
          </td>
        </tr>
      `;
    }).join('');
  };

  const renderCart = () => {
    if (!cart.length) {
      cartRowsEl.innerHTML = '<tr><td class="empty-cell" colspan="5">Chưa có sản phẩm trong đơn hàng.</td></tr>';
      cartSummaryEl.textContent = '0 mặt hàng / 0 sản phẩm';
      cartTotalEl.textContent = formatCurrency(0);
      return;
    }

    let totalAmount = 0;
    let totalUnits = 0;

    cartRowsEl.innerHTML = cart.map((item) => {
      const lineTotal = item.unitPrice * item.quantity;
      totalAmount += lineTotal;
      totalUnits += item.quantity;
      return `
        <tr>
          <td>
            <strong>${escapeHtml(item.productName)}</strong><br>
            <small>${escapeHtml(item.productCode)} (Tồn: ${item.stockQuantity})</small>
          </td>
          <td>${formatCurrency(item.unitPrice)}</td>
          <td>
            <div style="display:flex;align-items:center;gap:4px;">
              <button class="table-action" type="button" data-pos-dec="${item.productId}">-</button>
              <input
                type="number"
                min="1"
                max="${item.stockQuantity}"
                value="${item.quantity}"
                data-pos-qty="${item.productId}"
                style="width:56px;text-align:center;padding:4px;"
              >
              <button class="table-action" type="button" data-pos-inc="${item.productId}">+</button>
            </div>
          </td>
          <td><strong>${formatCurrency(lineTotal)}</strong></td>
          <td><button class="table-action" type="button" data-pos-remove="${item.productId}">Xóa</button></td>
        </tr>
      `;
    }).join('');

    cartSummaryEl.textContent = `${cart.length} mặt hàng / ${totalUnits} sản phẩm`;
    cartTotalEl.textContent = formatCurrency(totalAmount);
  };

  const loadProducts = async (query = '') => {
    try {
      const data = await listSalesProducts({ query, status: 'ACTIVE', size: 100 });
      products = data.content || [];
      productCountEl.textContent = `${(data.totalElements ?? products.length).toLocaleString('vi-VN')} sản phẩm đang kinh doanh`;
      renderProducts();
    } catch (error) {
      showError(error);
    }
  };

  const loadCustomersList = async () => {
    try {
      const data = await listCustomers({ size: 100 });
      customers = data.content || [];
      renderCustomerOptions();
    } catch (error) {
      showError(error);
    }
  };

  const addToCart = (productId) => {
    clearAlerts();
    const product = products.find((p) => p.id === productId);
    if (!product) return;

    const maxStock = product.stockQuantity ?? 0;
    if (maxStock <= 0) {
      showError(`Sản phẩm "${product.productName}" đã hết hàng trong kho.`);
      return;
    }

    const existing = cart.find((c) => c.productId === productId);
    if (existing) {
      if (existing.quantity + 1 > maxStock) {
        showError(`Số lượng mua không được vượt quá tồn kho hiện tại (${maxStock}) của "${product.productName}" (BR-01).`);
        return;
      }
      existing.quantity += 1;
    } else {
      cart.push({
        productId: product.id,
        productCode: product.productCode,
        productName: product.productName,
        unitPrice: Number(product.salePrice),
        stockQuantity: maxStock,
        quantity: 1,
      });
    }
    renderCart();
    renderProducts();
  };

  const updateCartQty = (productId, nextQty) => {
    clearAlerts();
    const item = cart.find((c) => c.productId === productId);
    if (!item) return;

    if (nextQty <= 0) {
      cart = cart.filter((c) => c.productId !== productId);
    } else if (nextQty > item.stockQuantity) {
      showError(`Sản phẩm "${item.productName}" chỉ còn tồn kho ${item.stockQuantity} sản phẩm (BR-01).`);
      item.quantity = item.stockQuantity;
    } else {
      item.quantity = nextQty;
    }
    renderCart();
    renderProducts();
  };

  root.querySelector('#pos-product-filter').addEventListener('submit', (event) => {
    event.preventDefault();
    loadProducts(event.currentTarget.elements.query.value);
  });

  root.querySelector('#pos-product-reset').addEventListener('click', () => {
    const filterForm = root.querySelector('#pos-product-filter');
    filterForm.elements.query.value = '';
    loadProducts('');
  });

  customerSelectEl.addEventListener('change', () => {
    selectedCustomerId = customerSelectEl.value;
  });

  root.querySelector('#toggle-pos-new-customer').addEventListener('click', () => {
    quickCustomerForm.hidden = !quickCustomerForm.hidden;
  });

  root.querySelector('#cancel-pos-new-customer').addEventListener('click', () => {
    quickCustomerForm.reset();
    quickCustomerForm.hidden = true;
  });

  quickCustomerForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    clearAlerts();
    try {
      const created = await saveCustomer(null, {
        customerName: quickCustomerForm.elements.customerName.value,
        phone: quickCustomerForm.elements.phone.value,
        email: quickCustomerForm.elements.email.value,
        address: quickCustomerForm.elements.address.value,
      });
      customers.unshift(created);
      selectedCustomerId = String(created.id);
      renderCustomerOptions();
      quickCustomerForm.reset();
      quickCustomerForm.hidden = true;
      showSuccess(`Đã thêm và chọn khách hàng "${created.customerName}".`);
    } catch (error) {
      showError(error);
    }
  });

  root.querySelector('#clear-pos-order').addEventListener('click', () => {
    cart = [];
    selectedCustomerId = '';
    customerSelectEl.value = '';
    root.querySelector('#pos-order-note').value = '';
    createdInvoiceEl.hidden = true;
    clearAlerts();
    renderCart();
    renderProducts();
  });

  root.addEventListener('click', (event) => {
    const addBtn = event.target.closest('[data-pos-add]');
    if (addBtn) {
      addToCart(Number(addBtn.dataset.posAdd));
      return;
    }
    const decBtn = event.target.closest('[data-pos-dec]');
    if (decBtn) {
      const id = Number(decBtn.dataset.posDec);
      const current = cart.find((c) => c.productId === id);
      if (current) updateCartQty(id, current.quantity - 1);
      return;
    }
    const incBtn = event.target.closest('[data-pos-inc]');
    if (incBtn) {
      const id = Number(incBtn.dataset.posInc);
      const current = cart.find((c) => c.productId === id);
      if (current) updateCartQty(id, current.quantity + 1);
      return;
    }
    const removeBtn = event.target.closest('[data-pos-remove]');
    if (removeBtn) {
      updateCartQty(Number(removeBtn.dataset.posRemove), 0);
    }
  });

  root.addEventListener('change', (event) => {
    const qtyInput = event.target.closest('[data-pos-qty]');
    if (!qtyInput) return;
    const id = Number(qtyInput.dataset.posQty);
    const val = parseInt(qtyInput.value, 10);
    updateCartQty(id, Number.isNaN(val) || val < 1 ? 1 : val);
  });

  submitBtn.addEventListener('click', async () => {
    clearAlerts();
    if (!cart.length) {
      showError('Đơn hàng phải có ít nhất một sản phẩm (Bảng 3.10).');
      return;
    }

    submitBtn.disabled = true;
    submitBtn.textContent = 'Đang xử lý giao dịch & lập hóa đơn...';
    try {
      const result = await createSalesOrder({
        customerId: customerSelectEl.value ? Number(customerSelectEl.value) : null,
        paymentMethod: root.querySelector('#pos-payment-method').value,
        note: root.querySelector('#pos-order-note').value.trim() || null,
        items: cart.map((item) => ({
          productId: item.productId,
          quantity: item.quantity,
        })),
      });

      cart = [];
      root.querySelector('#pos-order-note').value = '';
      renderCart();
      await loadProducts(root.querySelector('#pos-product-filter').elements.query.value);

      showSuccess(`Giao dịch hoàn tất! Đã tạo đơn hàng ${result.orderCode}, xuất hóa đơn ${result.invoiceCode} và trừ tồn kho.`);

      createdInvoiceEl.hidden = false;
      createdInvoiceEl.innerHTML = `
        <div class="invoice-detail__heading">
          <div>
            <p class="eyebrow">Hóa đơn vừa xuất thành công (Bảng 3.11)</p>
            <h3>${escapeHtml(result.invoiceCode)} — Đơn ${escapeHtml(result.orderCode)}</h3>
          </div>
          <strong>${formatCurrency(result.totalAmount)}</strong>
        </div>
        <dl class="invoice-meta">
          <div><dt>Khách hàng</dt><dd>${escapeHtml(result.customerName || 'Khách lẻ')}${result.customerPhone ? ` (${escapeHtml(result.customerPhone)})` : ''}</dd></div>
          <div><dt>Nhân viên lập</dt><dd>${escapeHtml(result.createdBy || '—')}</dd></div>
          <div><dt>Phương thức TT</dt><dd>${escapeHtml(paymentLabel(result.paymentMethod))}</dd></div>
          <div><dt>Thời gian lập</dt><dd>${formatDateTime(result.invoiceDate)}</dd></div>
          <div><dt>Trạng thái</dt><dd>${escapeHtml(result.status)}</dd></div>
        </dl>
        ${result.note ? `<p style="margin:0 0 12px;"><strong>Ghi chú:</strong> ${escapeHtml(result.note)}</p>` : ''}
        <div class="data-table-wrap">
          <table class="data-table">
            <thead>
              <tr><th>Mã SP</th><th>Tên sản phẩm</th><th>Số lượng</th><th>Đơn giá</th><th>Thành tiền</th></tr>
            </thead>
            <tbody>
              ${(result.items || []).map((line) => `
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
      createdInvoiceEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
    } catch (error) {
      showError(error);
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = 'Xác nhận giao dịch & Xuất hóa đơn';
    }
  });

  await Promise.all([loadProducts(''), loadCustomersList()]);
}
