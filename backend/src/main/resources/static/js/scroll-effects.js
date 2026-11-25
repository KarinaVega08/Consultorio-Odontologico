/**
 * Animaciones de scroll del frontend (inspiradas en Odentix):
 *   1. El header cambia de estado al superar cierto scroll (barra -> pill flotante).
 *   2. Los bloques marcados con data-reveal aparecen al entrar en viewport.
 * Sin dependencias externas. Degradación graceful si no hay IntersectionObserver.
 */
(function () {
  const UMBRAL_SCROLL = 60;
  const header = document.querySelector('.site-header');

  if (header) {
    function aplicarHeader() {
      header.classList.toggle('is-scrolled', window.scrollY > UMBRAL_SCROLL);
    }

    aplicarHeader();
    window.addEventListener('scroll', aplicarHeader, { passive: true });
  }

  const bloques = document.querySelectorAll('[data-reveal]');
  if (!bloques.length || !('IntersectionObserver' in window)) {
    return;
  }

  bloques.forEach(function (bloque) {
    bloque.classList.add('reveal-pending');
  });

  const observer = new IntersectionObserver(function (entradas) {
    entradas.forEach(function (entrada) {
      if (!entrada.isIntersecting) {
        return;
      }
      entrada.target.classList.add('is-visible');
      observer.unobserve(entrada.target);
    });
  }, { threshold: 0.12, rootMargin: '0px 0px -40px 0px' });

  bloques.forEach(function (bloque) {
    observer.observe(bloque);
  });
})();
