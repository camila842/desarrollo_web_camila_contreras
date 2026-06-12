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


function validarComentario(nombre, texto) {
  const errores = [];

  if (nombre.length < 3 || nombre.length > 80) {
    errores.push("El nombre debe tener entre 3 y 80 caracteres.");
  }

  if (texto.length < 5) {
    errores.push("El comentario debe tener al menos 5 caracteres.");
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

  const errores = validarComentario(nombre, texto);

  if (errores.length > 0) {
    mostrarErroresComentario(errores);
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

    nombreInput.value = "";
    textoInput.value = "";

    await cargarComentarios();
  } catch (error) {
    console.error("Error al agregar comentario:", error);
    mostrarErroresComentario(["Ocurrió un error al agregar el comentario."]);
  }
}


document.addEventListener("DOMContentLoaded", function () {
  const form = document.getElementById("comentario-form");

  if (!form) {
    return;
  }

  const actividadId = form.dataset.actividadId;

  cargarComentarios(actividadId);

  form.addEventListener("submit", agregarComentario);
});