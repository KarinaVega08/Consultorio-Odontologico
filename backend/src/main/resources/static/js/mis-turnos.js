/**
 * Etapa 2 (Mai) — Panel "Mis Turnos":
 *   GET /api/appointments + DELETE /api/appointments/{id}
 * Requiere api.js y catalog.js cargados antes.
 */
(function () {
  if (!Api.isLoggedIn()) {
    window.location.replace('/login.html?redirect=' + encodeURIComponent('/mis-turnos.html'));
    return;
  }

  const list = document.getElementById('turnos-list');
  const emptyState = document.getElementById('empty-state');
  const alertBox = document.getElementById('turnos-alert');

  const nombres = {
    profesionales: {},
    prestaciones: {},
    sillonesBoxes: {}
  };

  function showAlert(message, type) {
    alertBox.textContent = message;
    alertBox.className = 'alert ' + (type || 'info');
  }

  function formatDateTime(iso) {
    const date = new Date(iso);
    return date.toLocaleDateString('es-AR') + ' ' +
      date.toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit' });
  }

  async function cargarNombres() {
    try {
      const catalogo = await Catalog.load();
      (catalogo.profesionales || []).forEach(function (item) {
        nombres.profesionales[item.id] = item.nombre;
      });
      (catalogo.prestaciones || []).forEach(function (item) {
        nombres.prestaciones[item.id] = item.nombre;
      });
      (catalogo.sillonesBoxes || []).forEach(function (item) {
        nombres.sillonesBoxes[item.id] = item.nombre;
      });
    } catch (error) {
      // El listado sigue funcionando aunque no haya catálogo disponible.
    }
  }

  function celdaConDetalle(principal, detalle) {
    const td = document.createElement('td');
    td.textContent = principal;
    if (detalle) {
      const small = document.createElement('small');
      small.textContent = detalle;
      td.appendChild(small);
    }
    return td;
  }

  function render(turnos) {
    list.textContent = '';
    emptyState.classList.toggle('hidden', turnos.length > 0);

    turnos
      .slice()
      .sort(function (a, b) {
        return new Date(a.fechaHoraInicio) - new Date(b.fechaHoraInicio);
      })
      .forEach(function (turno) {
        const fila = document.createElement('tr');
        fila.appendChild(celdaConDetalle(
          formatDateTime(turno.fechaHoraInicio),
          nombres.profesionales[turno.profesionalId] || ''
        ));
        fila.appendChild(celdaConDetalle(
          formatDateTime(turno.fechaHoraFin),
          (nombres.prestaciones[turno.prestacionId] || '') + ' · ' +
            (nombres.sillonesBoxes[turno.sillonBoxId] || '')
        ));

        const tdEstado = document.createElement('td');
        const estado = String(turno.estado || '').toLowerCase();
        const badge = document.createElement('span');
        badge.className = 'badge ' + estado;
        badge.textContent = turno.estado;
        tdEstado.appendChild(badge);
        fila.appendChild(tdEstado);

        const tdAcciones = document.createElement('td');
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'btn btn-secondary';
        if (estado === 'cancelado' || estado === 'completado') {
          btn.textContent = 'Sin acciones';
          btn.disabled = true;
        } else {
          btn.textContent = 'Cancelar';
          btn.addEventListener('click', function () {
            cancelar(turno.id);
          });
        }
        tdAcciones.appendChild(btn);
        fila.appendChild(tdAcciones);

        list.appendChild(fila);
      });
  }

  async function cargarTurnos() {
    const res = await Api.get('/api/appointments');
    if (!res.ok) {
      if (res.status === 401 || res.status === 403) {
        Api.clearToken();
        window.location.replace('/login.html?redirect=' + encodeURIComponent('/mis-turnos.html'));
        return;
      }
      throw new Error((res.data && res.data.message) || 'No se pudo cargar tus turnos');
    }
    render(res.data || []);
  }

  async function cancelar(id) {
    if (!window.confirm('¿Confirmás la cancelación de este turno? El horario volverá a estar disponible.')) {
      return;
    }
    try {
      const res = await Api.delete('/api/appointments/' + id);
      if (!res.ok) {
        showAlert((res.data && res.data.message) || 'No se pudo cancelar el turno', 'error');
        return;
      }
      showAlert('Turno cancelado. El horario ya está libre.', 'success');
      await cargarTurnos();
    } catch (error) {
      showAlert('Error de conexión con el backend', 'error');
    }
  }

  (async function init() {
    await cargarNombres();
    try {
      await cargarTurnos();
    } catch (error) {
      showAlert(error.message, 'error');
    }
  })();
})();
