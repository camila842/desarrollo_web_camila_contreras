# CC5002 - Tarea 1

En los últimos años se ha incrementado de forma notoria la cantidad de estudiantes en el DCC, con lo cual también ha aumentado la cantidad de funcionarios(as) y académicos(as) del Departamento. Con este aumento de integrantes de la comunidad, el Área de Calidad de Vida del Departamento está preocupada de poder conocer de mejor forma las actividades que realizan los y las integrantes de la comunidad, aparte de las obligaciones académicas y laborales. En particular, el interés es sobre actividades de tipo: artísticas, deportivas, tecnológicas, sociales y recreativas en general.

Para ello se ha comenzado a implementar una aplicación web que permita entender la vida de la comunidad DCCiana. Esta consta de 4 interfaces principales que vendrían siendo la página de Bienvenida, que funcionará como página de entrada, con resúmenes de la comunidad o información importante que se determine que pueda tomar lugar; una página de Registro, que permita a los usuarios crear una cuenta dentro de la aplicación; una página Actividades, que permitirá que los usuarios registren las actividades extracurriculares que realizan; y una página denominada Miembros, que permitirá hacer *queries* sobre la información de los miembros de la comunidad y redirigir a otra página en la que se puedan ver las métricas de esta misma. A las distintas interfaces se puede acceder por medio de un encabezado que todas tienen en común, que redirige al usuario a cada sección antes mencionada.

### Página de Bienvenida

La página de bienvenida muestra un mensaje introductorio de la aplicación **Extra-Curricular DCC**, indicando que el sistema permite gestionar miembros y actividades extracurriculares del Departamento de Ciencias de la Computación.

Además, se incluye una tabla con los últimos 5 miembros agregados al sistema. Para cada registro se muestra la información principal disponible:

- Nombre del miembro.
- Email del miembro.
- Actividad asociada.
- Tipo de actividad.

En los casos donde un miembro aún no tenga actividades registradas, los campos correspondientes a `Actividad` y `Tipo` pueden aparecer como `None`, ya que no existe una actividad asociada a ese miembro en la base de datos.

### Registro

El sitio de registro de usuarios corresponde a un formulario que pide que este entregue datos tales como su nombre, apellido, rol en la facultad, correo electrónico y contraseña. Para que el usuario pueda inscribirse exitosamente en la plataforma, deberá pasar el filtro de registro, o mejor dicho, la validación del formulario, que considera los siguientes puntos:

* Los campos Nombre y Apellido deben no estar vacíos y tener un largo de al menos 3 caracteres.
* El campo "Rol en la Facultad" no debe ser el entregado por defecto; el usuario debe haber hecho una selección de alguno de ellos.
* El Correo Electrónico debe:
    - No ser vacío.
    - Tener el carácter "@".
    - No estar vacío a ambos lados del carácter "@".
    - A la derecha del "@", debe haber un texto que contenga un punto y que a ambos lados de este no tenga un string vacío (decisión tomada con tal de que el correo sea ingresado con un dominio *válido*).
    - A la izquierda del "@" debe haber un string de largo al menos 3.
* Basado en gestores de contraseñas ya existentes, se estableció que las contraseñas debían tener un largo mínimo de 8 caracteres y debían incluir alguno de los siguientes caracteres especiales: *"@, ¿, ?, {, }, ."*. Estos fueron elegidos de manera arbitraria y en pequeña cantidad con tal de solo lograr que el código funcione.

Una vez que se cumplan todos los puntos señalados previamente, el usuario podrá obtener una cuenta en la plataforma.

### Actividades

A modo de diseño, la página comienza tan solo con un botón que, al presionarlo, despliega el formulario para que el usuario ingrese alguna actividad. Este formulario requiere un nombre de actividad, los días en que se realiza, el tipo y alguna URL y archivo que evidencie la existencia y ejecución de dicha actividad. Además, para que la actividad quede inscrita exitosamente, se necesita pasar una validación que considera:

* El Nombre de la Actividad no debe ser vacío y debe tener un largo de al menos 3 caracteres.
* Debe haberse seleccionado por lo menos un día de la semana. Una vez seleccionado, aparecen dos inputs nuevos, en los cuales se debe ingresar la hora de inicio y término de la actividad.
* El Tipo de Actividad no puede ser el por defecto.
* El ingreso de al menos un archivo en formato *".png, .jpg, .jpeg, .mp4"*.
* El link que se ingrese debe ser una URL en formato válido.

Si y solo si se cumplen los puntos anteriores, el usuario podrá registrar una actividad que será listada dentro de la misma página.

### Miembros

La página para realizar *queries* sobre los miembros de la comunidad contiene un pequeño formulario que pregunta por los filtros que se querrán aplicar sobre los datos. Al presionar el botón de "Buscar", se puede enviar el formulario, el cual no cuenta con una validación, ya que los inputs están restringidos a valores predeterminados y, de no seleccionarse alguno de ellos, la búsqueda solo adquirirá un valor "*default*" y desplegará los resultados. Originalmente, se pensó la opción de ordenar por más de un atributo duplicando esa sección específica del formulario; sin embargo, por temas de tiempo no pudo ser aplicada. Por último, dada la falta de datos backend del proyecto, al presionar buscar solo aparecen unos valores en la tabla que fueron puestos a mano en el código.

Dentro de esta sección, se adjuntó una forma de acceder a una página distinta que contendrá las gráficas de la información. En el futuro se considerará que estas puedan generarse en base a *queries default* a la base de datos, que podrán generarse por medio de formularios como el de filtros de la página de miembros, es decir, estableciendo opciones predeterminadas que en conjunto provean una *query* válida.

### Nota sobre la base de datos

Durante el desarrollo de esta entrega se detectaron inconsistencias entre el esquema original de la base de datos y los requerimientos implementados en la Tarea 1. Por esta razón, se realizaron modificaciones sobre algunas tablas para mantener coherencia entre el modelo de datos, los formularios y las funcionalidades solicitadas.

Estas modificaciones ya fueron incorporadas en los scripts SQL entregados por el equipo docente, por lo que basta con ejecutar dichos scripts para obtener el esquema actualizado.

En caso de querer aplicar únicamente las alteraciones realizadas sobre una base de datos ya existente, se debe revisar el archivo `tarea2.sql` y ejecutar las secciones que contienen sentencias `ALTER`.