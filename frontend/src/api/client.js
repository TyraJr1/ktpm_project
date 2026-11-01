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
