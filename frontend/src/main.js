import './styles/main.css';
import { redirectAuthenticatedUser, requireAuth } from './auth/routeGuard.js';
import { routeForRole } from './auth/authStore.js';
import { renderAdminDashboardPage } from './pages/AdminDashboardPage.js';
import {
  renderCategoryPage, renderInventoryPage, renderInvoicePage, renderProductPage,
  renderSupplierPage,
} from './pages/AdminDataPages.js';
import { renderLoginPage } from './pages/LoginPage.js';
import { renderForbiddenPage, renderRolePlaceholderPage } from './pages/RolePlaceholderPage.js';
import { renderAccountPage, renderReportPage } from './pages/AdminAccountReportPages.js';
import { currentPath, navigate } from './utils/router.js';

const root = document.querySelector('#app');

async function renderRoute() {
  const path = currentPath();

  if (path === '/' || path === '/login') {
    if (!redirectAuthenticatedUser()) renderLoginPage(root);
    return;
  }

  const routeRoles = {
    '/admin/dashboard': 'ADMIN',
    '/admin/categories': 'ADMIN',
    '/admin/products': 'ADMIN',
    '/admin/suppliers': 'ADMIN',
    '/admin/inventory': 'ADMIN',
    '/admin/invoices': 'ADMIN',
    '/admin/accounts': 'ADMIN',
    '/admin/reports': 'ADMIN',
    '/sales': 'SALES_STAFF',
    '/warehouse': 'WAREHOUSE_STAFF',
  };
  const requiredRole = routeRoles[path];

  if (requiredRole) {
    const guard = requireAuth(requiredRole);
    if (guard.reason === 'forbidden') {
      renderForbiddenPage(root, guard.auth);
      return;
    }
    if (!guard.allowed) return;

    const adminPages = {
      '/admin/dashboard': renderAdminDashboardPage,
      '/admin/categories': renderCategoryPage,
      '/admin/products': renderProductPage,
      '/admin/suppliers': renderSupplierPage,
      '/admin/inventory': renderInventoryPage,
      '/admin/invoices': renderInvoicePage,
      '/admin/accounts': renderAccountPage,
      '/admin/reports': renderReportPage,
    };
    if (requiredRole === 'ADMIN') await adminPages[path](root, guard.auth);
    else await renderRolePlaceholderPage(root, guard.auth);
    return;
  }

  const guard = requireAuth();
  if (guard.allowed) navigate(routeForRole(guard.auth.role), { replace: true });
}

document.addEventListener('click', (event) => {
  const link = event.target.closest('a[data-link], a[data-home-link]');
  if (!link) return;
  event.preventDefault();
  if (link.hasAttribute('data-home-link')) {
    const guard = requireAuth();
    if (guard.allowed) navigate(routeForRole(guard.auth.role));
    return;
  }
  navigate(link.getAttribute('href'));
});

window.addEventListener('popstate', renderRoute);
window.addEventListener('auth:unauthorized', () => navigate('/login', { replace: true }));
renderRoute();
