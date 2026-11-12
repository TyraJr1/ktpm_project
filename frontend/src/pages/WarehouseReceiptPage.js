import {
  ApiError,
  confirmPurchaseReceipt,
  createPurchaseReceipt,
  getPurchaseReceiptDetail,
  listPurchaseReceipts,
  listWarehouseProducts,
  listWarehouseSuppliers,
} from '../api/client.js';
import { renderWarehouseShell } from '../components/WarehouseShell.js';
import { escapeHtml, formatCurrency, formatDateTime } from '../utils/format.js';

function statusBadge(status) {
  if (status === 'CONFIRMED') {
    return '<span class="data-status is-active">CONFIRMED (Đã nhập kho)</span>';
  }
  return '<span class="data-status is-inactive">PENDING (Chờ xác nhận)</span>';
}

export async function renderWarehouseReceiptPage(root, auth) {
  renderWarehouseShell(root, auth, `
    <section class="admin-page">
      <header class="admin-page__heading">
        <div>
          <h2>Quản lý Phiếu nhập kho & Xác nhận Nhập hàng</h2>
          <p>Lập phiếu nhập hàng từ nhà cung cấp đang hoạt động (UC-21 / Bảng 3.4) và xác nhận nhập kho để cộng tồn kho chính thức kiêm ghi nhật ký INBOUND (UC-22 / Bảng 3.7).</p>
        </div>
        <button class="button button--primary" type="button" id="toggle-create-receipt-btn">+ Lập phiếu nhập kho mới</button>
      </header>

      <div class="form-alert" id="wh-receipt-error" role="alert" hidden></div>
      <div class="form-alert" id="wh-receipt-success" role="status" hidden style="background:#f0fdf4;border-color:#86efac;color:#166534;"></div>

      <section class="data-editor" id="wh-receipt-builder" hidden>
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
          <div>
            <p class="eyebrow" style="margin-bottom:4px;">Lập phiếu nhập kho mới (UC-21 / FR-INV-02)</p>
            <h3 style="margin:0;">Chọn nhà cung cấp, thêm mặt hàng nhập và đơn giá nhập</h3>
          </div>
          <button class="button button--ghost" type="button" id="close-receipt-builder-btn">Đóng biểu mẫu</button>
        </div>

        <div class="editor-grid" style="margin-bottom:18px;">
          <label>Nhà cung cấp (Đang hoạt động - ACTIVE) *
            <select id="receipt-supplier-select" required>
              <option value="">-- Chọn nhà cung cấp --</option>
            </select>
          </label>
          <label>Ghi chú phiếu nhập
            <input id="receipt-note-input" maxlength="1000" placeholder="VD: Nhập lô hàng văn phòng phẩm đợt 1 tháng này...">
          </label>
        </div>

        <div style="border:1px solid #e2e8f0;border-radius:12px;padding:16px;background:#f8fafc;margin-bottom:18px;">
          <strong style="display:block;margin-bottom:10px;font-size:0.82rem;color:#1e293b;">
            Thêm sản phẩm vào phiếu nhập (Mỗi sản phẩm xuất hiện tối đa 1 dòng trên phiếu - DB-DECISION-07)
          </strong>
          <div style="display:grid;grid-template-columns:1.5fr 140px 180px auto;gap:12px;align-items:end;">
            <label style="font-size:0.72rem;font-weight:600;color:#40506a;">Sản phẩm (ACTIVE)
              <select id="receipt-product-picker" style="width:100%;margin-top:6px;padding:10px;border-radius:9px;border:1px solid #cdd7df;background:#fff;"></select>
            </label>
            <label style="font-size:0.72rem;font-weight:600;color:#40506a;">Số lượng nhập (&gt; 0)
              <input id="receipt-picker-qty" type="number" min="1" step="1" value="10" style="width:100%;margin-top:6px;padding:10px;border-radius:9px;border:1px solid #cdd7df;background:#fff;">
            </label>
            <label style="font-size:0.72rem;font-weight:600;color:#40506a;">Đơn giá nhập (₫, &ge; 0)
              <input id="receipt-picker-price" type="number" min="0" step="100" value="0" style="width:100%;margin-top:6px;padding:10px;border-radius:9px;border:1px solid #cdd7df;background:#fff;">
            </label>
            <button class="button button--ghost" type="button" id="add-receipt-line-btn">+ Thêm vào phiếu</button>
          </div>
        </div>

        <div class="data-table-wrap" style="margin-bottom:16px;">
          <table class="data-table">
            <thead>
              <tr>
                <th>Mã SP</th>
                <th>Tên sản phẩm</th>
                <th>Tồn hiện tại</th>
                <th>Số lượng nhập</th>
                <th>Đơn giá nhập (₫)</th>
                <th>Thành tiền</th>
                <th></th>
              </tr>
            </thead>
            <tbody id="receipt-draft-rows">
              <tr><td class="empty-cell" colspan="7">Chưa có mặt hàng nào trong phiếu nhập.</td></tr>
            </tbody>
          </table>
        </div>

        <div style="display:flex;justify-content:space-between;align-items:center;flex-wrap:wrap;gap:14px;padding:14px 18px;border-radius:12px;background:#f0fdf4;border:1px solid #86efac;">
          <div>
            <strong style="display:block;color:#166534;">Tổng giá trị phiếu nhập (tự động tính)</strong>
            <small id="receipt-draft-summary" style="color:#15803d;">0 mặt hàng / 0 đơn vị nhập</small>
          </div>
          <strong id="receipt-draft-total" style="font-size:1.4rem;color:#14532d;">0 ₫</strong>
        </div>

        <div class="editor-actions" style="margin-top:18px;">
          <button class="button button--ghost" type="button" id="clear-receipt-draft-btn">Làm mới dòng hàng</button>
          <button class="button button--ghost" type="button" id="save-pending-receipt-btn">
            Lưu phiếu chờ xác nhận (PENDING)
          </button>
          <button class="button button--primary" type="button" id="save-confirm-receipt-btn">
            Tạo & Xác nhận nhập kho ngay (CONFIRMED)
          </button>
        </div>
      </section>

      <form class="data-filter" id="wh-receipt-filter" style="grid-template-columns:minmax(220px,1fr) 190px 160px 160px auto auto;">
        <input name="query" placeholder="Tìm theo mã phiếu (PNK-...) hoặc tên nhà cung cấp">
        <select name="status">
          <option value="">Tất cả trạng thái</option>
          <option value="PENDING">PENDING (Chờ xác nhận)</option>
          <option value="CONFIRMED">CONFIRMED (Đã nhập kho)</option>
        </select>
        <label>Từ ngày<input name="fromDate" type="date"></label>
        <label>Đến ngày<input name="toDate" type="date"></label>
        <button class="button button--ghost" type="submit">Lọc phiếu</button>
        <button class="button button--ghost" type="button" id="wh-receipt-reset">Tất cả</button>
      </form>

      <div class="data-panel">
        <div class="data-table-wrap">
          <table class="data-table">
            <thead>
              <tr>
                <th>Mã phiếu nhập</th>
                <th>Nhà cung cấp</th>
                <th>Thời gian lập</th>
                <th>Người lập</th>
                <th>Tổng chi phí</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody id="wh-receipt-rows">
              <tr><td class="empty-cell" colspan="7">Đang tải danh sách phiếu nhập kho...</td></tr>
            </tbody>
          </table>
        </div>
        <p class="data-count" id="wh-receipt-count"></p>
      </div>

      <section class="invoice-detail" id="wh-receipt-detail" hidden></section>
    </section>
  `, { active: 'Nhập hàng (Phiếu nhập)' });

  const errorEl = root.querySelector('#wh-receipt-error');
  const successEl = root.querySelector('#wh-receipt-success');
  const builderSection = root.querySelector('#wh-receipt-builder');
  const supplierSelect = root.querySelector('#receipt-supplier-select');
  const noteInput = root.querySelector('#receipt-note-input');
  const productPicker = root.querySelector('#receipt-product-picker');
  const pickerQty = root.querySelector('#receipt-picker-qty');
  const pickerPrice = root.querySelector('#receipt-picker-price');
  const draftRowsEl = root.querySelector('#receipt-draft-rows');
  const draftSummaryEl = root.querySelector('#receipt-draft-summary');
  const draftTotalEl = root.querySelector('#receipt-draft-total');
  const filterForm = root.querySelector('#wh-receipt-filter');
  const receiptRowsEl = root.querySelector('#wh-receipt-rows');
  const receiptCountEl = root.querySelector('#wh-receipt-count');
  const detailEl = root.querySelector('#wh-receipt-detail');

  let suppliers = [];
  let products = [];
  let draftLines = [];

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

  const syncDefaultPurchasePrice = () => {
    const prod = products.find((p) => p.id === Number(productPicker.value));
    if (prod) {
      // Suggest 70% of salePrice as default purchase price convenience
      const suggested = Math.round((Number(prod.salePrice || 0) * 0.7) / 100) * 100;
      pickerPrice.value = String(suggested);
    }
  };

  const renderBuilderSelectors = () => {
    supplierSelect.innerHTML = `
      <option value="">-- Chọn nhà cung cấp đang hoạt động --</option>
      ${suppliers.map((s) => `
        <option value="${s.id}">
          ${escapeHtml(s.supplierCode)} — ${escapeHtml(s.supplierName)}${s.phone ? ` (${escapeHtml(s.phone)})` : ''}
        </option>
      `).join('')}
    `;

    productPicker.innerHTML = products.length
      ? products.map((p) => `
          <option value="${p.id}">
            ${escapeHtml(p.productCode)} — ${escapeHtml(p.productName)} (Tồn: ${p.stockQuantity ?? 0} | Giá bán: ${formatCurrency(p.salePrice)})
          </option>
        `).join('')
      : '<option value="">-- Không có sản phẩm ACTIVE --</option>';

    syncDefaultPurchasePrice();

    const preselectedSupplierId = sessionStorage.getItem('ktpm.warehouse.preselectSupplierId');
    if (preselectedSupplierId) {
      sessionStorage.removeItem('ktpm.warehouse.preselectSupplierId');
      supplierSelect.value = preselectedSupplierId;
      builderSection.hidden = false;
    }
  };

  const renderDraftLines = () => {
    if (!draftLines.length) {
      draftRowsEl.innerHTML = '<tr><td class="empty-cell" colspan="7">Chưa có mặt hàng nào trong phiếu nhập. Hãy chọn sản phẩm phía trên và nhấn "+ Thêm vào phiếu".</td></tr>';
      draftSummaryEl.textContent = '0 mặt hàng / 0 đơn vị nhập';
      draftTotalEl.textContent = formatCurrency(0);
      return;
    }

    let totalCost = 0;
    let totalUnits = 0;

    draftRowsEl.innerHTML = draftLines.map((line) => {
      const lineTotal = line.quantity * line.purchasePrice;
      totalCost += lineTotal;
      totalUnits += line.quantity;
      return `
        <tr>
          <td><strong>${escapeHtml(line.productCode)}</strong></td>
          <td>${escapeHtml(line.productName)}</td>
          <td>${line.stockQuantity}</td>
          <td>
            <input
              type="number"
              min="1"
              step="1"
              value="${line.quantity}"
              data-draft-qty="${line.productId}"
              style="width:84px;padding:6px;border-radius:7px;border:1px solid #cbd5e1;"
            >
          </td>
          <td>
            <input
              type="number"
              min="0"
              step="100"
              value="${line.purchasePrice}"
              data-draft-price="${line.productId}"
              style="width:128px;padding:6px;border-radius:7px;border:1px solid #cbd5e1;"
            >
          </td>
          <td><strong>${formatCurrency(lineTotal)}</strong></td>
          <td>
            <button class="table-action table-action--danger" type="button" data-draft-remove="${line.productId}">Xóa</button>
          </td>
        </tr>
      `;
    }).join('');

    draftSummaryEl.textContent = `${draftLines.length} mặt hàng / ${totalUnits.toLocaleString('vi-VN')} đơn vị nhập`;
    draftTotalEl.textContent = formatCurrency(totalCost);
  };

  const renderReceiptDetail = (item) => {
    const lines = item.items || item.lines || [];
    const totalCost = item.totalCost ?? item.totalAmount ?? 0;
    detailEl.hidden = false;
    detailEl.innerHTML = `
      <div class="invoice-detail__heading">
        <div>
          <p class="eyebrow">Chi tiết phiếu nhập kho (${escapeHtml(item.status)})</p>
          <h3>${escapeHtml(item.receiptCode)} — ${escapeHtml(item.supplierName)}</h3>
        </div>
        <div style="display:flex;align-items:center;gap:12px;">
          <strong>${formatCurrency(totalCost)}</strong>
          ${item.status === 'PENDING' ? `
            <button class="button button--primary" type="button" data-confirm-receipt="${item.id}">
              Xác nhận nhập kho & Cộng tồn (UC-22)
            </button>
          ` : ''}
        </div>
      </div>
      <dl class="invoice-meta">
        <div><dt>Mã nhà cung cấp</dt><dd>${escapeHtml(item.supplierCode || '—')}</dd></div>
        <div><dt>Tên nhà cung cấp</dt><dd>${escapeHtml(item.supplierName || '—')}</dd></div>
        <div><dt>Nhân viên lập</dt><dd>${escapeHtml(item.createdBy || '—')}</dd></div>
        <div><dt>Thời gian lập</dt><dd>${formatDateTime(item.receiptDate)}</dd></div>
      </dl>
      ${item.note ? `<p style="margin:0 0 12px;"><strong>Ghi chú:</strong> ${escapeHtml(item.note)}</p>` : ''}
      <div class="data-table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>Mã SP</th>
              <th>Tên sản phẩm</th>
              <th>Số lượng nhập</th>
              <th>Đơn giá nhập</th>
              <th>Thành tiền</th>
            </tr>
          </thead>
          <tbody>
            ${lines.map((line) => `
              <tr>
                <td><strong>${escapeHtml(line.productCode)}</strong></td>
                <td>${escapeHtml(line.productName)}</td>
                <td><strong>+${line.quantity}</strong></td>
                <td>${formatCurrency(line.purchasePrice)}</td>
                <td><strong>${formatCurrency(line.lineTotal)}</strong></td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    `;
    detailEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
  };

  const loadMasterData = async () => {
    try {
      const [supData, prodData] = await Promise.all([
        listWarehouseSuppliers({ status: 'ACTIVE', size: 100 }),
        listWarehouseProducts({ status: 'ACTIVE', size: 200 }),
      ]);
      suppliers = supData.content || [];
      products = prodData.content || [];
      renderBuilderSelectors();
    } catch (error) {
      showError(error);
    }
  };

  const loadReceipts = async () => {
    const query = filterForm.elements.query.value.trim();
    const status = filterForm.elements.status.value;
    const fromDate = filterForm.elements.fromDate.value;
    const toDate = filterForm.elements.toDate.value;

    if (fromDate && toDate && fromDate > toDate) {
      showError('Từ ngày không được lớn hơn đến ngày.');
      return;
    }

    try {
      const data = await listPurchaseReceipts({ query, status, fromDate, toDate, size: 100 });
      const list = data.content || [];
      receiptCountEl.textContent = `${(data.totalElements ?? list.length).toLocaleString('vi-VN')} phiếu nhập kho`;
      receiptRowsEl.innerHTML = list.length ? list.map((r) => `
        <tr>
          <td><strong>${escapeHtml(r.receiptCode)}</strong></td>
          <td>
            <strong>${escapeHtml(r.supplierName)}</strong><br>
            <small>${escapeHtml(r.supplierCode || '')}</small>
          </td>
          <td>${formatDateTime(r.receiptDate)}</td>
          <td>${escapeHtml(r.createdBy || '—')}</td>
          <td><strong>${formatCurrency(r.totalCost ?? r.totalAmount ?? 0)}</strong></td>
          <td>${statusBadge(r.status)}</td>
          <td class="table-actions">
            <button class="table-action" type="button" data-view-receipt="${r.id}">Chi tiết</button>
            ${r.status === 'PENDING' ? `
              <button class="table-action" type="button" data-confirm-receipt="${r.id}">Xác nhận nhập</button>
            ` : ''}
          </td>
        </tr>
      `).join('') : '<tr><td class="empty-cell" colspan="7">Không có phiếu nhập kho phù hợp.</td></tr>';
    } catch (error) {
      showError(error);
    }
  };

  const submitReceipt = async (confirmImmediately) => {
    clearAlerts();
    const supplierId = Number(supplierSelect.value);
    if (!supplierId) {
      showError('Vui lòng chọn nhà cung cấp đang hoạt động (Bảng 3.4).');
      return;
    }
    if (!draftLines.length) {
      showError('Phiếu nhập phải có ít nhất một mặt hàng (Bảng 3.4).');
      return;
    }

    const pendingBtn = root.querySelector('#save-pending-receipt-btn');
    const confirmBtn = root.querySelector('#save-confirm-receipt-btn');
    pendingBtn.disabled = true;
    confirmBtn.disabled = true;

    try {
      const created = await createPurchaseReceipt({
        supplierId,
        note: noteInput.value.trim() || null,
        confirmImmediately,
        items: draftLines.map((line) => ({
          productId: line.productId,
          quantity: line.quantity,
          purchasePrice: line.purchasePrice,
        })),
      });

      draftLines = [];
      noteInput.value = '';
      supplierSelect.value = '';
      builderSection.hidden = true;
      renderDraftLines();

      if (confirmImmediately) {
        showSuccess(`Đã lập và xác nhận phiếu nhập ${created.receiptCode}, cộng tồn kho và ghi lịch sử INBOUND thành công!`);
      } else {
        showSuccess(`Đã lưu phiếu nhập ${created.receiptCode} ở trạng thái PENDING (Chờ xác nhận). Tồn kho sẽ được cộng khi xác nhận phiếu.`);
      }

      renderReceiptDetail(created);
      await Promise.all([loadReceipts(), loadMasterData()]);
    } catch (error) {
      showError(error);
    } finally {
      pendingBtn.disabled = false;
      confirmBtn.disabled = false;
    }
  };

  root.querySelector('#toggle-create-receipt-btn').addEventListener('click', () => {
    builderSection.hidden = !builderSection.hidden;
  });

  root.querySelector('#close-receipt-builder-btn').addEventListener('click', () => {
    builderSection.hidden = true;
  });

  productPicker.addEventListener('change', syncDefaultPurchasePrice);

  root.querySelector('#add-receipt-line-btn').addEventListener('click', () => {
    clearAlerts();
    const productId = Number(productPicker.value);
    const prod = products.find((p) => p.id === productId);
    if (!prod) {
      showError('Vui lòng chọn sản phẩm hợp lệ.');
      return;
    }
    const qty = parseInt(pickerQty.value, 10);
    const price = parseFloat(pickerPrice.value);
    if (Number.isNaN(qty) || qty <= 0) {
      showError('Số lượng nhập phải lớn hơn 0 (Bảng 3.4).');
      return;
    }
    if (Number.isNaN(price) || price < 0) {
      showError('Đơn giá nhập không được âm (Bảng 3.4).');
      return;
    }

    const existing = draftLines.find((item) => item.productId === productId);
    if (existing) {
      existing.quantity += qty;
      existing.purchasePrice = price;
    } else {
      draftLines.push({
        productId: prod.id,
        productCode: prod.productCode,
        productName: prod.productName,
        stockQuantity: prod.stockQuantity ?? 0,
        quantity: qty,
        purchasePrice: price,
      });
    }
    renderDraftLines();
  });

  root.querySelector('#clear-receipt-draft-btn').addEventListener('click', () => {
    draftLines = [];
    clearAlerts();
    renderDraftLines();
  });

  root.querySelector('#save-pending-receipt-btn').addEventListener('click', () => submitReceipt(false));
  root.querySelector('#save-confirm-receipt-btn').addEventListener('click', () => submitReceipt(true));

  root.addEventListener('change', (event) => {
    const qtyInput = event.target.closest('[data-draft-qty]');
    if (qtyInput) {
      const id = Number(qtyInput.dataset.draftQty);
      const line = draftLines.find((l) => l.productId === id);
      const val = parseInt(qtyInput.value, 10);
      if (line) {
        line.quantity = Number.isNaN(val) || val < 1 ? 1 : val;
        renderDraftLines();
      }
      return;
    }

    const priceInput = event.target.closest('[data-draft-price]');
    if (priceInput) {
      const id = Number(priceInput.dataset.draftPrice);
      const line = draftLines.find((l) => l.productId === id);
      const val = parseFloat(priceInput.value);
      if (line) {
        line.purchasePrice = Number.isNaN(val) || val < 0 ? 0 : val;
        renderDraftLines();
      }
    }
  });

  root.addEventListener('click', async (event) => {
    const removeBtn = event.target.closest('[data-draft-remove]');
    if (removeBtn) {
      const id = Number(removeBtn.dataset.draftRemove);
      draftLines = draftLines.filter((l) => l.productId !== id);
      renderDraftLines();
      return;
    }

    const viewBtn = event.target.closest('[data-view-receipt]');
    if (viewBtn) {
      clearAlerts();
      try {
        const detail = await getPurchaseReceiptDetail(viewBtn.dataset.viewReceipt);
        renderReceiptDetail(detail);
      } catch (error) {
        showError(error);
      }
      return;
    }

    const confirmBtn = event.target.closest('[data-confirm-receipt]');
    if (confirmBtn) {
      clearAlerts();
      confirmBtn.disabled = true;
      try {
        const updated = await confirmPurchaseReceipt(confirmBtn.dataset.confirmReceipt);
        const lineCount = (updated.items || updated.lines || []).length;
        showSuccess(`Đã xác nhận phiếu nhập ${updated.receiptCode}, cộng tồn kho cho ${lineCount} mặt hàng và ghi lịch sử INBOUND!`);
        renderReceiptDetail(updated);
        await Promise.all([loadReceipts(), loadMasterData()]);
      } catch (error) {
        showError(error);
      } finally {
        confirmBtn.disabled = false;
      }
    }
  });

  filterForm.addEventListener('submit', (event) => {
    event.preventDefault();
    clearAlerts();
    loadReceipts();
  });

  root.querySelector('#wh-receipt-reset').addEventListener('click', () => {
    filterForm.elements.query.value = '';
    filterForm.elements.status.value = '';
    filterForm.elements.fromDate.value = '';
    filterForm.elements.toDate.value = '';
    clearAlerts();
    loadReceipts();
  });

  await Promise.all([loadMasterData(), loadReceipts()]);
}
