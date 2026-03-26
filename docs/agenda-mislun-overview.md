## Módulo `Agenda Mislun`

## 1. Overview

`Agenda Mislun` es un módulo nuevo dentro del proyecto `miportafolio`, pensado como una aplicación privada integrada al portafolio público. Su propósito es ofrecer un espacio híbrido donde puedan coexistir organización funcional y expresión personal en una misma interfaz, sin obligar a usar una lógica rígida o lineal.

El módulo tendrá una doble función:

* **uso personal real**, como agenda, journal, repositorio de notas, links, recordatorios y registro emocional;
* **muestra técnica**, como evidencia de diseño funcional, modelado de dominio, autenticación, autorización, persistencia y construcción modular dentro de una arquitectura hexagonal.

El acceso al módulo estará visible desde el Home del portafolio, pero su uso completo quedará restringido a usuarios autorizados.

---

## 2. Objetivo

Construir un espacio privado, persistente y visualmente amable que permita:

* registrar tareas por fecha y horario;
* mantener notas sueltas;
* escribir entradas tipo journal;
* guardar links útiles;
* consultar un calendario por día;
* registrar recordatorios;
* clasificar contenido mediante etiquetas;
* registrar estado de ánimo o mood del día;
* incorporar un espacio visual tipo collage con imagen, gif y quote;
* buscar información histórica en forma transversal.

La aplicación no debe forzar separación absoluta entre lo funcional y lo simbólico, sino permitir que **ambos convivan dentro del mismo día**, según el estado o necesidad del momento.

---

## 3. Alcance

### Incluye en esta versión

* acceso desde Home mediante ícono visible;
* vista pública limitada del módulo;
* autenticación con `username/email + password`;
* logout;
* cambio de contraseña;
* autorización por roles;
* validación adicional por permiso/aprobación del administrador;
* CRUD de tareas;
* CRUD de notas sueltas;
* CRUD de links;
* CRUD de recordatorios;
* gestión de etiquetas;
* registro de estados de ánimo;
* calendario diario;
* espacio collage simple con imagen, gif y quote;
* buscador histórico general;
* persistencia en PostgreSQL;
* integración con backend Spring Boot existente.

### No incluye en esta versión

* colaboración simultánea en tiempo real;
* chat interno;
* sincronización con Google Calendar u otros servicios externos;
* notificaciones push;
* editor visual avanzado de collage;
* analíticas complejas en tiempo real;
* motor de recomendaciones automáticas;
* carga de archivos pesados fuera de los necesarios para el collage básico.

---

## 4. Contexto funcional

El módulo nace para resolver una tensión concreta entre dos formas de uso personal:

* **organización externa**: tareas, horarios, agenda, estudio, trabajo, recordatorios;
* **expresión interna**: notas libres, journal, collage, registro simbólico, links con resonancia, mood del día.

La mezcla desordenada de ambas funciones en herramientas rígidas genera presión estética, bloqueo y abandono. Por eso, `Agenda Mislun` se diseña como un entorno donde estructura y libertad puedan coexistir sin anularse.

---

## 5. Usuarios y roles

### Usuario público

Es cualquier visitante no autenticado del portafolio.
Puede ver que el módulo existe y acceder a una **demo mínima sin edición**, acompañada por un mensaje que indique que la aplicación es de uso privado y requiere permiso para utilizarla.

No puede acceder a contenido real privado.

### Usuario registrado sin aprobación

Es un usuario con credenciales creadas pero aún no habilitado por la administradora.
Puede intentar autenticarse, pero no debe obtener acceso operativo al módulo mientras no tenga aprobación explícita.

### Usuario `USER` aprobado

Es un usuario autenticado, habilitado por la administradora.
Puede acceder al módulo según los permisos definidos para su rol. En esta primera versión, este rol queda preparado a nivel de diseño aunque el uso principal siga siendo personal.

### Usuario `ADMIN`

Es la administradora del sistema.
Puede:

* aprobar o rechazar usuarios;
* iniciar sesión y cerrar sesión;
* cambiar contraseña;
* crear, editar y eliminar tareas;
* crear, editar y eliminar notas;
* crear, editar y eliminar links;
* crear, editar y eliminar recordatorios;
* gestionar etiquetas;
* registrar y consultar moods;
* cargar contenido de collage;
* acceder al histórico mediante buscador general;
* consultar calendario;
* administrar permisos.

---

## 6. Requerimientos funcionales

### RF-01 — Acceso autenticado

El sistema debe permitir login mediante `email o username` y `password`.

### RF-02 — Logout

El sistema debe permitir cerrar sesión de forma explícita.

### RF-03 — Cambio de contraseña

El sistema debe permitir al usuario autenticado cambiar su contraseña.

### RF-04 — Aprobación de usuarios

El sistema debe contemplar que un usuario registrado no pueda utilizar el módulo hasta ser aprobado por el administrador.

### RF-05 — Demo pública

El sistema debe ofrecer una demo pública mínima sin acceso a edición ni a datos privados reales.

### RF-06 — Tareas por horario

El sistema debe permitir crear, listar, editar y eliminar tareas asociadas a una fecha y, opcionalmente, a un horario específico.

### RF-07 — Atributos de tarea

Cada tarea debe contemplar como mínimo:

* título;
* descripción;
* fecha;
* horario opcional o rango horario;
* prioridad;
* estado;
* etiquetas.

### RF-08 — Notas sueltas

El sistema debe permitir registrar notas breves de texto libre, independientes del journal.

### RF-09 — Journal expression

El sistema debe permitir registrar entradas de journal de contenido más expresivo o reflexivo.

### RF-10 — Calendario

El sistema debe permitir navegar por días y asociar tareas, recordatorios y contenido relevante a una fecha concreta.

### RF-11 — Recordatorios

El sistema debe permitir crear, listar, editar y eliminar recordatorios.

### RF-12 — Etiquetas

El sistema debe permitir clasificar tareas, notas, journal, links y recordatorios mediante etiquetas.

### RF-13 — Histórico general

El sistema debe ofrecer un buscador histórico transversal para recuperar contenido por texto, incluyendo tareas, notas, journal y links.

### RF-14 — Mood del día

El sistema debe permitir registrar estados de ánimo o mood del día.

### RF-15 — Estadísticas de mood

El sistema debe dejar disponible la información de moods para futuras estadísticas.

### RF-16 — Lista de links

El sistema debe permitir guardar links como entidad propia.

### RF-17 — Collage simple

El sistema debe permitir registrar un bloque visual con:

* una imagen;
* un gif o enlace embebible;
* una frase o quote.

### RF-18 — Coexistencia funcional/simbólica

El sistema no debe forzar que el día sea exclusivamente funcional o exclusivamente simbólico. Ambos tipos de contenido deben poder convivir en la misma fecha.

---

## 7. Requerimientos no funcionales

### RNF-01 — Seguridad

Las contraseñas deben almacenarse con hash seguro, nunca en texto plano.

### RNF-02 — Arquitectura

El módulo debe respetar la arquitectura hexagonal existente del proyecto.

### RNF-03 — Persistencia

La información debe almacenarse en PostgreSQL.

### RNF-04 — Escalabilidad

El diseño debe soportar crecimiento hacia múltiples usuarios y permisos más granulares.

### RNF-05 — Mantenibilidad

Las responsabilidades deben permanecer separadas entre dominio, casos de uso, controladores y persistencia.

### RNF-06 — Usabilidad

La interfaz debe priorizar baja fricción, lectura clara y acceso rápido a bloques frecuentes.

### RNF-07 — Estética visual

La interfaz debe responder a una identidad visual pastel, amable y atmosférica, sin perder legibilidad.

## 11. Reglas de negocio

* Solo usuarios autenticados y aprobados pueden usar el módulo privado.
* Solo `ADMIN` puede aprobar usuarios.
* Un usuario no aprobado no puede operar contenido.
* Toda tarea debe tener título y fecha.
* Toda nota libre debe tener contenido.
* Toda entrada journal debe tener contenido.
* Todo link debe tener URL válida.
* Todo recordatorio debe tener al menos título y fecha.
* El mood puede registrarse por fecha.
* Una misma fecha puede contener simultáneamente tareas, journal, notas, links y mood.
* El contenido privado nunca debe mostrarse en la demo pública.
* El buscador histórico debe recuperar coincidencias en múltiples tipos de contenido.

---

## 17. MVP recomendado

Para una primera entrega razonable y defendible, conviene priorizar:

* login
* roles y aprobación
* tareas
* notas sueltas
* journal
* links
* buscador histórico básico
* mood
* demo pública mínima

### Segunda etapa

* calendario más rico
* recordatorios avanzados
* collage con imagen/gif
* estadísticas de mood
* UI más refinada

### Tercera etapa

* mejoras visuales profundas
* permisos más granulares
* más tipos de contenido
* integraciones externas si alguna vez te interesa

---

## 18. Riesgos y consideraciones

* Querer meter todo en v1 puede inflar demasiado el módulo.
* El collage visual puede volverse más costoso de lo que parece.
* El buscador histórico transversal necesita buen diseño de consultas.
* La aprobación de usuarios agrega una capa útil de seguridad, pero también más lógica de negocio.
* La demo pública debe estar muy bien aislada para no exponer datos reales.

---

## 19. Decisiones de diseño

### Separación suave, no rígida

No se obliga a dividir el día en “modo funcional” o “modo simbólico”. Ambos conviven.

### Seguridad reforzada

Registro no equivale a acceso: hace falta aprobación.

### Módulo escalable

Aunque el uso principal sea personal, se deja preparado para múltiples usuarios.

### UI como parte del producto

La estética pastel no es adorno secundario; forma parte de la experiencia del módulo.

---

## 20. Resumen ejecutivo

`Agenda Mislun` será un módulo privado integrado al portafolio de Yesica Mabel Puerto, con acceso restringido y diseño híbrido entre agenda funcional y espacio expresivo. El sistema ofrecerá tareas por horario, calendario, notas, journal, links, recordatorios, etiquetas, moods, collage y buscador histórico, con persistencia en PostgreSQL y seguridad basada en autenticación, roles y aprobación administrativa. La interfaz seguirá una estética pastel, amable y atmosférica, manteniendo coherencia con la identidad visual del portafolio.

---