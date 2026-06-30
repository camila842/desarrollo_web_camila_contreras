// --- Helpers anti-XSS ---

function escaparHtml(texto) {
  return texto
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function escaparRegExp(texto) {
  return texto.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

// Escapa el texto primero, luego envuelve coincidencias en <mark>.
// Nunca inserta datos del usuario/BD en innerHTML sin escapar.
function destacarCoincidencia(texto, query) {
  if (!texto) return "";
  const textoEscapado = escaparHtml(texto);
  const queryEscapado = escaparRegExp(escaparHtml(query));
  if (!queryEscapado) return textoEscapado;
  return textoEscapado.replace(new RegExp(`(${queryEscapado})`, "gi"), "<mark>$1</mark>");
}

// --- Formato nota ---

function formatearPromedio(promedio) {
  if (promedio === null || promedio === undefined) return "-";
  return Number(promedio).toFixed(1);
}

function textoEvaluaciones(cantidad) {
  return cantidad === 1 ? "1 evaluación" : `${cantidad} evaluaciones`;
}

// --- Panel de evaluación (radio buttons 1-7, sin input libre) ---

function crearPanelEvaluar(actividadId) {
  const panel = document.createElement("div");
  panel.className = "evaluar-panel";
  panel.style.display = "none";

  const etiqueta = document.createElement("span");
  etiqueta.textContent = "Selecciona una nota: ";
  panel.appendChild(etiqueta);

  for (let valor = 1; valor <= 7; valor++) {
    const label = document.createElement("label");
    label.style.marginRight = "6px";

    const radio = document.createElement("input");
    radio.type = "radio";
    radio.name = `nota-${actividadId}`;
    radio.value = valor;

    label.appendChild(radio);
    label.appendChild(document.createTextNode(` ${valor}`));
    panel.appendChild(label);
  }

  panel.appendChild(document.createElement("br"));

  const errorSpan = document.createElement("span");
  errorSpan.className = "error";
  panel.appendChild(errorSpan);

  const botonConfirmar = document.createElement("button");
  botonConfirmar.textContent = "Confirmar";
  botonConfirmar.className = "link-button";
  botonConfirmar.style.marginTop = "6px";
  panel.appendChild(botonConfirmar);

  return panel;
}

// --- Envío de nota ---

async function enviarNota(actividadId, nota, card) {
  try {
    const response = await fetch(`/api/activities/${actividadId}/notas`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ nota: nota })
    });

    const data = await response.json();

    if (response.status === 401) {
      mostrarErrorEvaluar(card, "Debes iniciar sesión para evaluar.");
      return;
    }

    if (response.status === 409 || Boolean(data.yaEvaluo)) {
      // Actualizar la sección de evaluación a "ya evaluaste"
      reemplazarConMensajeYaEvaluo(card);
      return;
    }

    if (!data.ok) {
      const panel = card.querySelector(".evaluar-panel");
      const errorSpan = panel ? panel.querySelector(".error") : null;
      if (errorSpan) {
        errorSpan.textContent = (data.errores && data.errores[0]) || "Error al guardar la nota.";
        errorSpan.className = "error visible";
      }
      return;
    }

    // Actualizar nota promedio y cantidad
    const notaValor = card.querySelector(".nota-valor");
    const notaCantidad = card.querySelector(".nota-cantidad");
    if (notaValor) notaValor.textContent = formatearPromedio(data.promedio);
    if (notaCantidad) notaCantidad.textContent = textoEvaluaciones(data.cantidadNotas);

    // Reemplazar la sección de evaluación por "Ya evaluaste esta actividad"
    reemplazarConMensajeYaEvaluo(card);

  } catch (err) {
    console.error("Error al enviar nota:", err);
    mostrarErrorEvaluar(card, "Ocurrió un error al guardar la nota.");
  }
}

function reemplazarConMensajeYaEvaluo(card) {
  const evaluarSeccion = card.querySelector(".evaluar-seccion");
  if (!evaluarSeccion) return;
  evaluarSeccion.innerHTML = "";
  const msg = document.createElement("p");
  msg.className = "ya-evaluo";
  msg.textContent = "Ya evaluaste esta actividad.";
  evaluarSeccion.appendChild(msg);
}

function mostrarErrorEvaluar(card, texto) {
  const panel = card.querySelector(".evaluar-panel");
  const errorSpan = panel ? panel.querySelector(".error") : null;
  if (errorSpan) {
    errorSpan.textContent = texto;
    errorSpan.className = "error visible";
  }
}

// --- Sección de evaluación por tarjeta ---

function crearSeccionEvaluar(actividad) {
  const seccion = document.createElement("div");
  seccion.className = "evaluar-seccion";

  if (actividad.puedeEvaluar) {
    // Usuario logueado y aún no evaluó: mostrar botón + panel
    const botonEvaluar = document.createElement("button");
    botonEvaluar.textContent = "Evaluar";
    botonEvaluar.className = "link-button";
    seccion.appendChild(botonEvaluar);

    const panel = crearPanelEvaluar(actividad.actividadId);
    seccion.appendChild(panel);

    botonEvaluar.addEventListener("click", () => {
      panel.style.display = panel.style.display === "none" ? "block" : "none";
    });

    const botonConfirmar = panel.querySelector("button");
    const errorSpan = panel.querySelector(".error");

    botonConfirmar.addEventListener("click", async () => {
      const seleccionado = panel.querySelector(
        `input[name="nota-${actividad.actividadId}"]:checked`
      );

      if (!seleccionado) {
        errorSpan.textContent = "Selecciona una nota entre 1 y 7.";
        errorSpan.className = "error visible";
        return;
      }

      const nota = parseInt(seleccionado.value, 10);

      if (!Number.isInteger(nota) || nota < 1 || nota > 7) {
        errorSpan.textContent = "La nota debe ser un número entero entre 1 y 7.";
        errorSpan.className = "error visible";
        return;
      }

      errorSpan.className = "error";
      // La card es el .form-results que contiene esta sección
      await enviarNota(actividad.actividadId, nota, seccion.closest(".form-results"));
    });

  } else if (actividad.yaEvaluo) {
    // Usuario logueado pero ya evaluó
    const msg = document.createElement("p");
    msg.className = "ya-evaluo";
    msg.textContent = "Ya evaluaste esta actividad.";
    seccion.appendChild(msg);

  } else {
    // Usuario no autenticado
    const enlace = document.createElement("a");
    enlace.href = "/login";
    enlace.textContent = "Inicia sesión para evaluar";
    seccion.appendChild(enlace);
  }

  return seccion;
}

// --- Tarjeta de resultado ---

function crearResultadoNode(actividad, query) {
  const div = document.createElement("div");
  div.className = "form-results";
  div.dataset.actividadId = actividad.actividadId;

  const nombre = document.createElement("p");
  nombre.className = "nombre";
  nombre.innerHTML = destacarCoincidencia(actividad.nombre, query);
  div.appendChild(nombre);

  const tipo = document.createElement("p");
  tipo.className = "tipo";
  tipo.textContent = actividad.tipo;
  div.appendChild(tipo);

  const miembro = document.createElement("p");
  const miembroStrong = document.createElement("strong");
  miembroStrong.textContent = "Miembro: ";
  miembro.appendChild(miembroStrong);
  miembro.appendChild(document.createTextNode(actividad.miembro));
  div.appendChild(miembro);

  const dia = document.createElement("p");
  const diaStrong = document.createElement("strong");
  diaStrong.textContent = "Día: ";
  dia.appendChild(diaStrong);
  dia.appendChild(document.createTextNode(actividad.dia));
  div.appendChild(dia);

  const comuna = document.createElement("p");
  const comunaStrong = document.createElement("strong");
  comunaStrong.textContent = "Comuna: ";
  comuna.appendChild(comunaStrong);
  const comunaSpan = document.createElement("span");
  comunaSpan.innerHTML = destacarCoincidencia(actividad.comuna, query);
  comuna.appendChild(comunaSpan);
  div.appendChild(comuna);

  const descripcion = document.createElement("p");
  const descripcionStrong = document.createElement("strong");
  descripcionStrong.textContent = "Descripción: ";
  descripcion.appendChild(descripcionStrong);
  const descripcionSpan = document.createElement("span");
  descripcionSpan.innerHTML = destacarCoincidencia(actividad.descripcion, query);
  descripcion.appendChild(descripcionSpan);
  div.appendChild(descripcion);

  // Nota promedio + cantidad de evaluaciones
  const notaDisplay = document.createElement("p");
  const notaStrong = document.createElement("strong");
  notaStrong.textContent = "Nota promedio: ";
  notaDisplay.appendChild(notaStrong);

  const notaValor = document.createElement("span");
  notaValor.className = "nota-valor";
  notaValor.textContent = formatearPromedio(actividad.promedio);
  notaDisplay.appendChild(notaValor);

  notaDisplay.appendChild(document.createTextNode(" "));

  const notaCantidad = document.createElement("span");
  notaCantidad.className = "nota-cantidad";
  notaCantidad.style.color = "#666";
  notaCantidad.style.fontSize = "0.9em";
  notaCantidad.textContent = textoEvaluaciones(actividad.cantidadNotas || 0);
  notaDisplay.appendChild(notaCantidad);

  div.appendChild(notaDisplay);

  // Sección de evaluación (evaluar / ya evaluaste / inicia sesión)
  div.appendChild(crearSeccionEvaluar(actividad));

  // Enlace al detalle completo de la actividad
  const verDetalle = document.createElement("a");
  verDetalle.href = `/activities/${actividad.actividadId}`;
  verDetalle.className = "link-button";
  verDetalle.textContent = "Ver detalle";
  verDetalle.style.marginTop = "8px";
  div.appendChild(verDetalle);

  return div;
}

// --- Búsqueda ---

async function buscarActividades(query) {
  const contenedor = document.getElementById("buscar-resultados");

  try {
    const response = await fetch(
      `/api/actividades/buscar?q=${encodeURIComponent(query)}`
    );
    const data = await response.json();

    contenedor.innerHTML = "";

    if (!data.ok) {
      contenedor.textContent = "No se pudo realizar la búsqueda.";
      return;
    }

    if (data.resultados.length === 0) {
      contenedor.textContent = "No se encontraron actividades para tu búsqueda.";
      return;
    }

    data.resultados.forEach(actividad => {
      contenedor.appendChild(crearResultadoNode(actividad, query));
    });

  } catch (err) {
    console.error("Error al buscar actividades:", err);
    contenedor.textContent = "Ocurrió un error al buscar actividades.";
  }
}

function debounce(fn, delay) {
  let timeoutId = null;
  return (...args) => {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => fn(...args), delay);
  };
}

// --- Inicialización ---

document.addEventListener("DOMContentLoaded", function () {
  const input = document.getElementById("buscar-texto");
  if (!input) return;

  const contenedor = document.getElementById("buscar-resultados");
  const errorBuscar = document.getElementById("error-buscar");
  const buscarDebounced = debounce(buscarActividades, 300);

  input.addEventListener("input", () => {
    const query = input.value.trim();

    if (query.length < 3) {
      errorBuscar.className = query.length === 0 ? "error" : "error visible";
      contenedor.innerHTML = "";
      return;
    }

    errorBuscar.className = "error";
    buscarDebounced(query);
  });
});
