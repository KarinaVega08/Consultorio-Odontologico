(function () {
  const alertBox = document.getElementById('alert-box');
  const authSection = document.getElementById('auth-section');
  const profileSection = document.getElementById('profile-section');
  const tabLogin = document.getElementById('tab-login');
  const tabRegister = document.getElementById('tab-register');
  const loginForm = document.getElementById('login-form');
  const registerForm = document.getElementById('register-form');
  const recoverForm = document.getElementById('recover-form');
  const resetForm = document.getElementById('reset-form');
  const forgotBtn = document.getElementById('forgot-btn');
  const backToLoginBtn = document.getElementById('back-to-login-btn');
  const resetBackBtn = document.getElementById('reset-back-btn');
  const tabsBar = document.querySelector('#auth-section .tabs');
  const gmailForm = document.getElementById('gmail-form');
  const gmailLinkBox = document.getElementById('gmail-link-box');
  const logoutBtn = document.getElementById('logout-btn');

  function showAlert(message, type) {
    alertBox.textContent = message;
    alertBox.className = 'alert ' + type;
  }

  function clearAlert() {
    alertBox.textContent = '';
    alertBox.className = 'alert';
  }

  /** Vuelve a la página que pidió login (?redirect=/reservar.html). */
  function redirectIfRequested() {
    const target = new URLSearchParams(window.location.search).get('redirect');
    if (target && target.startsWith('/') && !target.startsWith('//')) {
      window.location.href = target;
      return true;
    }
    return false;
  }

  function switchTab(mode) {
    clearAlert();
    const isLogin = mode === 'login';
    tabLogin.classList.toggle('active', isLogin);
    tabRegister.classList.toggle('active', !isLogin);
    loginForm.classList.toggle('hidden', !isLogin);
    registerForm.classList.toggle('hidden', isLogin);
    if (recoverForm) recoverForm.classList.add('hidden');
    if (resetForm) resetForm.classList.add('hidden');
    if (tabsBar) tabsBar.classList.remove('hidden');
    if (authTitle) authTitle.textContent = 'Bienvenido';
    if (authSubtitle) authSubtitle.textContent = 'Ingresá con tu DNI y contraseña o creá tu cuenta.';
  }

  const authTitle = document.getElementById('auth-title');
  const authSubtitle = document.getElementById('auth-subtitle');

  function renderProfile(patient) {
    authSection.classList.add('hidden');
    profileSection.classList.remove('hidden');
    if (authTitle) authTitle.textContent = 'Mi cuenta';
    if (authSubtitle) authSubtitle.textContent = 'Tus datos de acceso y recuperación.';
    document.getElementById('prof-name').textContent = patient.nombreCompleto;
    document.getElementById('prof-dni').textContent = patient.dni;
    document.getElementById('prof-gmail').textContent = patient.emailRecuperacion || 'No vinculado';
    gmailLinkBox.classList.toggle('hidden', !patient.requiereVincularGmail);
  }

  function extractErrorMessage(data, fallback) {
    if (data && data.fieldErrors) {
      const msgs = Object.values(data.fieldErrors);
      if (msgs.length > 0) return msgs.join('. ');
    }
    return (data && data.message) ? data.message : fallback;
  }

  tabLogin.addEventListener('click', () => switchTab('login'));
  tabRegister.addEventListener('click', () => switchTab('register'));

  registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearAlert();
    const payload = {
      nombreCompleto: document.getElementById('reg-name').value.trim(),
      dni: document.getElementById('reg-dni').value.trim(),
      password: document.getElementById('reg-password').value
    };
    try {
      const res = await Api.post('/api/auth/register', payload);
      if (!res.ok) {
        showAlert(extractErrorMessage(res.data, 'No se pudo completar el registro'), 'error');
        return;
      }
      registerForm.reset();
      try {
        const loginRes = await Api.post('/api/auth/login', {
          dni: payload.dni,
          password: payload.password
        });
        if (loginRes.ok) {
          Api.setToken(loginRes.data.token);
          if (window.Nav && window.Nav.refresh) window.Nav.refresh();
          renderProfile(loginRes.data.patient);
          showAlert('Cuenta creada e inicio de sesión exitoso.', 'success');
          if (!loginRes.data.patient.requiereVincularGmail) {
            redirectIfRequested();
          }
          return;
        }
      } catch {
        // Fallback al login manual si falla el auto-login
      }
      switchTab('login');
      document.getElementById('login-dni').value = payload.dni;
      showAlert('Cuenta creada correctamente. Ya podés iniciar sesión.', 'success');
    } catch {
      showAlert('Error de conexión con el servidor', 'error');
    }
  });

  loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearAlert();
    const payload = {
      dni: document.getElementById('login-dni').value.trim(),
      password: document.getElementById('login-password').value
    };
    try {
      const res = await Api.post('/api/auth/login', payload);
      if (!res.ok) {
        showAlert(extractErrorMessage(res.data, 'DNI o contraseña inválidos'), 'error');
        return;
      }
      Api.setToken(res.data.token);
      if (window.Nav && window.Nav.refresh) window.Nav.refresh();
      loginForm.reset();
      renderProfile(res.data.patient);
      showAlert('Sesión iniciada correctamente.', 'success');
      if (!res.data.patient.requiereVincularGmail) {
        redirectIfRequested();
      }
    } catch {
      showAlert('Error de conexión con el servidor', 'error');
    }
  });

  if (forgotBtn && recoverForm) {
    forgotBtn.addEventListener('click', () => {
      clearAlert();
      loginForm.classList.add('hidden');
      recoverForm.classList.remove('hidden');
      const dniActual = document.getElementById('login-dni').value.trim();
      if (dniActual) document.getElementById('rec-dni').value = dniActual;
    });
  }

  if (backToLoginBtn) {
    backToLoginBtn.addEventListener('click', () => switchTab('login'));
  }

  if (recoverForm) {
    recoverForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      clearAlert();
      const dni = document.getElementById('rec-dni').value.trim();
      try {
        const res = await Api.post('/api/auth/recover-password', { dni });
        if (!res.ok) {
          showAlert(extractErrorMessage(res.data, 'No se pudo procesar la recuperación'), 'error');
          return;
        }
        recoverForm.reset();
        switchTab('login');
        showAlert('Si el DNI tiene un Gmail vinculado, enviamos un enlace de recuperación.', 'success');
      } catch {
        showAlert('Error de conexión con el servidor', 'error');
      }
    });
  }

  gmailForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearAlert();
    if (!Api.isLoggedIn()) {
      showAlert('Sesión expirada. Volvé a iniciar sesión.', 'error');
      return;
    }
    const email = document.getElementById('gmail-input').value.trim();
    try {
      const res = await Api.post('/api/patients/me/recovery-email', { email });
      if (!res.ok) {
        showAlert(extractErrorMessage(res.data, 'No se pudo vincular el Gmail'), 'error');
        return;
      }
      gmailForm.reset();
      renderProfile(res.data);
      showAlert('Gmail de recuperación vinculado correctamente.', 'success');
      redirectIfRequested();
    } catch {
      showAlert('Error de conexión con el servidor', 'error');
    }
  });

  logoutBtn.addEventListener('click', () => {
    Api.clearToken();
    if (window.Nav && window.Nav.refresh) window.Nav.refresh();
    profileSection.classList.add('hidden');
    authSection.classList.remove('hidden');
    if (authTitle) authTitle.textContent = 'Bienvenido';
    if (authSubtitle) authSubtitle.textContent = 'Ingresá con tu DNI y contraseña o creá tu cuenta.';
    switchTab('login');
  });

  (function showRedirectReason() {
    if (Api.isLoggedIn()) return;
    const params = new URLSearchParams(window.location.search);
    const resetToken = params.get('resetToken');
    if (resetToken && resetForm) {
      if (tabsBar) tabsBar.classList.add('hidden');
      loginForm.classList.add('hidden');
      registerForm.classList.add('hidden');
      if (recoverForm) recoverForm.classList.add('hidden');
      resetForm.classList.remove('hidden');
      if (authTitle) authTitle.textContent = 'Restablecer contraseña';
      if (authSubtitle) authSubtitle.textContent = 'Ingresá tu nueva contraseña para recuperar el acceso.';
      return;
    }
    if (params.get('reason') === 'turnos') {
      showAlert('Iniciá sesión para ver y gestionar tus turnos.', 'info');
    }
  })();

  if (resetBackBtn) {
    resetBackBtn.addEventListener('click', () => {
      if (window.history && window.history.replaceState) {
        window.history.replaceState({}, '', '/login.html');
      }
      switchTab('login');
    });
  }

  if (resetForm) {
    resetForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      clearAlert();
      const token = new URLSearchParams(window.location.search).get('resetToken');
      const newPassword = document.getElementById('reset-password').value;
      const confirm = document.getElementById('reset-password-confirm').value;
      if (newPassword !== confirm) {
        showAlert('Las contraseñas no coinciden.', 'error');
        return;
      }
      try {
        const res = await Api.post('/api/auth/reset-password', { token, newPassword });
        if (!res.ok) {
          showAlert(extractErrorMessage(res.data, 'No se pudo restablecer la contraseña'), 'error');
          return;
        }
        resetForm.reset();
        if (window.history && window.history.replaceState) {
          window.history.replaceState({}, '', '/login.html');
        }
        switchTab('login');
        showAlert('Contraseña actualizada correctamente. Ya podés iniciar sesión.', 'success');
      } catch {
        showAlert('Error de conexión con el servidor', 'error');
      }
    });
  }

  (async function restoreSession() {
    if (!Api.isLoggedIn()) return;
    try {
      const res = await Api.get('/api/patients/me');
      if (res.ok) {
        renderProfile(res.data);
      } else {
        Api.clearToken();
      }
    } catch {
      // Ignorar si el backend no está disponible al cargar
    }
  })();
})();
