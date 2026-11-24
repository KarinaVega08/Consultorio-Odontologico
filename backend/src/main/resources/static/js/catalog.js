/**
 * Carga del catálogo público (/api/catalog) para los combos de reserva.
 * Requiere que api.js esté cargado antes.
 */
(function () {
  let cache = null;

  async function load() {
    if (cache) {
      return cache;
    }
    const res = await Api.get('/api/catalog');
    if (!res.ok) {
      throw new Error('No se pudo cargar el catálogo del consultorio');
    }
    cache = res.data;
    return cache;
  }

  function fillSelect(select, items, placeholder) {
    if (!select) {
      return;
    }
    select.textContent = '';
    const empty = document.createElement('option');
    empty.value = '';
    empty.textContent = placeholder || 'Seleccioná una opción';
    select.appendChild(empty);

    (items || []).forEach(function (item) {
      const option = document.createElement('option');
      option.value = item.id;
      option.textContent = item.duracionMinutos
        ? item.nombre + ' (' + item.duracionMinutos + ' min)'
        : item.nombre;
      select.appendChild(option);
    });
  }

  window.Catalog = {
    load: load,
    fillSelect: fillSelect
  };
})();
