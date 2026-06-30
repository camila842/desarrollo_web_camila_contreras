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

function destacarCoincidencia(texto, query) {
  if (!texto) {
    return "";
  }

  const textoEscapado = escaparHtml(texto);
  const queryEscapado = escaparRegExp(escaparHtml(query));

  if (!queryEscapado) {
    return textoEscapado;
  }

  const regex = new RegExp(`(${queryEscapado})`, "gi");
  return textoEscapado.replace(regex, "<mark>$1</mark>");
}

function crearResultadoNode(actividad, query) {
  const div = document.createElement("div");
  div.className = "form-results";

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

  return div;
}

async function buscarActividades(query) {
  const contenedor = document.getElementById("buscar-resultados");

  try {
    const response = await fetch(`/api/actividades/buscar?q=${encodeURIComponent(query)}`);
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
  } catch (error) {
    console.error("Error al buscar actividades:", error);
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

document.addEventListener("DOMContentLoaded", function () {
  const input = document.getElementById("buscar-texto");

  if (!input) {
    return;
  }

  const contenedor = document.getElementById("buscar-resultados");
  const errorBuscar = document.getElementById("error-buscar");

  const buscarDebounced = debounce((query) => {
    buscarActividades(query);
  }, 300);

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
