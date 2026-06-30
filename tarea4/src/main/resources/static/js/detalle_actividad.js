// const PALABRAS_PROHIBIDAS = [
//   "holi",
//   "que tal",
//   "weeena"
// ];

async function cargarPalabrasProhibidas() {
  const response = await fetch("/api/palabras-prohibidas");
  const data = await response.json();

  if (!data.ok) {
    return [];
  }

  return data.palabras;
}

async function contienePalabraProhibida(texto) {
  const textoNormalizado = texto.toLowerCase();
  const palabrasProhibidas = await cargarPalabrasProhibidas()
  console.log(palabrasProhibidas);
  return palabrasProhibidas.some(palabra => {
    return textoNormalizado.includes(palabra.toLowerCase());
  });
}


async function cargarComentarios() {
  try {
    const form = document.getElementById("comentario-form");
    const actividadId = form.dataset.actividadId;

    const response = await fetch(`/api/activities/${actividadId}/comentarios`);
    const data = await response.json();

    const contenedor = document.getElementById("comentarios-listado");

    if (!data.ok) {
      contenedor.textContent = "No se pudieron cargar los comentarios.";
      return;
    }

    if (data.comentarios.length === 0) {
      contenedor.textContent = "No hay comentarios para esta actividad.";
      return;
    }

    contenedor.innerHTML = "";

    data.comentarios.forEach(comentario => {
      const div = document.createElement("div");
      div.classList.add("comentario");

      const fecha = document.createElement("p");
      const fechaStrong = document.createElement("strong");
      fechaStrong.textContent = "Fecha: ";
      fecha.appendChild(fechaStrong);
      fecha.appendChild(document.createTextNode(comentario.fecha));

      const nombre = document.createElement("p");
      const nombreStrong = document.createElement("strong");
      nombreStrong.textContent = "Nombre: ";
      nombre.appendChild(nombreStrong);
      nombre.appendChild(document.createTextNode(comentario.nombre));

      const texto = document.createElement("p");
      const textoStrong = document.createElement("strong");
      textoStrong.textContent = "Comentario: ";
      texto.appendChild(textoStrong);
      texto.appendChild(document.createTextNode(comentario.texto));

      const hr = document.createElement("hr");

      div.appendChild(fecha);
      div.appendChild(nombre);
      div.appendChild(texto);
      div.appendChild(hr);

      contenedor.appendChild(div);
    });

  } catch (error) {
    console.error("Error al cargar comentarios:", error);
  }
}


function limpiarErroresInlineComentario() {
  const errorNombre = document.getElementById("error-nombre-comentario");
  const errorTexto = document.getElementById("error-texto-comentario");
  if (errorNombre) { errorNombre.textContent = ""; errorNombre.className = "error"; }
  if (errorTexto) { errorTexto.textContent = ""; errorTexto.className = "error"; }
}

async function validarComentario(nombre, texto) {
  const errores = [];
  const errorNombre = document.getElementById("error-nombre-comentario");
  const errorTexto = document.getElementById("error-texto-comentario");

  limpiarErroresInlineComentario();

  if (nombre.length < 3 || nombre.length > 80) {
    const msg = "El nombre debe tener entre 3 y 80 caracteres.";
    errores.push(msg);
    if (errorNombre) { errorNombre.textContent = msg; errorNombre.className = "error visible"; }
  }

  if (texto.length < 5 || texto.length > 300) {
    const msg = "El comentario debe tener entre 5 y 300 caracteres.";
    errores.push(msg);
    if (errorTexto) { errorTexto.textContent = msg; errorTexto.className = "error visible"; }
  } else if (await contienePalabraProhibida(texto)) {
    const msg = "El comentario contiene palabras no permitidas.";
    errores.push(msg);
    if (errorTexto) { errorTexto.textContent = msg; errorTexto.className = "error visible"; }
  }

  return errores;
}

function mostrarErroresComentario(errores) {
  const contenedorErrores = document.getElementById("comentario-errores");

  contenedorErrores.innerHTML = "";

  errores.forEach(error => {
    const p = document.createElement("p");

    p.classList.add("error");
    p.classList.add("visible");

    p.textContent = error;

    contenedorErrores.appendChild(p);
  });
}


async function agregarComentario(event) {
  event.preventDefault();

  const form = document.getElementById("comentario-form");
  const actividadId = form.dataset.actividadId;

  const nombreInput = document.getElementById("nombre");
  const textoInput = document.getElementById("texto");

  const nombre = nombreInput.value.trim();
  const texto = textoInput.value.trim();

  const errores = await validarComentario(nombre, texto);

  if (errores.length > 0) {
    document.getElementById("comentario-errores").innerHTML = "";
    return;
  }

  try {
    const response = await fetch(`/api/activities/${actividadId}/comentarios`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        nombre: nombre,
        texto: texto
      })
    });

    const data = await response.json();

    if (!data.ok) {
      mostrarErroresComentario(data.errores);
      return;
    }

    mostrarErroresComentario([]);
    limpiarErroresInlineComentario();

    nombreInput.value = "";
    textoInput.value = "";

    await cargarComentarios();
  } catch (error) {
    console.error("Error al agregar comentario:", error);
    mostrarErroresComentario(["Ocurrió un error al agregar el comentario."]);
  }
}


async function enviarEvaluacion(event) {
  event.preventDefault();

  const form = document.getElementById("evaluacion-form");
  const actividadId = form.dataset.actividadId;
  const seleccionado = form.querySelector("input[name='nota']:checked");
  const errorSpan = document.getElementById("error-evaluacion");

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

  try {
    const response = await fetch(`/api/activities/${actividadId}/notas`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ nota: nota })
    });

    const data = await response.json();

    if (response.status === 401) {
      errorSpan.textContent = "Debes iniciar sesión para evaluar.";
      errorSpan.className = "error visible";
      return;
    }

    if (response.status === 409 || Boolean(data.yaEvaluo)) {
      mostrarYaEvaluo();
      return;
    }

    if (!data.ok) {
      errorSpan.textContent = (data.errores && data.errores[0]) || "Error al guardar la evaluación.";
      errorSpan.className = "error visible";
      return;
    }

    const promedioSpan = document.getElementById("nota-promedio");
    const cantidadSpan = document.getElementById("nota-cantidad");

    if (promedioSpan) {
      promedioSpan.textContent = data.promedio !== null ? Number(data.promedio).toFixed(1) : "-";
    }

    if (cantidadSpan) {
      const n = data.cantidadNotas;
      cantidadSpan.textContent = n === 1 ? "1 evaluación" : `${n} evaluaciones`;
    }

    mostrarYaEvaluo();

  } catch (err) {
    console.error("Error al enviar evaluación:", err);
    errorSpan.textContent = "Ocurrió un error al enviar la evaluación.";
    errorSpan.className = "error visible";
  }
}

function mostrarYaEvaluo() {
  const seccion = document.getElementById("evaluacion-seccion");
  seccion.innerHTML = "";
  const msg = document.createElement("p");
  msg.className = "ya-evaluo";
  msg.textContent = "Ya evaluaste esta actividad.";
  seccion.appendChild(msg);
}


document.addEventListener("DOMContentLoaded", function () {
  const form = document.getElementById("comentario-form");

  if (!form) {
    return;
  }

  cargarComentarios();

  form.addEventListener("submit", agregarComentario);

  const evaluacionForm = document.getElementById("evaluacion-form");
  if (evaluacionForm) {
    evaluacionForm.addEventListener("submit", enviarEvaluacion);
  }
});