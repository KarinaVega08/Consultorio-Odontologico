(function () {
  const alertBox = document.getElementById('alert-box');
  const authSection = document.getElementById('auth-section');
  const profileSection = document.getElementById('profile-section');
  const tabLogin = document.getElementById('tab-login');
  const tabRegister = document.getElementById('tab-register');
  const loginForm = document.getElementById('login-form');
  const registerForm = document.getElementById('register-form');
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

  function switchTab(mode) {
    clearAlert();
    const isLogin = mode === 'login';
    tabLogin.classList.toggle('active', isLogin);
    tabRegister.classList.toggle('active', !isLogin);
    loginForm.classList.toggle('hidden', !isLogin);
    registerForm.classList.toggle('hidden', isLogin);
  }

  function renderProfile(patient) {
    authSection.classList.add('hidden');
    profileSection.classList.remove('hidden');
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
      loginForm.reset();
      renderProfile(res.data.patient);
      showAlert('Sesión iniciada correctamente.', 'success');
    } catch {
      showAlert('Error de conexión con el servidor', 'error');
    }
  });

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
    } catch {
      showAlert('Error de conexión con el servidor', 'error');
    }
  });

  logoutBtn.addEventListener('click', () => {
    Api.clearToken();
    profileSection.classList.add('hidden');
    authSection.classList.remove('hidden');
    switchTab('login');
  });

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
