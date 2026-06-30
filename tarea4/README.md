# Funcionalidades implementadas

## Buscador de actividades

### Descripción general

Se implementó una vista de búsqueda en `/buscar` que permite encontrar actividades del sistema sin recargar la página. La búsqueda se dispara automáticamente mientras el usuario escribe, con un umbral mínimo de 3 caracteres.

### Flujo de uso

1. El usuario accede a `/buscar` y escribe en el campo `#buscar-texto`.
2. Cuando el texto tiene al menos 3 caracteres, se ejecuta la función `buscarActividades(query)` (con debounce de 300 ms para evitar peticiones innecesarias).
3. Se llama a `GET /api/actividades/buscar?q=<texto>` de forma asíncrona.
4. Los resultados se renderizan dinámicamente en el contenedor `#buscar-resultados`.

### Campos considerados en la búsqueda

La query JPQL en `ActividadRepository.buscarPorTexto(String q)` busca coincidencias (case-insensitive, con `LOWER` + `LIKE`) en:

- `a.nombre` — nombre de la actividad
- `a.descripcion` — descripción de la actividad
- `c.nombre` — nombre de la comuna del miembro asociado

### Datos mostrados por resultado

Cada tarjeta de resultado muestra:

| Campo | Fuente |
|---|---|
| Nombre de la actividad | `actividad.nombre` |
| Tipo de actividad | `actividad.tipo` |
| Día | `actividad.dia` |
| Miembro asociado | `miembro.nombre` |
| Comuna | `miembro.comuna.nombre` |
| Descripción | `actividad.descripcion` |
| Nota promedio | calculada en `ApiService.buscarActividades()` |

### Comportamiento cuando no hay resultados

Si `data.resultados.length === 0`, el contenedor muestra el texto: `"No se encontraron actividades para tu búsqueda."`.

Si la consulta tiene menos de 3 caracteres, se muestra el mensaje de error `#error-buscar` y el contenedor se vacía.

### Destacado de coincidencias

La función `destacarCoincidencia(texto, query)` en `buscar.js`:

1. Escapa el texto con `escaparHtml()` para prevenir XSS antes de cualquier manipulación.
2. Escapa el query para uso en RegExp con `escaparRegExp()`.
3. Aplica `String.replace()` con una expresión regular global e insensible a mayúsculas (`gi`) que envuelve cada coincidencia en una etiqueta `<mark>`.

El resultado se asigna a `.innerHTML` solo después del doble escapado. Se destacan: nombre de la actividad, descripción y nombre de la comuna.

### Archivos principales involucrados

| Capa | Archivo |
|---|---|
| Template | `templates/buscar.html` |
| JavaScript | `static/js/buscar.js` |
| Controlador vista | `AppController` → `GET /buscar` |
| Controlador API | `ApiController` → `GET /api/actividades/buscar` |
| Servicio | `ApiService.buscarActividades(String q, Integer miembroId)` |
| Repositorio | `ActividadRepository.buscarPorTexto(String q)` |

---

## Evaluación de actividades

### Descripción general

Los resultados del buscador y la vista de detalle de actividad permiten a un miembro autenticado evaluar una actividad con una nota entera entre 1 y 7. La nota se guarda en la base de datos y el promedio se recalcula y actualiza en la interfaz sin recargar la página.

### Tabla usada para guardar notas

La entidad `Nota` (clase `Nota.java`) se mapea a la tabla `nota` con las siguientes columnas:

| Columna | Tipo | Descripción |
|---|---|---|
| `id` | INTEGER (PK) | Generada automáticamente |
| `nota` | INTEGER NOT NULL | Valor entre 1 y 7 |
| `actividad_id` | INTEGER (FK) | Referencia a la actividad evaluada |
| `miembro_id` | INTEGER (FK) | Referencia al miembro que evaluó |

Restricción única `uq_nota_actividad_miembro` sobre `(actividad_id, miembro_id)`: cada miembro solo puede evaluar una vez por actividad.

### Flujo de evaluación

**Desde el buscador (`/buscar`):**

1. Cada tarjeta de resultado incluye la nota promedio (`.nota-valor`) y el contador (`.nota-cantidad`).
2. Si el usuario está autenticado y no ha evaluado aún (`puedeEvaluar: true`), aparece un botón "Evaluar".
3. Al hacer clic, se despliega un panel con radio buttons del 1 al 7 (creados dinámicamente por `crearPanelEvaluar(actividadId)`).
4. Al confirmar, la función `enviarNota(actividadId, nota, card)` llama a `POST /api/activities/{actividad_id}/notas`.
5. Tras éxito, se actualizan `.nota-valor` y `.nota-cantidad` en la tarjeta y la sección de evaluación se reemplaza por el mensaje "Ya evaluaste esta actividad."

**Desde el detalle de actividad (`/activities/{actividad_id}`):**

1. El template `detalle_actividad.html` renderiza el formulario `#evaluacion-form` con radio buttons del 1 al 7 si `puedeEvaluar` es verdadero (evaluado en `AppController`).
2. La función `enviarEvaluacion(event)` en `detalle_actividad.js` intercepta el submit y llama a `POST /api/activities/{actividadId}/notas`.
3. Tras éxito, actualiza los spans `#nota-promedio` y `#nota-cantidad` y reemplaza el formulario con el mensaje "Ya evaluaste esta actividad."

### Validaciones aplicadas

**Frontend (antes de enviar):**

- Se requiere que un radio button esté seleccionado; si no, se muestra error.
- Se verifica `Number.isInteger(nota) && nota >= 1 && nota <= 7`.

**Backend (`ApiService.agregarNota`):**

- Si `nota == null || nota < 1 || nota > 7` → respuesta 400 con mensaje de error.
- Si `notaRepository.existsByActividadIdAndMiembroId(actividadId, miembroId)` → respuesta 409 (`yaEvaluo: true`).
- En `ApiController`, el valor recibido del JSON se normaliza de `Integer`, `Long` o `Double` (entero) antes de pasar al servicio.

### Cálculo de nota promedio

Tras guardar cada `Nota`, `ApiService` llama a:

- `NotaRepository.findPromedioByActividadId(actividadId)` — JPQL: `SELECT AVG(n.nota) FROM Nota n WHERE n.actividad.id = :actividadId`
- `NotaRepository.countByActividadId(actividadId)` — derivado automáticamente por Spring Data.

El promedio se retorna en la respuesta JSON como `promedio` y el frontend lo formatea con `.toFixed(1)`. Si no hay notas, se muestra `-`.

Para la vista de detalle, `AppService.getPromedioNota()` y `AppService.getCantidadNotas()` delegan al mismo repositorio y el resultado se pasa al modelo Thymeleaf como `promedioTexto` y `cantidadNotas`.

### Actualización asíncrona de la interfaz

La llamada al endpoint usa `fetch` con método POST y `Content-Type: application/json`. La respuesta incluye `{ ok, promedio, cantidadNotas }`. El frontend actualiza el DOM directamente sin recargar la página:

- Buscador: actualiza `.nota-valor` y `.nota-cantidad` en la tarjeta afectada.
- Detalle: actualiza `#nota-promedio` y `#nota-cantidad`.

### Archivos principales involucrados

| Capa | Archivo |
|---|---|
| Template buscador | `templates/buscar.html` |
| Template detalle | `templates/detalle_actividad.html` |
| JS buscador | `static/js/buscar.js` |
| JS detalle | `static/js/detalle_actividad.js` |
| Controlador API | `ApiController` → `POST /api/activities/{actividad_id}/notas` |
| Controlador vista | `AppController` → `GET /activities/{actividad_id}` |
| Servicio API | `ApiService.agregarNota(Integer actividadId, Integer nota, Integer miembroId)` |
| Servicio vista | `AppService.getPromedioNota()`, `getCantidadNotas()`, `yaEvaluoActividad()` |
| Repositorio notas | `NotaRepository` |
| Entidad | `Nota` |

---

## Decisiones de implementación

**Búsqueda asíncrona con debounce.**
La búsqueda se realiza con `fetch` desde el frontend sin recargar la página. Se aplica un debounce de 300 ms sobre el evento `input` para evitar llamadas al servidor en cada pulsación de tecla.

**Umbral mínimo de 3 caracteres.**
El umbral se valida tanto en el frontend (antes de llamar al API) como en el backend (`ApiService.buscarActividades`), que retorna error si `q.trim().length < 3`.

**Búsqueda JPQL con LOWER + LIKE.**
Se usa una query JPQL en `ActividadRepository` en lugar de filtrado en Java, para delegar la búsqueda a la base de datos y evitar cargar todas las actividades en memoria.

**Nota promedio como valor mostrado.**
Se muestra el promedio de todas las notas de una actividad, no la nota individual del usuario. El promedio se calcula con `AVG` en la base de datos.

**Radio buttons como selector de nota.**
La selección de nota usa `<input type="radio">` con valores fijos del 1 al 7, generados en el template HTML (detalle) o dinámicamente en JS (buscador). No se acepta texto libre.

**Un voto por miembro por actividad.**
La restricción `uq_nota_actividad_miembro` en la tabla `nota` y la verificación previa con `existsByActividadIdAndMiembroId` en el servicio garantizan que cada miembro solo evalúe una vez. En caso de duplicado, el backend responde con HTTP 409.

**Recálculo inmediato del promedio.**
Después de guardar la nota, el servicio recalcula el promedio y la cantidad de evaluaciones en la misma operación y los incluye en la respuesta JSON, evitando una segunda llamada al servidor.

**Actualización del DOM sin recargar.**
El frontend actualiza únicamente los elementos afectados (nota promedio, contador, sección de evaluación) manipulando el DOM directamente con JavaScript, sin recargar la página ni el listado de resultados.

**Separación de responsabilidades.**
- La base de datos almacena y agrega los datos (tabla `nota`, query `AVG`).
- `ApiService` contiene la lógica de negocio (validación, persistencia, cálculo).
- `ApiController` gestiona la capa HTTP (parseo del body, manejo de sesión, códigos de respuesta).
- El frontend (`buscar.js`, `detalle_actividad.js`) se encarga de la presentación y la interacción.
