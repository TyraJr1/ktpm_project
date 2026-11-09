export function navigate(path, { replace = false } = {}) {
  if (window.location.pathname === path) {
    window.dispatchEvent(new PopStateEvent('popstate'));
    return;
  }

  if (replace) window.history.replaceState({}, '', path);
  else window.history.pushState({}, '', path);
  window.dispatchEvent(new PopStateEvent('popstate'));
}

export function currentPath() {
  return window.location.pathname.replace(/\/+$/, '') || '/';
}

