import { getAuth, routeForRole } from './authStore.js';
import { navigate } from '../utils/router.js';

export function requireAuth(requiredRole = null) {
  const auth = getAuth();
  if (!auth) {
    navigate('/login', { replace: true });
    return { allowed: false, auth: null, reason: 'unauthenticated' };
  }

  if (requiredRole && auth.role !== requiredRole) {
    return { allowed: false, auth, reason: 'forbidden' };
  }

  return { allowed: true, auth, reason: null };
}

export function redirectAuthenticatedUser() {
  const auth = getAuth();
  if (!auth) return false;
  navigate(routeForRole(auth.role), { replace: true });
  return true;
}
