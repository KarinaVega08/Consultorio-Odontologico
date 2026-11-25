/**
 * Intro del home: overlay con el nombre "Open Mouth" letra por letra.
 * Replica la animación de entrada de Odentix sin dependencias externas y
 * se muestra solo una vez por sesión (sessionStorage).
 * Se salta con ?nointro o si el usuario prefiere movimiento reducido.
 */
(function () {
  const CLAVE_SESION = 'openmouth_intro_visto';
  const TEXTO = 'Open Mouth';

  function leerFlag() {
    try {
      return sessionStorage.getItem(CLAVE_SESION) === '1';
    } catch (error) {
      return false;
    }
  }

  function marcarFlag() {
    try {
      sessionStorage.setItem(CLAVE_SESION, '1');
    } catch (error) {
      // Sin sessionStorage la intro puede repetirse; no es un error funcional.
    }
  }

  if (leerFlag() || new URLSearchParams(window.location.search).has('nointro')) {
    marcarFlag();
    return;
  }

  if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    marcarFlag();
    return;
  }

  if (!document.body) {
    return;
  }

  // Se marca antes de empezar: si recargan a mitad de animación, no se repite.
  marcarFlag();

  const overlay = document.createElement('div');
  overlay.className = 'intro-overlay';
  overlay.setAttribute('aria-hidden', 'true');

  const palabra = document.createElement('div');
  palabra.className = 'intro-word';

  Array.from(TEXTO).forEach(function (letra, indice) {
    const span = document.createElement('span');
    span.className = 'intro-letter';
    span.textContent = letra === ' ' ? '\u00A0' : letra;
    span.style.setProperty('--in-delay', (indice * 0.05).toFixed(2) + 's');
    span.style.setProperty('--out-delay', (1.7 + indice * 0.03).toFixed(2) + 's');
    palabra.appendChild(span);
  });

  overlay.appendChild(palabra);
  document.body.appendChild(overlay);

  const overflowOriginal = document.body.style.overflow;
  document.body.style.overflow = 'hidden';
  overlay.classList.add('is-leaving');

  function finalizar() {
    if (!overlay.parentNode) {
      return;
    }
    overlay.remove();
    document.body.style.overflow = overflowOriginal;
  }

  overlay.addEventListener('animationend', function (event) {
    if (event.animationName === 'intro-overlay-out') {
      finalizar();
    }
  });

  // Respaldo por si animationend no se dispara (pestaña en segundo plano).
  window.setTimeout(finalizar, 3400);
})();
