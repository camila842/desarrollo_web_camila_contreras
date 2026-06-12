# CC5002 - Tarea 3


## Sobre esta entrega

Durante el desarrollo se incorporaron nuevas funcionalidades orientadas a mejorar la visualización de información, la navegación entre entidades y la interacción de los usuarios con las actividades registradas.

### Visualización de datos mediante gráficos

Se agregó una sección de visualización estadística para apoyar el análisis de los datos registrados en la aplicación. En particular, se trabajó en gráficos asociados a los registros de miembros y actividades, permitiendo observar tendencias y cantidades de forma más clara que con tablas tradicionales.

Esta decisión busca que la aplicación no solo permita registrar información, sino también entregar una vista resumida y útil del estado del sistema.


### Comentarios en actividades

Se incorporó la funcionalidad de comentarios asociados a actividades. Estos comentarios permiten agregar observaciones o información adicional sobre una actividad específica.

Como decisión de diseño, los comentarios no se muestran directamente desde un listado general de actividades. En su lugar, se decidió que aparezcan al desplegar o consultar una actividad asociada a un miembro. Es decir, el flujo esperado es:

1. Ir a la sección de miembros o listado de miembros.
2. Seleccionar un miembro.
3. Ver las actividades asociadas a ese miembro.
4. Entrar al detalle de una actividad.
5. Consultar o agregar comentarios para esa actividad.

Esta decisión mantiene el listado principal más limpio y evita sobrecargar la interfaz con información secundaria. Además, vincula los comentarios al contexto en que son más útiles: la relación entre un miembro y sus actividades.

### Navegación y detalle de actividades

Se agregó una vista de detalle para cada actividad. Esta vista muestra información específica de la actividad, como nombre, tipo, día, hora de inicio, duración y descripción.

Desde esta página también se permite agregar comentarios y volver al detalle del miembro relacionado.

Para mantener HTML válido, los botones de navegación fueron implementados como enlaces estilizados mediante CSS cuando su función es redirigir a otra página.

### Filtro de palabras en comentarios

Como decisión de diseño, se incorporó un filtro básico de palabras que pueden ser consideradas ofensivas o inapropiadas dentro de los comentarios.

El objetivo de este filtro es evitar que se registren comentarios con lenguaje inadecuado en la plataforma, manteniendo un espacio más respetuoso para la comunidad. Si el texto ingresado contiene alguna palabra incluida en la lista de términos restringidos, el comentario no es aceptado por el sistema.

Este filtro fue implementado como una validación adicional sobre el formulario de comentarios. La lista de palabras consideradas no permitidas fue definida de manera manual y puede ser ajustada en el futuro según los criterios de moderación que se quieran aplicar.