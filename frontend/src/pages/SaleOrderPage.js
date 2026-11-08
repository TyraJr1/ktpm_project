import { ApiError, listSalesProducts, createSalesOrder } from '../api/client.js';
import { renderSalesShell } from '../components/SalesShell.js';

let products = [];
let cart = [];

function cartTotal() {
  return cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
}

function renderProductGrid(container) {
  if (products.length === 0) {
    container.innerHTML = '<p style="text-align:center;padding:2rem;color:#888">Không có sản phẩm nào.</p>';
    return;
  }
  container.innerHTML = products.map((p) => `
    <div class="module-card" style="cursor:pointer" data-product-id="${p.id}">
      <p style="font-weight:600">${p.name}</p>
      <strong style="color:var(--primary,#2563eb)">${(p.price ?? 0).toLocaleString('vi-VN')} đ</strong>
      <small>Tồn kho: ${p.stockQuantity ?? '—'}</small>
    </div>`).join('');
}

function renderCart(container) {
  const itemsHtml = cart.length === 0
    ? '<p style="text-align:center;color:#888;padding:1rem">Giỏ hàng trống</p>'
    : cart.map((item, idx) => `
      <div style="display:flex;justify-content:space-between;align-items:center;padding:.5rem 0;border-bottom:1px solid #eee">
        <span>${item.name} (x${item.quantity})</span>
        <span style="display:flex;align-items:center;gap:.5rem">
          <span>${(item.price * item.quantity).toLocaleString('vi-VN')} đ</span>
          <button class="button button--ghost" data-remove-idx="${idx}" style="padding:2px 6px;font-size:.75rem" type="button">✕</button>
        </span>
      </div>`).join('');

  container.innerHTML = `
    <div style="margin-bottom:1rem">
      <label style="font-weight:600;display:block;margin-bottom:.25rem">SĐT khách hàng</label>
      <input type="text" id="customer-phone" class="form-input" placeholder="Số điện thoại" style="width:100%" />
    </div>
    <div id="cart-items" style="flex:1;overflow-y:auto;margin-bottom:1rem">${itemsHtml}</div>
    <div style="border-top:2px solid #333;padding-top:.75rem;margin-bottom:.75rem;display:flex;justify-content:space-between;font-weight:700;font-size:1.1rem">
      <span>Tổng cộng:</span>
      <span>${cartTotal().toLocaleString('vi-VN')} đ</span>
    </div>
    <button class="button" id="btn-checkout" type="button" style="width:100%">Thanh toán</button>
    <div class="form-alert" id="checkout-error" role="alert" hidden style="margin-top:.75rem"></div>
    <div class="form-alert" id="checkout-success" role="alert" hidden style="margin-top:.75rem;background:#d1fae5;color:#065f46"></div>
  `;
}

function addToCart(product) {
  const existing = cart.find((item) => item.productId === product.id);
  if (existing) {
    existing.quantity += 1;
  } else {
    cart.push({ productId: product.id, name: product.name, price: product.price, quantity: 1 });
  }
}

export async function renderSalesOrderPage(root, auth) {
  cart = [];
  products = [];

  renderSalesShell(root, auth, `
    <div style="display:flex;gap:1.5rem;align-items:flex-start">
      <div style="flex:2">
        <div class="panel-heading"><div><h3>Sản phẩm</h3></div></div>
        <div class="module-grid" id="product-grid" style="margin-top:.75rem">
          <p style="text-align:center;padding:2rem;color:#888">Đang tải sản phẩm…</p>
        </div>
      </div>
      <div style="flex:1;background:#fff;border-radius:8px;padding:1.25rem;box-shadow:0 1px 3px rgba(0,0,0,.1)">
        <h3 style="margin:0 0 1rem">Đơn hàng hiện tại</h3>
        <div id="cart-container"></div>
      </div>
    </div>
    <div class="form-alert" id="page-error" role="alert" hidden></div>
  `, { active: 'Bán hàng POS' });

  const productGrid = root.querySelector('#product-grid');
  const cartContainer = root.querySelector('#cart-container');

  // Load products
  try {
    const data = await listSalesProducts({ size: 100 });
    products = data.content || data || [];
    renderProductGrid(productGrid);
  } catch (error) {
    const alert = root.querySelector('#page-error');
    alert.textContent = error instanceof ApiError ? error.message : 'Không thể tải danh sách sản phẩm.';
    alert.hidden = false;
  }

  renderCart(cartContainer);

  // Event delegation
  root.addEventListener('click', async (event) => {
    // Add to cart
    const productCard = event.target.closest('[data-product-id]');
    if (productCard) {
      const id = Number(productCard.dataset.productId);
      const product = products.find((p) => p.id === id);
      if (product) {
        addToCart(product);
        renderCart(cartContainer);
      }
      return;
    }

    // Remove from cart
    const removeBtn = event.target.closest('[data-remove-idx]');
    if (removeBtn) {
      const idx = Number(removeBtn.dataset.removeIdx);
      cart.splice(idx, 1);
      renderCart(cartContainer);
      return;
    }

    // Checkout
    if (event.target.closest('#btn-checkout')) {
      if (cart.length === 0) {
        const errEl = root.querySelector('#checkout-error');
        if (errEl) { errEl.textContent = 'Giỏ hàng trống!'; errEl.hidden = false; }
        return;
      }

      const phoneInput = root.querySelector('#customer-phone');
      const orderPayload = {
        customerPhone: phoneInput ? phoneInput.value.trim() : '',
        items: cart.map((item) => ({
          productId: item.productId,
          quantity: item.quantity,
          price: item.price,
        })),
      };

      const btn = event.target.closest('#btn-checkout');
      btn.disabled = true;
      btn.textContent = 'Đang xử lý…';
      try {
        await createSalesOrder(orderPayload);
        cart = [];
        renderCart(cartContainer);
        const successEl = root.querySelector('#checkout-success');
        if (successEl) { successEl.textContent = 'Tạo đơn hàng thành công!'; successEl.hidden = false; }
      } catch (error) {
        const errEl = root.querySelector('#checkout-error');
        if (errEl) {
          errEl.textContent = error instanceof ApiError ? error.message : 'Lỗi khi tạo đơn hàng.';
          errEl.hidden = false;
        }
      }
    }
  });
}
