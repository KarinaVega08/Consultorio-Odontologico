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

  const modal = document.getElementById('turno-modal');
  const modalIcon = document.getElementById('modal-icon');
  const modalTitle = document.getElementById('modal-title');
  const modalSubtitle = document.getElementById('modal-subtitle');
  const modalDetails = document.getElementById('modal-details');
  const modalCancel = document.getElementById('modal-cancel');
  const modalConfirm = document.getElementById('modal-confirm');
  let modalConfig = null;

  const nombres = {
    profesionales: {},
    prestaciones: {},
    sillonesBoxes: {}
  };

  function showAlert(message, type) {
    alertBox.textContent = message;
    alertBox.className = 'alert ' + (type || 'info');
  }

  function formatHour(iso) {
    return new Date(iso).toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit', hour12: false });
  }

  function formatDateTime(iso) {
    const date = new Date(iso);
    return date.toLocaleDateString('es-AR') + ' ' +
      date.toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit', hour12: false });
  }

  function abrirModal(config) {
    modalConfig = config;
    modalIcon.textContent = config.icon || '✦';
    modalIcon.className = 'modal-icon ' + (config.tone || 'info');
    modalTitle.textContent = config.title;
    modalSubtitle.textContent = config.subtitle || '';
    modalDetails.textContent = '';
    (config.detalles || []).forEach(function (item) {
      const dt = document.createElement('dt');
      dt.textContent = item.label;
      const dd = document.createElement('dd');
      dd.textContent = item.value;
      modalDetails.appendChild(dt);
      modalDetails.appendChild(dd);
    });
    modalCancel.textContent = config.cancelText || 'Volver';
    modalConfirm.textContent = config.confirmText || 'Confirmar';
    modal.classList.remove('hidden');
    document.body.classList.add('modal-open');
    modalConfirm.focus();
  }

  function cerrarModal(confirmado) {
    if (modal.classList.contains('hidden')) {
      return;
    }
    modal.classList.add('hidden');
    document.body.classList.remove('modal-open');
    const config = modalConfig;
    modalConfig = null;
    if (confirmado && config && config.onConfirm) {
      config.onConfirm();
    } else if (!confirmado && config && config.onCancel) {
      config.onCancel();
    }
  }

  modalCancel.addEventListener('click', function () { cerrarModal(false); });
  modalConfirm.addEventListener('click', function () { cerrarModal(true); });
  modal.querySelector('.modal-backdrop').addEventListener('click', function () { cerrarModal(false); });
  document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape') {
      cerrarModal(false);
    }
  });

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
            cancelar(turno);
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

  async function cancelar(turno) {
    const acepta = await new Promise(function (resolve) {
      abrirModal({
        icon: '✦',
        tone: 'info',
        title: '¿Cancelar el turno?',
        subtitle: 'El horario volverá a estar disponible para otras reservas.',
        detalles: [
          { label: 'Fecha y hora', value: formatDateTime(turno.fechaHoraInicio) + ' a ' + formatHour(turno.fechaHoraFin) },
          { label: 'Profesional', value: nombres.profesionales[turno.profesionalId] || '' },
          { label: 'Prestación', value: nombres.prestaciones[turno.prestacionId] || '' },
          { label: 'Sillón', value: nombres.sillonesBoxes[turno.sillonBoxId] || '' }
        ],
        cancelText: 'Volver',
        confirmText: 'Sí, cancelar turno',
        onConfirm: function () { resolve(true); },
        onCancel: function () { resolve(false); }
      });
    });
    if (!acepta) {
      return;
    }
    try {
      const res = await Api.delete('/api/appointments/' + turno.id);
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
