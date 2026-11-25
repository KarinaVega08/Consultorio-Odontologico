/**
 * Módulo HTTP del frontend (C-07 frontend-web, Etapa 1).
 * Centraliza el token de sesión en localStorage y las peticiones autenticadas.
 * La lógica de negocio (disponibilidad, reservas, turnos) es Etapa 2 (Mailén Claros).
 */
(function () {
  const TOKEN_KEY = 'consultorio_auth_token';

  function getToken() {
    return localStorage.getItem(TOKEN_KEY);
  }

  function setToken(token) {
    localStorage.setItem(TOKEN_KEY, token);
  }

  function clearToken() {
    localStorage.removeItem(TOKEN_KEY);
  }

  function isLoggedIn() {
    return !!getToken();
  }

  /**
   * Petición genérica. Devuelve {ok, status, data} con el JSON parseado (o null).
   * Si hay token guardado, agrega el encabezado Authorization: Bearer <token>.
   */
  async function request(method, path, body) {
    const headers = {};
    const token = getToken();
    if (token) {
      headers['Authorization'] = 'Bearer ' + token;
    }
    if (body !== undefined && body !== null) {
      headers['Content-Type'] = 'application/json';
    }
    const response = await fetch(path, {
      method: method,
      headers: headers,
      body: body !== undefined && body !== null ? JSON.stringify(body) : undefined
    });
    let data = null;
    try {
      data = await response.json();
    } catch (e) {
      // Respuesta sin cuerpo JSON (por ejemplo 204 o HTML)
    }
    return { ok: response.ok, status: response.status, data: data };
  }

  const get = (path) => request('GET', path);
  const post = (path, body) => request('POST', path, body);
  const put = (path, body) => request('PUT', path, body);
  const del = (path, body) => request('DELETE', path, body);

  window.Api = {
    TOKEN_KEY: TOKEN_KEY,
    getToken: getToken,
    setToken: setToken,
    clearToken: clearToken,
    isLoggedIn: isLoggedIn,
    request: request,
    get: get,
    post: post,
    put: put,
    delete: del
  };
})();
