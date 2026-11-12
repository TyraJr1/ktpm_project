import { clearAuth, getAuth } from '../auth/authStore.js';

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export class ApiError extends Error {
  constructor(status, message, payload = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.payload = payload;
  }
}

async function parseBody(response) {
  const contentType = response.headers.get('content-type') || '';
  if (response.status === 204) return null;
  if (contentType.includes('application/json')) return response.json();
  return response.text();
}

export async function apiRequest(path, options = {}) {
  const { method = 'GET', body, authenticated = false } = options;
  const headers = { Accept: 'application/json' };

  if (body !== undefined) headers['Content-Type'] = 'application/json';

  if (authenticated) {
    const auth = getAuth();
    if (auth) headers.Authorization = `${auth.tokenType} ${auth.accessToken}`;
  }

  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new ApiError(0, 'Không thể kết nối tới máy chủ. Vui lòng thử lại.');
  }

  const payload = await parseBody(response);

  if (response.status === 401 && authenticated) {
    clearAuth();
    window.dispatchEvent(new CustomEvent('auth:unauthorized'));
  }

  if (!response.ok) {
    const fallback = response.status === 403
      ? 'Bạn không có quyền truy cập chức năng này.'
      : 'Yêu cầu không thành công.';
    throw new ApiError(response.status, payload?.message || fallback, payload);
  }

  return payload;
}

export function login(username, password) {
  return apiRequest('/api/auth/login', {
    method: 'POST',
    body: { username, password },
  });
}

export function logout() {
  return apiRequest('/api/auth/logout', {
    method: 'POST',
    authenticated: true,
  });
}

export function verifyAdminAccess() {
  return apiRequest('/api/admin/ping', { authenticated: true });
}

export function verifySalesAccess() {
  return apiRequest('/api/sales/ping', { authenticated: true });
}

export function verifyWarehouseAccess() {
  return apiRequest('/api/warehouse/ping', { authenticated: true });
}

function queryString(params = {}) {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.set(key, value);
  });
  const value = search.toString();
  return value ? `?${value}` : '';
}

export function listCategories() {
  return apiRequest('/api/admin/categories', { authenticated: true });
}

export function saveCategory(id, body) {
  return apiRequest(`/api/admin/categories${id ? `/${id}` : ''}`, {
    method: id ? 'PUT' : 'POST', body, authenticated: true,
  });
}

export function listProducts(params) {
  return apiRequest(`/api/admin/products${queryString(params)}`, { authenticated: true });
}

export function saveProduct(id, body) {
  return apiRequest(`/api/admin/products${id ? `/${id}` : ''}`, {
    method: id ? 'PUT' : 'POST', body, authenticated: true,
  });
}

export function updateProductStatus(id, status) {
  return apiRequest(`/api/admin/products/${id}/status`, {
    method: 'PATCH', body: { status }, authenticated: true,
  });
}

export function listSuppliers(params) {
  return apiRequest(`/api/admin/suppliers${queryString(params)}`, { authenticated: true });
}

export function saveSupplier(id, body) {
  return apiRequest(`/api/admin/suppliers${id ? `/${id}` : ''}`, {
    method: id ? 'PUT' : 'POST', body, authenticated: true,
  });
}

export function listInventory(params) {
  return apiRequest(`/api/admin/inventory${queryString(params)}`, { authenticated: true });
}

export function listInvoices(params) {
  return apiRequest(`/api/admin/invoices${queryString(params)}`, { authenticated: true });
}

export function getInvoiceDetail(id) {
  return apiRequest(`/api/admin/invoices/${id}`, { authenticated: true });
}

export function listAccounts(params) {
  return apiRequest(`/api/admin/accounts${queryString(params)}`, { authenticated: true });
}

export function createAccount(body) {
  return apiRequest('/api/admin/accounts', { method: 'POST', body, authenticated: true });
}

export function updateAccount(id, body) {
  return apiRequest(`/api/admin/accounts/${id}`, { method: 'PUT', body, authenticated: true });
}

export function changeAccountPassword(id, password) {
  return apiRequest(`/api/admin/accounts/${id}/password`, {
    method: 'PATCH', body: { password }, authenticated: true,
  });
}

export function deleteAccount(id) {
  return apiRequest(`/api/admin/accounts/${id}`, { method: 'DELETE', authenticated: true });
}

export function getReportSummary(fromDate, toDate) {
  return apiRequest(`/api/admin/reports/summary${queryString({ fromDate, toDate })}`, { authenticated: true });
}

export function getTopProducts(fromDate, toDate, limit = 10) {
  return apiRequest(`/api/admin/reports/top-products${queryString({ fromDate, toDate, limit })}`, { authenticated: true });
}

export function getInventoryReport(params = {}) {
  return apiRequest(`/api/admin/reports/inventory${queryString(params)}`, { authenticated: true });
}


// ── Sales Staff API ──
export function getSalesDashboard() {
  return apiRequest('/api/sales/dashboard', { authenticated: true });
}

export function listSalesProducts(params) {
  return apiRequest(`/api/sales/products${queryString(params)}`, { authenticated: true });
}

export function createSalesOrder(body) {
  return apiRequest('/api/sales/orders', { method: 'POST', body, authenticated: true });
}

export function getSalesOrderDetail(id) {
  return apiRequest(`/api/sales/orders/${id}`, { authenticated: true });
}

export function listSalesInvoices(params) {
  return apiRequest(`/api/sales/invoices${queryString(params)}`, { authenticated: true });
}

export function getSalesInvoiceDetail(id) {
  return apiRequest(`/api/sales/invoices/${id}`, { authenticated: true });
}

export function listCustomers(params) {
  return apiRequest(`/api/sales/customers${queryString(params)}`, { authenticated: true });
}

export function saveCustomer(id, body) {
  return apiRequest(`/api/sales/customers${id ? `/${id}` : ''}`, {
    method: id ? 'PUT' : 'POST', body, authenticated: true,
  });
}

export function getCustomerPurchaseHistory(id) {
  return apiRequest(`/api/sales/customers/${id}/purchase-history`, { authenticated: true });
}

// ── Warehouse Staff API ──
export function getWarehouseDashboard() {
  return apiRequest('/api/warehouse/dashboard', { authenticated: true });
}

export function listWarehouseProducts(params) {
  return apiRequest(`/api/warehouse/products${queryString(params)}`, { authenticated: true });
}

export function listWarehouseSuppliers(params) {
  return apiRequest(`/api/warehouse/suppliers${queryString(params)}`, { authenticated: true });
}

export function listPurchaseReceipts(params) {
  return apiRequest(`/api/warehouse/receipts${queryString(params)}`, { authenticated: true });
}

export function getPurchaseReceiptDetail(id) {
  return apiRequest(`/api/warehouse/receipts/${id}`, { authenticated: true });
}

export function createPurchaseReceipt(body) {
  return apiRequest('/api/warehouse/receipts', { method: 'POST', body, authenticated: true });
}

export function confirmPurchaseReceipt(id) {
  return apiRequest(`/api/warehouse/receipts/${id}/confirm`, { method: 'POST', authenticated: true });
}

export function createStockOutbound(body) {
  return apiRequest('/api/warehouse/outbound', { method: 'POST', body, authenticated: true });
}

export function createStockAdjustment(body) {
  return apiRequest('/api/warehouse/adjustments', { method: 'POST', body, authenticated: true });
}

export function listStockTransactions(params) {
  return apiRequest(`/api/warehouse/transactions${queryString(params)}`, { authenticated: true });
}

