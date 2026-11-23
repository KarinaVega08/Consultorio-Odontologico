/**
 * Navegación común (C-07 frontend-web, Etapa 1): estado de cuenta en el header.
 * Requiere js/api.js cargado antes.
 */
(function () {
  function aplicar() {
    const account = document.getElementById('nav-account');
    const logoutBtn = document.getElementById('nav-logout');
    if (!account) return;

    if (window.Api && Api.isLoggedIn()) {
      account.textContent = 'Mi Cuenta';
      account.href = '/login.html';
      if (logoutBtn) {
        logoutBtn.classList.remove('hidden');
        logoutBtn.onclick = function () {
          Api.clearToken();
          window.location.href = '/index.html';
        };
      }
    } else {
      account.textContent = 'Iniciar sesión';
      account.href = '/login.html';
      if (logoutBtn) {
        logoutBtn.classList.add('hidden');
        logoutBtn.onclick = null;
      }
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', aplicar);
  } else {
    aplicar();
  }
})();
