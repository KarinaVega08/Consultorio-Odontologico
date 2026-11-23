(function () {
  const TOKEN_KEY = 'consultorio_auth_token';

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
      const res = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      const data = await res.json().catch(() => null);
      if (!res.ok) {
        showAlert(extractErrorMessage(data, 'No se pudo completar el registro'), 'error');
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
      const res = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
      const data = await res.json().catch(() => null);
      if (!res.ok) {
        showAlert(extractErrorMessage(data, 'DNI o contraseña inválidos'), 'error');
        return;
      }
      localStorage.setItem(TOKEN_KEY, data.token);
      loginForm.reset();
      renderProfile(data.patient);
      showAlert('Sesión iniciada correctamente.', 'success');
    } catch {
      showAlert('Error de conexión con el servidor', 'error');
    }
  });

  gmailForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearAlert();
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) {
      showAlert('Sesión expirada. Volvé a iniciar sesión.', 'error');
      return;
    }
    const email = document.getElementById('gmail-input').value.trim();
    try {
      const res = await fetch('/api/patients/me/recovery-email', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': 'Bearer ' + token
        },
        body: JSON.stringify({ email })
      });
      const data = await res.json().catch(() => null);
      if (!res.ok) {
        showAlert(extractErrorMessage(data, 'No se pudo vincular el Gmail'), 'error');
        return;
      }
      gmailForm.reset();
      renderProfile(data);
      showAlert('Gmail de recuperación vinculado correctamente.', 'success');
    } catch {
      showAlert('Error de conexión con el servidor', 'error');
    }
  });

  logoutBtn.addEventListener('click', () => {
    localStorage.removeItem(TOKEN_KEY);
    profileSection.classList.add('hidden');
    authSection.classList.remove('hidden');
    switchTab('login');
  });

  (async function restoreSession() {
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) return;
    try {
      const res = await fetch('/api/patients/me', {
        headers: { 'Authorization': 'Bearer ' + token }
      });
      if (res.ok) {
        const patient = await res.json();
        renderProfile(patient);
      } else {
        localStorage.removeItem(TOKEN_KEY);
      }
    } catch {
      // Ignorar si el backend no está disponible al cargar
    }
  })();
})();
