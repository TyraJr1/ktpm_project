import { ApiError, login } from '../api/client.js';
import { routeForRole, saveAuth } from '../auth/authStore.js';
import { navigate } from '../utils/router.js';

export function renderLoginPage(root) {
  root.innerHTML = `
    <main class="login-page">
      <section class="login-intro" aria-label="Giới thiệu hệ thống">
        <div class="login-intro__content">
          <span class="brand__mark brand__mark--large">VP</span>
          <p class="eyebrow eyebrow--light">Vận hành cửa hàng</p>
          <h1>Quản lý bán hàng<br/>văn phòng phẩm</h1>
          <p>Nền tảng vận hành tập trung dành cho quản lý, nhân viên bán hàng và nhân viên kho.</p>
          <div class="login-intro__status"><span></span> Quản lý tập trung, thao tác thuận tiện</div>
        </div>
      </section>
      <section class="login-panel">
        <div class="login-card">
          <div class="login-card__heading">
            <p class="eyebrow">Chào mừng trở lại</p>
            <h2>Đăng nhập hệ thống</h2>
            <p>Sử dụng tài khoản được cấp để tiếp tục.</p>
          </div>
          <form id="login-form" novalidate>
            <div class="field">
              <label for="username">Tên đăng nhập</label>
              <input id="username" name="username" type="text" autocomplete="username" placeholder="Nhập tên đăng nhập" />
              <small class="field__error" id="username-error"></small>
            </div>
            <div class="field">
              <label for="password">Mật khẩu</label>
              <div class="password-wrap">
                <input id="password" name="password" type="password" autocomplete="current-password" placeholder="Nhập mật khẩu" />
                <button type="button" class="password-toggle" id="password-toggle" aria-label="Hiện mật khẩu">Hiện</button>
              </div>
              <small class="field__error" id="password-error"></small>
            </div>
            <div class="form-alert" id="login-error" role="alert" hidden></div>
            <button class="button button--primary button--full" id="login-button" type="submit">Đăng nhập</button>
          </form>
          <p class="login-card__note">Nếu gặp vấn đề với tài khoản, vui lòng liên hệ quản trị viên.</p>
        </div>
      </section>
    </main>`;

  const form = root.querySelector('#login-form');
  const username = root.querySelector('#username');
  const password = root.querySelector('#password');
  const submitButton = root.querySelector('#login-button');
  const formError = root.querySelector('#login-error');

  root.querySelector('#password-toggle').addEventListener('click', (event) => {
    const showing = password.type === 'text';
    password.type = showing ? 'password' : 'text';
    event.currentTarget.textContent = showing ? 'Hiện' : 'Ẩn';
    event.currentTarget.setAttribute('aria-label', showing ? 'Hiện mật khẩu' : 'Ẩn mật khẩu');
  });

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    formError.hidden = true;
    const usernameValue = username.value.trim();
    const passwordValue = password.value;
    const usernameError = root.querySelector('#username-error');
    const passwordError = root.querySelector('#password-error');
    usernameError.textContent = usernameValue ? '' : 'Tên đăng nhập không được để trống.';
    passwordError.textContent = passwordValue ? '' : 'Mật khẩu không được để trống.';
    username.classList.toggle('is-invalid', !usernameValue);
    password.classList.toggle('is-invalid', !passwordValue);
    if (!usernameValue || !passwordValue) return;

    submitButton.disabled = true;
    submitButton.textContent = 'Đang đăng nhập…';

    try {
      const response = await login(usernameValue, passwordValue);
      const auth = saveAuth(response);
      password.value = '';
      navigate(routeForRole(auth.role), { replace: true });
    } catch (error) {
      formError.hidden = false;
      if (error instanceof ApiError && error.status === 401) {
        formError.textContent = 'Tên đăng nhập hoặc mật khẩu không chính xác.';
      } else if (error instanceof ApiError && error.status === 403) {
        formError.textContent = 'Bạn không có quyền truy cập chức năng này.';
      } else {
        formError.textContent = error.message || 'Đăng nhập không thành công.';
      }
    } finally {
      submitButton.disabled = false;
      submitButton.textContent = 'Đăng nhập';
    }
  });

  username.focus();
}
