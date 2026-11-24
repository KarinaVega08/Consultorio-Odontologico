/**
 * Etapa 2 (Mai) — Lógica de reserva:
 *   1. carga profesionales / prestaciones / sillones desde /api/catalog
 *   2. consulta franjas libres con GET /api/availability
 *   3. confirma con POST /api/appointments
 * Requiere api.js y catalog.js cargados antes.
 */
(function () {
  const form = document.getElementById('reserva-form');
  const profesionalSel = document.getElementById('profesional');
  const sillonSel = document.getElementById('sillon');
  const prestacionSel = document.getElementById('prestacion');
  const fechaInput = document.getElementById('fecha');
  const buscarBtn = document.getElementById('buscar-franjas');
  const franjasBox = document.getElementById('franjas');
  const alertBox = document.getElementById('reserva-alert');

  const modal = document.getElementById('turno-modal');
  const modalIcon = document.getElementById('modal-icon');
  const modalTitle = document.getElementById('modal-title');
  const modalSubtitle = document.getElementById('modal-subtitle');
  const modalDetails = document.getElementById('modal-details');
  const modalCancel = document.getElementById('modal-cancel');
  const modalConfirm = document.getElementById('modal-confirm');
  let modalConfig = null;

  function showAlert(message, type) {
    alertBox.textContent = message;
    alertBox.className = 'alert ' + (type || 'info');
  }

  function clearAlert() {
    alertBox.textContent = '';
    alertBox.className = 'alert';
  }

  function todayISO() {
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    return now.toISOString().slice(0, 10);
  }

  function formatHour(iso) {
    const date = new Date(iso);
    return date.toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit' });
  }

  function formatDateTime(iso) {
    const date = new Date(iso);
    return date.toLocaleDateString('es-AR') + ' ' + formatHour(iso);
  }

  function selectedLabel(select) {
    if (!select || select.selectedIndex <= 0) {
      return '';
    }
    return select.options[select.selectedIndex].text;
  }

  function detalleTurno(inicio, finTexto, nombrePaciente) {
    const cuando = formatDateTime(inicio) + (finTexto ? ' a ' + finTexto : '');
    return [
      { label: 'Paciente', value: nombrePaciente || '' },
      { label: 'Profesional', value: selectedLabel(profesionalSel) },
      { label: 'Prestación', value: selectedLabel(prestacionSel) },
      { label: 'Sillón', value: selectedLabel(sillonSel) },
      { label: 'Fecha y hora', value: cuando }
    ];
  }

  function horaFinPrevista(inicio, minutos) {
    const fin = new Date(inicio);
    fin.setMinutes(fin.getMinutes() + minutos);
    return formatHour(fin);
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
    modalCancel.textContent = config.cancelText || 'Cancelar';
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

  function pedirConfirmacion(detalles) {
    return new Promise(function (resolve) {
      abrirModal({
        icon: '✦',
        tone: 'info',
        title: '¿Confirmar el turno?',
        subtitle: 'Revisá los datos antes de reservar.',
        detalles: detalles,
        cancelText: 'Volver',
        confirmText: 'Sí, reservar turno',
        onConfirm: function () { resolve(true); },
        onCancel: function () { resolve(false); }
      });
    });
  }

  function errorMessage(data, fallback) {
    if (data && data.fieldErrors) {
      return Object.values(data.fieldErrors).join('. ');
    }
    return (data && data.message) ? data.message : fallback;
  }

  function setLoading(loading) {
    buscarBtn.disabled = loading;
    buscarBtn.textContent = loading ? 'Buscando...' : 'Buscar horarios disponibles';
  }

  modalCancel.addEventListener('click', function () { cerrarModal(false); });
  modalConfirm.addEventListener('click', function () { cerrarModal(true); });
  modal.querySelector('.modal-backdrop').addEventListener('click', function () { cerrarModal(false); });
  document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape') {
      cerrarModal(false);
    }
  });

  async function init() {
    fechaInput.min = todayISO();
    if (!fechaInput.value) {
      fechaInput.value = fechaInput.min;
    }

    try {
      const catalogo = await Catalog.load();
      Catalog.fillSelect(profesionalSel, catalogo.profesionales, 'Seleccioná un profesional');
      Catalog.fillSelect(sillonSel, catalogo.sillonesBoxes, 'Seleccioná un sillón/box');
      Catalog.fillSelect(prestacionSel, catalogo.prestaciones, 'Seleccioná una prestación');

      // La reserva necesita un sillón concreto: preseleccionamos el primero.
      if (catalogo.sillonesBoxes && catalogo.sillonesBoxes.length > 0) {
        sillonSel.selectedIndex = 1;
      }

      if (!catalogo.profesionales || !catalogo.profesionales.length ||
          !catalogo.prestaciones || !catalogo.prestaciones.length) {
        showAlert('No hay profesionales o prestaciones cargados. Ejecutá el script de datos demo (perfil demo).', 'error');
      }
    } catch (error) {
      showAlert('No se pudo cargar el catálogo. Verificá que el backend esté en http://localhost:8080.', 'error');
    }
  }

  async function buscarFranjas() {
    if (!form.reportValidity()) {
      return;
    }
    if (!sillonSel.value) {
      showAlert('Seleccioná un sillón/box para poder reservar.', 'error');
      return;
    }

    setLoading(true);
    try {
      const params = new URLSearchParams({
        profesionalId: profesionalSel.value,
        fecha: fechaInput.value,
        prestacionId: prestacionSel.value,
        sillonBoxId: sillonSel.value
      });

      const res = await Api.get('/api/availability?' + params.toString());
      franjasBox.textContent = '';

      if (!res.ok) {
        showAlert(errorMessage(res.data, 'No se pudo consultar la disponibilidad'), 'error');
        return;
      }

      const franjas = (res.data && res.data.franjas) || [];
      if (franjas.length === 0) {
        const empty = document.createElement('div');
        empty.className = 'empty-state';
        const strong = document.createElement('strong');
        strong.textContent = 'No hay horarios libres para esa combinación';
        empty.appendChild(strong);
        empty.appendChild(document.createTextNode('Probá con otra fecha, profesional o prestación.'));
        franjasBox.appendChild(empty);
        return;
      }

      const grid = document.createElement('div');
      grid.className = 'slot-grid';
      franjas.forEach(function (slot) {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'slot';
        btn.textContent = formatHour(slot.inicio) + ' – ' + formatHour(slot.fin);
        btn.setAttribute('data-inicio', slot.inicio);
        btn.addEventListener('click', function () {
          reservar(slot.inicio, btn);
        });
        grid.appendChild(btn);
      });
      franjasBox.appendChild(grid);
    } catch (error) {
      showAlert('Error de conexión con el backend', 'error');
    } finally {
      setLoading(false);
    }
  }

  async function reservar(inicio, btn) {
    clearAlert();

    if (!Api.isLoggedIn()) {
      showAlert('Iniciá sesión para confirmar el turno. Te redirigimos al login.', 'info');
      window.setTimeout(function () {
        window.location.href = '/login.html?redirect=' + encodeURIComponent('/reservar.html');
      }, 900);
      return;
    }
    if (!sillonSel.value) {
      showAlert('Seleccioná un sillón/box antes de reservar.', 'error');
      return;
    }

    btn.disabled = true;
    try {
      const me = await Api.get('/api/patients/me');
      if (!me.ok) {
        Api.clearToken();
        showAlert('Tu sesión venció. Volvé a iniciar sesión.', 'error');
        return;
      }

      let finPrevisto = '';
      try {
        const catalogo = await Catalog.load();
        const prestacion = (catalogo.prestaciones || []).find(function (p) {
          return p.id === prestacionSel.value;
        });
        if (prestacion && prestacion.duracionMinutos) {
          finPrevisto = horaFinPrevista(inicio, prestacion.duracionMinutos);
        }
      } catch (error) {
        // Sin catálogo no se puede estimar el fin; el modal muestra solo el inicio.
      }

      const confirma = await pedirConfirmacion(
        detalleTurno(inicio, finPrevisto, me.data.nombreCompleto)
      );
      if (!confirma) {
        showAlert('Reserva cancelada: no se creó ningún turno.', 'info');
        return;
      }

      const payload = {
        pacienteId: me.data.id,
        profesionalId: profesionalSel.value,
        sillonBoxId: sillonSel.value,
        prestacionId: prestacionSel.value,
        fechaHoraInicio: inicio
      };

      const res = await Api.post('/api/appointments', payload);
      if (!res.ok) {
        showAlert(errorMessage(res.data, 'No se pudo reservar el turno'), 'error');
        return;
      }

      abrirModal({
        icon: '✓',
        tone: 'success',
        title: '¡Turno reservado con éxito!',
        subtitle: 'Ya podés verlo en "Mis Turnos".',
        detalles: detalleTurno(res.data.fechaHoraInicio, formatHour(res.data.fechaHoraFin), me.data.nombreCompleto),
        cancelText: 'Seguir reservando',
        confirmText: 'Ver mis turnos',
        onConfirm: function () { window.location.href = '/mis-turnos.html'; }
      });
      showAlert('Turno reservado para el ' + formatDateTime(inicio) + '. Ya podés verlo en "Mis Turnos".', 'success');
      await buscarFranjas();
    } catch (error) {
      showAlert('Error de conexión con el backend', 'error');
    } finally {
      btn.disabled = false;
    }
  }

  buscarBtn.addEventListener('click', function () {
    clearAlert();
    buscarFranjas();
  });

  init();
})();
