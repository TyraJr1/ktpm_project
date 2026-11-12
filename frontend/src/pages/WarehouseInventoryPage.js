import {
  ApiError,
  createStockAdjustment,
  createStockOutbound,
  listWarehouseProducts,
} from '../api/client.js';
import { renderWarehouseShell } from '../components/WarehouseShell.js';
import { escapeHtml, formatCurrency } from '../utils/format.js';

export async function renderWarehouseInventoryPage(root, auth) {
  renderWarehouseShell(root, auth, `
    <section class="admin-page">
      <header class="admin-page__heading">
        <div>
          <h2>Quản lý Tồn kho, Xuất kho & Kiểm kê Điều chỉnh</h2>
          <p>Tra cứu danh sách sản phẩm và số lượng tồn kho hiện tại (UC-18, UC-19, UC-20), thực hiện nghiệp vụ xuất kho (UC-23) và kiểm kê điều chỉnh chênh lệch tồn kho thực tế (UC-24).</p>
        </div>
        <div style="display:flex;gap:8px;flex-wrap:wrap;">
          <button class="button button--ghost" type="button" id="open-outbound-btn">- Xuất kho (Outbound)</button>
          <button class="button button--primary" type="button" id="open-adjust-btn">± Kiểm kê & Điều chỉnh kho</button>
        </div>
      </header>

      <div class="form-alert" id="wh-inv-error" role="alert" hidden></div>
      <div class="form-alert" id="wh-inv-success" role="status" hidden style="background:#f0fdf4;border-color:#86efac;color:#166534;"></div>

      <form class="data-editor" id="wh-outbound-form" hidden>
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:14px;">
          <div>
            <p class="eyebrow" style="margin-bottom:4px;">Nghiệp vụ Xuất kho (UC-23 / FR-INV-04)</p>
            <h3 style="margin:0;">Lập phiếu xuất kho nội bộ / xuất hủy / xuất sử dụng</h3>
          </div>
          <span class="data-status is-inactive">OUTBOUND (- Tồn kho)</span>
        </div>
        <div class="editor-grid editor-grid--three">
          <label>Sản phẩm cần xuất kho *
            <select name="productId" id="outbound-product-select" required></select>
          </label>
          <label>Số lượng xuất *
            <input name="quantity" id="outbound-qty-input" type="number" min="1" step="1" value="1" required>
            <small id="outbound-stock-preview">Tồn hiện tại: —</small>
          </label>
          <label>Lý do xuất kho *
            <input name="reason" maxlength="500" required placeholder="VD: Xuất hàng lỗi/hư hỏng, xuất dùng văn phòng...">
          </label>
        </div>
        <div class="editor-actions">
          <button class="button button--ghost" type="button" id="cancel-outbound-btn">Hủy</button>
          <button class="button button--primary" type="submit" id="submit-outbound-btn">Xác nhận Xuất kho & Trừ tồn</button>
        </div>
      </form>

      <form class="data-editor" id="wh-adjust-form" hidden>
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:14px;">
          <div>
            <p class="eyebrow" style="margin-bottom:4px;">Nghiệp vụ Kiểm kê & Điều chỉnh tồn kho (UC-24 / Bảng 2.3 mục 8-9)</p>
            <h3 style="margin:0;">Đối chiếu số lượng kiểm đếm thực tế và cân bằng tồn kho</h3>
          </div>
          <span class="data-status is-active">ADJUSTMENT (± Kiểm kê)</span>
        </div>
        <div class="editor-grid editor-grid--three">
          <label>Sản phẩm kiểm kê *
            <select name="productId" id="adjust-product-select" required></select>
          </label>
          <label>Số lượng kiểm đếm thực tế (&ge; 0) *
            <input name="actualQuantity" id="adjust-actual-input" type="number" min="0" step="1" value="0" required>
            <small id="adjust-diff-preview">Tồn sổ sách: — | Chênh lệch: —</small>
          </label>
          <label>Lý do điều chỉnh chênh lệch *
            <input name="reason" maxlength="500" required placeholder="VD: Kiểm kê định kỳ tháng, bù lệch kiểm đếm...">
          </label>
        </div>
        <div class="editor-actions">
          <button class="button button--ghost" type="button" id="cancel-adjust-btn">Hủy</button>
          <button class="button button--primary" type="submit" id="submit-adjust-btn">Cập nhật Tồn kho thực tế</button>
        </div>
      </form>

      <form class="data-filter" id="wh-product-filter" style="grid-template-columns:minmax(220px,1fr) 180px 200px auto auto;">
        <input name="query" placeholder="Tìm theo mã sản phẩm (SP...) hoặc tên sản phẩm (UC-19)">
        <select name="status">
          <option value="">Tất cả trạng thái</option>
          <option value="ACTIVE">Đang hoạt động (ACTIVE)</option>
          <option value="INACTIVE">Ngừng kinh doanh (INACTIVE)</option>
        </select>
        <select name="stockFilter" id="wh-stock-level-filter">
          <option value="ALL">Mọi mức tồn kho</option>
          <option value="LOW">Sắp hết hàng (&le; 10)</option>
          <option value="OUT">Đã hết hàng (= 0)</option>
        </select>
        <button class="button button--ghost" type="submit">Tra cứu</button>
        <button class="button button--ghost" type="button" id="wh-product-reset">Đặt lại</button>
      </form>

      <div class="data-panel">
        <div class="data-table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>Mã SP</th>
                <th>Tên sản phẩm</th>
                <th>Danh mục</th>
                <th>Giá bán niêm yết</th>
                <th>Số lượng tồn kho</th>
                <th>Mức cảnh báo</th>
                <th>Trạng thái</th>
                <th>Nghiệp vụ kho</th>
              </tr>
            </thead>
            <tbody id="wh-product-rows">
              <tr><td class="empty-cell" colspan="8">Đang tải danh sách sản phẩm và tồn kho...</td></tr>
            </tbody>
          </table>
        </div>
        <p class="data-count" id="wh-product-count"></p>
      </div>
    </section>
  `, { active: 'Tồn kho & Điều chỉnh' });

  const errorEl = root.querySelector('#wh-inv-error');
  const successEl = root.querySelector('#wh-inv-success');
  const outboundForm = root.querySelector('#wh-outbound-form');
  const adjustForm = root.querySelector('#wh-adjust-form');
  const outboundSelect = root.querySelector('#outbound-product-select');
  const outboundQtyInput = root.querySelector('#outbound-qty-input');
  const outboundPreview = root.querySelector('#outbound-stock-preview');
  const adjustSelect = root.querySelector('#adjust-product-select');
  const adjustActualInput = root.querySelector('#adjust-actual-input');
  const adjustDiffPreview = root.querySelector('#adjust-diff-preview');
  const filterForm = root.querySelector('#wh-product-filter');
  const stockLevelFilter = root.querySelector('#wh-stock-level-filter');
  const rowsEl = root.querySelector('#wh-product-rows');
  const countEl = root.querySelector('#wh-product-count');

  let allProducts = [];

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

  const getProductById = (id) => allProducts.find((item) => item.id === Number(id));

  const populateProductSelectors = (selectedId = null) => {
    const activeItems = allProducts;
    const optionsHtml = activeItems.length
      ? activeItems.map((p) => `
          <option value="${p.id}" ${Number(selectedId) === p.id ? 'selected' : ''}>
            ${escapeHtml(p.productCode)} — ${escapeHtml(p.productName)} (Tồn: ${p.stockQuantity ?? 0})
          </option>
        `).join('')
      : '<option value="">-- Không có sản phẩm --</option>';
    outboundSelect.innerHTML = optionsHtml;
    adjustSelect.innerHTML = optionsHtml;
    updateOutboundPreview();
    updateAdjustPreview();
  };

  const updateOutboundPreview = () => {
    const prod = getProductById(outboundSelect.value);
    if (!prod) {
      outboundPreview.textContent = 'Tồn hiện tại: —';
      return;
    }
    const current = prod.stockQuantity ?? 0;
    const qty = parseInt(outboundQtyInput.value, 10) || 0;
    const after = current - qty;
    outboundQtyInput.max = String(Math.max(current, 1));
    if (qty <= 0) {
      outboundPreview.textContent = `Tồn hiện tại: ${current} | Số lượng xuất phải > 0`;
      outboundPreview.style.color = '#b42318';
    } else if (after < 0) {
      outboundPreview.textContent = `Tồn hiện tại: ${current} | Vượt quá tồn kho (${after}) — Không hợp lệ (FR-INV-06)`;
      outboundPreview.style.color = '#b42318';
    } else {
      outboundPreview.textContent = `Tồn hiện tại: ${current} → Tồn sau xuất: ${after}`;
      outboundPreview.style.color = '#087b72';
    }
  };

  const updateAdjustPreview = () => {
    const prod = getProductById(adjustSelect.value);
    if (!prod) {
      adjustDiffPreview.textContent = 'Tồn sổ sách: — | Chênh lệch: —';
      return;
    }
    const current = prod.stockQuantity ?? 0;
    const actual = parseInt(adjustActualInput.value, 10);
    if (Number.isNaN(actual) || actual < 0) {
      adjustDiffPreview.textContent = `Tồn sổ sách: ${current} | Số lượng thực tế phải >= 0`;
      adjustDiffPreview.style.color = '#b42318';
      return;
    }
    const diff = actual - current;
    if (diff === 0) {
      adjustDiffPreview.textContent = `Tồn sổ sách: ${current} | Chênh lệch: 0 (Khớp số liệu, không cần điều chỉnh)`;
      adjustDiffPreview.style.color = '#94500f';
    } else {
      const sign = diff > 0 ? `+${diff}` : String(diff);
      adjustDiffPreview.textContent = `Tồn sổ sách: ${current} | Thực tế: ${actual} | Chênh lệch: ${sign} đơn vị`;
      adjustDiffPreview.style.color = '#087b72';
    }
  };

  const renderTable = () => {
    const level = stockLevelFilter.value;
    const filtered = allProducts.filter((item) => {
      const stock = item.stockQuantity ?? 0;
      if (level === 'OUT') return stock === 0;
      if (level === 'LOW') return stock > 0 && stock <= 10;
      return true;
    });

    countEl.textContent = `Hiển thị ${filtered.length.toLocaleString('vi-VN')} / ${allProducts.length.toLocaleString('vi-VN')} sản phẩm`;

    if (!filtered.length) {
      rowsEl.innerHTML = '<tr><td class="empty-cell" colspan="8">Không tìm thấy sản phẩm phù hợp với bộ lọc.</td></tr>';
      return;
    }

    rowsEl.innerHTML = filtered.map((item) => {
      const stock = item.stockQuantity ?? 0;
      let stockBadge = '<span class="data-status is-active">Đủ hàng</span>';
      if (stock === 0) {
        stockBadge = '<span class="data-status is-inactive" style="background:#fff0ef;color:#b42318;border-color:#f3c9c5;">Hết hàng (0)</span>';
      } else if (stock <= 10) {
        stockBadge = '<span class="data-status is-inactive">Sắp hết (&le; 10)</span>';
      }

      return `
        <tr>
          <td><strong>${escapeHtml(item.productCode)}</strong></td>
          <td>
            <strong>${escapeHtml(item.productName)}</strong>
            ${item.description ? `<br><small>${escapeHtml(item.description)}</small>` : ''}
          </td>
          <td>${escapeHtml(item.categoryName || '—')}</td>
          <td>${formatCurrency(item.salePrice)}</td>
          <td><strong style="font-size:0.95rem;">${stock.toLocaleString('vi-VN')}</strong></td>
          <td>${stockBadge}</td>
          <td>
            <span class="data-status ${item.status === 'ACTIVE' ? 'is-active' : 'is-inactive'}">
              ${escapeHtml(item.status)}
            </span>
          </td>
          <td class="table-actions">
            <button
              class="table-action"
              type="button"
              data-row-outbound="${item.id}"
              ${stock <= 0 ? 'disabled title="Sản phẩm đã hết hàng, không thể xuất kho"' : ''}
            >
              - Xuất kho
            </button>
            <button class="table-action" type="button" data-row-adjust="${item.id}">
              ± Kiểm kê
            </button>
          </td>
        </tr>
      `;
    }).join('');
  };

  const loadProducts = async () => {
    try {
      const query = filterForm.elements.query.value.trim();
      const status = filterForm.elements.status.value;
      const data = await listWarehouseProducts({ query, status, size: 200 });
      allProducts = data.content || [];
      populateProductSelectors(outboundSelect.value || adjustSelect.value);
      renderTable();
    } catch (error) {
      showError(error);
    }
  };

  const openOutboundPanel = (productId = null) => {
    clearAlerts();
    adjustForm.hidden = true;
    outboundForm.hidden = false;
    populateProductSelectors(productId);
    if (productId) {
      outboundSelect.value = String(productId);
    }
    outboundQtyInput.value = '1';
    outboundForm.elements.reason.value = '';
    updateOutboundPreview();
    outboundForm.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  const openAdjustPanel = (productId = null) => {
    clearAlerts();
    outboundForm.hidden = true;
    adjustForm.hidden = false;
    populateProductSelectors(productId);
    if (productId) {
      adjustSelect.value = String(productId);
    }
    const prod = getProductById(adjustSelect.value);
    adjustActualInput.value = String(prod ? (prod.stockQuantity ?? 0) : 0);
    adjustForm.elements.reason.value = '';
    updateAdjustPreview();
    adjustForm.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  root.querySelector('#open-outbound-btn').addEventListener('click', () => openOutboundPanel());
  root.querySelector('#open-adjust-btn').addEventListener('click', () => openAdjustPanel());

  root.querySelector('#cancel-outbound-btn').addEventListener('click', () => {
    outboundForm.hidden = true;
  });
  root.querySelector('#cancel-adjust-btn').addEventListener('click', () => {
    adjustForm.hidden = true;
  });

  outboundSelect.addEventListener('change', updateOutboundPreview);
  outboundQtyInput.addEventListener('input', updateOutboundPreview);

  adjustSelect.addEventListener('change', () => {
    const prod = getProductById(adjustSelect.value);
    if (prod) {
      adjustActualInput.value = String(prod.stockQuantity ?? 0);
    }
    updateAdjustPreview();
  });
  adjustActualInput.addEventListener('input', updateAdjustPreview);

  filterForm.addEventListener('submit', (event) => {
    event.preventDefault();
    clearAlerts();
    loadProducts();
  });

  stockLevelFilter.addEventListener('change', renderTable);

  root.querySelector('#wh-product-reset').addEventListener('click', () => {
    filterForm.elements.query.value = '';
    filterForm.elements.status.value = '';
    stockLevelFilter.value = 'ALL';
    clearAlerts();
    loadProducts();
  });

  root.addEventListener('click', (event) => {
    const outBtn = event.target.closest('[data-row-outbound]');
    if (outBtn) {
      openOutboundPanel(Number(outBtn.dataset.rowOutbound));
      return;
    }
    const adjBtn = event.target.closest('[data-row-adjust]');
    if (adjBtn) {
      openAdjustPanel(Number(adjBtn.dataset.rowAdjust));
    }
  });

  outboundForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    clearAlerts();
    const submitBtn = root.querySelector('#submit-outbound-btn');
    submitBtn.disabled = true;
    try {
      const tx = await createStockOutbound({
        productId: Number(outboundSelect.value),
        quantity: Number(outboundQtyInput.value),
        reason: outboundForm.elements.reason.value.trim(),
      });
      outboundForm.hidden = true;
      showSuccess(`Đã xuất kho ${tx.quantity} sản phẩm "${tx.productName}" (${tx.productCode}) và ghi nhật ký OUTBOUND thành công.`);
      await loadProducts();
    } catch (error) {
      showError(error);
    } finally {
      submitBtn.disabled = false;
    }
  });

  adjustForm.addEventListener('submit', async (event) => {
    event.preventDefault();
    clearAlerts();
    const submitBtn = root.querySelector('#submit-adjust-btn');
    submitBtn.disabled = true;
    try {
      const tx = await createStockAdjustment({
        productId: Number(adjustSelect.value),
        actualQuantity: Number(adjustActualInput.value),
        reason: adjustForm.elements.reason.value.trim(),
      });
      adjustForm.hidden = true;
      showSuccess(`Đã kiểm kê & điều chỉnh tồn kho cho "${tx.productName}" (${tx.productCode}): ${tx.note}`);
      await loadProducts();
    } catch (error) {
      showError(error);
    } finally {
      submitBtn.disabled = false;
    }
  });

  await loadProducts();
}
