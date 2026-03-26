
## Navegación desde Home

El acceso al módulo Agenda Mislun se realiza desde el ícono "Notebook" ubicado en `Home.html`.

### Comportamiento

- Al hacer click en el ícono:
  - Si el usuario NO está autenticado:
    → redirigir a `/agenda/login` o mostrar demo pública.

  - Si el usuario está autenticado pero NO aprobado:
    → mostrar pantalla de acceso pendiente de autorización.

  - Si el usuario está autenticado y aprobado:
    → redirigir a `/agenda/dashboard`.

### Consideraciones

- El acceso debe ser visible para todos los usuarios.
- El contenido real del módulo nunca debe exponerse sin autenticación.
- La navegación debe ser consistente con el resto del portafolio.

## 8. Lineamientos de UI/UX

A partir de tu esquema, la pantalla principal del módulo queda conceptualmente organizada en estos bloques:

* **arriba izquierda**: tareas hoy;
* **arriba centro/derecha**: histórico buscador, etiquetas, mood;
* **arriba derecha**: calendario;
* **derecha media**: recordatorio;
* **abajo izquierda**: notas sueltas;
* **abajo centro**: lista de links;
* **abajo derecha**: journal expression;
* **sector visual complementario**: collage.

### Criterios de experiencia

* la pantalla debe permitir lectura rápida del estado del día;
* los bloques deben sentirse como “espacios habitables”, no como paneles corporativos duros;
* lo funcional y lo simbólico deben convivir sin competir visualmente;
* el acceso a carga/edición debe ser simple.

### Lineamientos visuales

Paleta sugerida:

* rosa pastel
* violeta pastel
* celeste pastel
* amarillo pastel

Texto:

* blanco
* violeta oscuro para contraste

La estética general debe sentirse:

* suave;
* íntima;
* ordenada;
* onírica pero usable.

Esto no forma parte de la lógica de negocio, pero sí del criterio de diseño del módulo.

---
## 13. API propuesta

### Autenticación

* `POST /api/v1/auth/login`
* `POST /api/v1/auth/logout`
* `PUT /api/v1/auth/password`

### Usuarios

* `POST /api/v1/users/register`
* `GET /api/v1/admin/users/pending`
* `PUT /api/v1/admin/users/{id}/approve`
* `PUT /api/v1/admin/users/{id}/reject`
* `GET /api/v1/admin/users`

### Tareas

* `GET /api/v1/admin/tareas`
* `GET /api/v1/admin/tareas/{id}`
* `POST /api/v1/admin/tareas`
* `PUT /api/v1/admin/tareas/{id}`
* `DELETE /api/v1/admin/tareas/{id}`

Filtros posibles:

* `?fecha=2026-03-24`
* `?prioridad=ALTA`
* `?estado=PENDIENTE`
* `?tag=estudio`

### Notas sueltas

* `GET /api/v1/admin/notas`
* `GET /api/v1/admin/notas/{id}`
* `POST /api/v1/admin/notas`
* `PUT /api/v1/admin/notas/{id}`
* `DELETE /api/v1/admin/notas/{id}`

### Journal

* `GET /api/v1/admin/journal`
* `GET /api/v1/admin/journal/{id}`
* `POST /api/v1/admin/journal`
* `PUT /api/v1/admin/journal/{id}`
* `DELETE /api/v1/admin/journal/{id}`

### Links

* `GET /api/v1/admin/links`
* `GET /api/v1/admin/links/{id}`
* `POST /api/v1/admin/links`
* `PUT /api/v1/admin/links/{id}`
* `DELETE /api/v1/admin/links/{id}`

### Recordatorios

* `GET /api/v1/admin/recordatorios`
* `GET /api/v1/admin/recordatorios/{id}`
* `POST /api/v1/admin/recordatorios`
* `PUT /api/v1/admin/recordatorios/{id}`
* `DELETE /api/v1/admin/recordatorios/{id}`

### Etiquetas

* `GET /api/v1/admin/etiquetas`
* `POST /api/v1/admin/etiquetas`
* `PUT /api/v1/admin/etiquetas/{id}`
* `DELETE /api/v1/admin/etiquetas/{id}`

### Mood

* `GET /api/v1/admin/moods`
* `POST /api/v1/admin/moods`
* `GET /api/v1/admin/moods/stats`

### Collage

* `GET /api/v1/admin/collage`
* `GET /api/v1/admin/collage/{id}`
* `POST /api/v1/admin/collage`
* `PUT /api/v1/admin/collage/{id}`
* `DELETE /api/v1/admin/collage/{id}`

### Histórico

* `GET /api/v1/admin/historico/search?q={texto}`

### Demo pública

* `GET /api/v1/public/agenda-mislun/demo`

---

## 14. Seguridad

Se propone implementar seguridad con Spring Security.

### Lineamientos

* password hasheada con algoritmo seguro;
* endpoints privados protegidos;
* autorización por roles `ADMIN` y `USER`;
* validación adicional de aprobación;
* configuración sensible por variables de entorno;
* separación estricta entre endpoints públicos y privados.

### Regla clave

No basta con estar registrado: el usuario debe además estar **aprobado**.

---

## 16. Integración con el frontend

Desde `Home.html` se agregará un acceso visual al módulo `Agenda Mislun`.

### Comportamiento esperado

* visitante público: ve el acceso, entra a demo mínima o pantalla informativa;
* usuario autenticado y aprobado: accede al dashboard real;
* usuario autenticado no aprobado: recibe mensaje de acceso pendiente de autorización.

### Pantallas mínimas sugeridas

* login
* vista pública/demo
* dashboard principal
* formulario/listado de tareas
* formulario/listado de notas
* calendario diario
* journal expression
* links
* recordatorios
* collage

---