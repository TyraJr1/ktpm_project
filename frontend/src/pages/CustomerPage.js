import { ApiError, listCustomers, saveCustomer } from '../api/client.js';
import { renderSalesShell } from '../components/SalesShell.js';

let customers = [];
let searchTerm = '';

function filtered() {
  if (!searchTerm) return customers;
  const q = searchTerm.toLowerCase();
  return customers.filter((c) =>
    (c.name || '').toLowerCase().includes(q) || (c.phone || '').includes(q),
  );
}

function renderTable(container) {
  const rows = filtered();
  if (rows.length === 0) {
    container.innerHTML = '<tr><td colspan="5" style="text-align:center;padding:1rem">Không tìm thấy khách hàng.</td></tr>';
    return;
  }
  container.innerHTML = rows.map((c) => `
    <tr>
      <td>${c.name || ''}</td>
      <td>${c.phone || ''}</td>
      <td>${c.email || '—'}</td>
      <td>${c.address || '—'}</td>
      <td><button class="button button--ghost" data-edit-id="${c.id}" type="button">Sửa</button></td>
    </tr>`).join('');
}

function openModal(root, customer = null) {
  let overlay = root.querySelector('#customer-modal-overlay');
  if (overlay) overlay.remove();

  const isEdit = customer !== null;
  const title = isEdit ? 'Sửa khách hàng' : 'Thêm khách hàng';

  overlay = document.createElement('div');
  overlay.id = 'customer-modal-overlay';
  overlay.style.cssText = 'position:fixed;inset:0;background:rgba(0,0,0,.45);display:flex;align-items:center;justify-content:center;z-index:1000';
  overlay.innerHTML = `
    <div style="background:#fff;border-radius:8px;padding:1.5rem;width:24rem;max-width:95vw">
      <h2 style="margin:0 0 1rem">${title}</h2>
      <form id="customer-form">
        <label style="display:block;margin-bottom:.75rem">
          <span style="font-weight:600;display:block;margin-bottom:.25rem">Tên *</span>
          <input type="text" name="name" required class="form-input" style="width:100%" value="${isEdit ? (customer.name || '') : ''}" />
        </label>
        <label style="display:block;margin-bottom:.75rem">
          <span style="font-weight:600;display:block;margin-bottom:.25rem">SĐT *</span>
          <input type="text" name="phone" required class="form-input" style="width:100%" value="${isEdit ? (customer.phone || '') : ''}" />
        </label>
        <label style="display:block;margin-bottom:.75rem">
          <span style="font-weight:600;display:block;margin-bottom:.25rem">Email</span>
          <input type="email" name="email" class="form-input" style="width:100%" value="${isEdit ? (customer.email || '') : ''}" />
        </label>
        <label style="display:block;margin-bottom:1rem">
          <span style="font-weight:600;display:block;margin-bottom:.25rem">Địa chỉ</span>
          <input type="text" name="address" class="form-input" style="width:100%" value="${isEdit ? (customer.address || '') : ''}" />
        </label>
        <div class="form-alert" id="modal-error" role="alert" hidden></div>
        <div style="display:flex;justify-content:flex-end;gap:.5rem;margin-top:.75rem">
          <button type="button" class="button button--ghost" id="btn-cancel-modal">Hủy</button>
          <button type="submit" class="button" id="btn-save-customer">Lưu</button>
        </div>
      </form>
    </div>`;

  root.appendChild(overlay);

  overlay.querySelector('#btn-cancel-modal').addEventListener('click', () => overlay.remove());
  overlay.addEventListener('click', (e) => { if (e.target === overlay) overlay.remove(); });

  overlay.querySelector('#customer-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const form = e.target;
    const body = {
      name: form.name.value.trim(),
      phone: form.phone.value.trim(),
      email: form.email.value.trim(),
      address: form.address.value.trim(),
    };

    const saveBtn = form.querySelector('#btn-save-customer');
    saveBtn.disabled = true;
    saveBtn.textContent = 'Đang lưu…';

    try {
      await saveCustomer(isEdit ? customer.id : null, body);
      overlay.remove();
      await loadCustomers(root);
    } catch (error) {
      const errEl = form.querySelector('#modal-error');
      errEl.textContent = error instanceof ApiError ? error.message : 'Lỗi khi lưu khách hàng.';
      errEl.hidden = false;
      saveBtn.disabled = false;
      saveBtn.textContent = 'Lưu';
    }
  });
}

async function loadCustomers(root) {
  try {
    const data = await listCustomers({});
    customers = data.content || data || [];
  } catch {
    customers = [];
  }
  const tbody = root.querySelector('#customers-tbody');
  if (tbody) renderTable(tbody);
}

export async function renderCustomerPage(root, auth) {
  customers = [];
  searchTerm = '';

  renderSalesShell(root, auth, `
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:1rem;flex-wrap:wrap;gap:.75rem">
      <div>
        <input type="text" id="customer-search" class="form-input" placeholder="Tìm kiếm theo tên hoặc SĐT…" style="width:20rem;max-width:100%" />
      </div>
      <button class="button" id="btn-add-customer" type="button">+ Thêm khách hàng</button>
    </div>
    <div style="overflow-x:auto;background:#fff;border-radius:8px;box-shadow:0 1px 3px rgba(0,0,0,.1)">
      <table class="data-table" style="width:100%">
        <thead><tr><th>Tên</th><th>SĐT</th><th>Email</th><th>Địa chỉ</th><th>Thao tác</th></tr></thead>
        <tbody id="customers-tbody"><tr><td colspan="5" style="text-align:center;padding:1rem">Đang tải…</td></tr></tbody>
      </table>
    </div>
    <div class="form-alert" id="page-error" role="alert" hidden></div>
  `, { active: 'Khách hàng' });

  await loadCustomers(root);

  // Search
  root.querySelector('#customer-search').addEventListener('input', (e) => {
    searchTerm = e.target.value;
    const tbody = root.querySelector('#customers-tbody');
    if (tbody) renderTable(tbody);
  });

  // Add customer
  root.querySelector('#btn-add-customer').addEventListener('click', () => openModal(root));

  // Edit customer (delegation)
  root.addEventListener('click', (e) => {
    const editBtn = e.target.closest('[data-edit-id]');
    if (editBtn) {
      const id = Number(editBtn.dataset.editId);
      const customer = customers.find((c) => c.id === id);
      if (customer) openModal(root, customer);
    }
  });
}
