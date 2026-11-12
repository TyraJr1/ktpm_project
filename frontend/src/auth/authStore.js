const STORAGE_KEY = 'ktpm.auth.v1';
const SUPPORTED_ROLES = new Set(['ADMIN', 'SALES_STAFF', 'WAREHOUSE_STAFF']);

function isValidAuthState(value) {
  return Boolean(
    value
      && typeof value.accessToken === 'string'
      && value.accessToken.length > 0
      && value.tokenType === 'Bearer'
      && typeof value.username === 'string'
      && typeof value.fullName === 'string'
      && SUPPORTED_ROLES.has(value.role),
  );
}

export function getAuth() {
  try {
    const value = JSON.parse(localStorage.getItem(STORAGE_KEY));
    if (isValidAuthState(value)) return value;
  } catch {
    // Invalid local state is treated as signed out.
  }
  clearAuth();
  return null;
}

export function saveAuth(loginResponse) {
  const authState = {
    accessToken: loginResponse.accessToken,
    tokenType: loginResponse.tokenType,
    userId: loginResponse.userId,
    username: loginResponse.username,
    fullName: loginResponse.fullName,
    role: loginResponse.role,
  };

  if (!isValidAuthState(authState)) {
    throw new Error('Phản hồi đăng nhập không đúng contract.');
  }

  localStorage.setItem(STORAGE_KEY, JSON.stringify(authState));
  return authState;
}

export function clearAuth() {
  localStorage.removeItem(STORAGE_KEY);
}

export function routeForRole(role) {
  if (role === 'ADMIN') return '/admin/dashboard';
  if (role === 'SALES_STAFF') return '/sales/dashboard';
  if (role === 'WAREHOUSE_STAFF') return '/warehouse/dashboard';
  return '/login';
}
