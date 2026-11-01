const roleLabels = {
  ADMIN: 'Quản trị viên',
  SALES_STAFF: 'Nhân viên bán hàng',
  WAREHOUSE_STAFF: 'Nhân viên kho',
};

export function displayRole(role) {
  return roleLabels[role] || 'Người dùng';
}

export function displayName(auth) {
  if (auth.role === 'ADMIN') return displayRole(auth.role);
  return auth.fullName || displayRole(auth.role);
}

export function initialsFor(auth) {
  return displayRole(auth.role).split(' ').slice(-2).map((part) => part.charAt(0)).join('').toUpperCase();
}
