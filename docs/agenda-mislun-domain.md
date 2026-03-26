## Estructura hexagonal del módulo

El módulo `Agenda Mislun` se integra siguiendo la arquitectura hexagonal existente del proyecto.

### Estructura de paquetes
miportafolio/
└── src/main/java/moondolphin/miportafolio/
    ├── domain/
    │   ├── model/
    │   │   ├── Tarea.java
    │   │   ├── NotaLibre.java
    │   │   ├── JournalEntry.java
    │   │   ├── LinkItem.java
    │   │   ├── Recordatorio.java
    │   │   ├── Etiqueta.java
    │   │   ├── MoodEntry.java
    │   │   ├── CollageEntry.java
    │   │   └── Usuario.java
    │   │
    │   └── port/
    │       ├── in/
    │       │   ├── TareaServicePort.java
    │       │   ├── NotaServicePort.java
    │       │   ├── JournalServicePort.java
    │       │   ├── LinkServicePort.java
    │       │   ├── RecordatorioServicePort.java
    │       │   ├── UsuarioServicePort.java
    │       │   └── AuthServicePort.java
    │       │
    │       └── out/
    │           ├── TareaRepositoryPort.java
    │           ├── NotaRepositoryPort.java
    │           ├── JournalRepositoryPort.java
    │           ├── LinkRepositoryPort.java
    │           ├── RecordatorioRepositoryPort.java
    │           ├── UsuarioRepositoryPort.java
    │           └── CollageRepositoryPort.java
    │
    ├── app/
    │   └── usecase/
    │       ├── TareaService.java
    │       ├── NotaService.java
    │       ├── JournalService.java
    │       ├── LinkService.java
    │       ├── RecordatorioService.java
    │       ├── UsuarioService.java
    │       └── AuthService.java
    │
    └── adapters/
        ├── primary/
        │   └── httpapi/
        │       └── agenda/
        │           ├── AuthHandler.java
        │           ├── TareaHandler.java
        │           ├── NotaHandler.java
        │           ├── JournalHandler.java
        │           ├── LinkHandler.java
        │           ├── RecordatorioHandler.java
        │           ├── UsuarioHandler.java
        │           └── CollageHandler.java
        │
        └── secondary/
            └── persistence/postgresql/
                ├── entity/
                │   ├── TareaJpaEntity.java
                │   ├── NotaJpaEntity.java
                │   ├── JournalJpaEntity.java
                │   ├── LinkJpaEntity.java
                │   ├── RecordatorioJpaEntity.java
                │   ├── UsuarioJpaEntity.java
                │   └── CollageJpaEntity.java
                │
                ├── repository/
                │   ├── TareaJpaRepository.java
                │   ├── NotaJpaRepository.java
                │   ├── JournalJpaRepository.java
                │   ├── LinkJpaRepository.java
                │   ├── RecordatorioJpaRepository.java
                │   ├── UsuarioJpaRepository.java
                │   └── CollageJpaRepository.java
                │
                └── adapter/
                    ├── TareaRepositoryAdapter.java
                    ├── NotaRepositoryAdapter.java
                    ├── JournalRepositoryAdapter.java
                    ├── LinkRepositoryAdapter.java
                    ├── RecordatorioRepositoryAdapter.java
                    ├── UsuarioRepositoryAdapter.java
                    └── CollageRepositoryAdapter.java

## 9. Arquitectura propuesta

El módulo seguirá la arquitectura hexagonal del proyecto actual.

### Capas

* **domain**: modelos, enums, puertos
* **app/usecase**: lógica de negocio del módulo
* **adapters/primary/httpapi**: handlers o controllers REST
* **adapters/secondary/persistence/postgresql**: entidades JPA, repositorios y adapters
* **frontend**: páginas estáticas, JS o templates consumiendo la API

### Principio

El módulo debe añadirse como subdominio nuevo, sin mezclar su lógica con el módulo actual de proyectos.

---

## 10. Modelo conceptual de dominio

### 10.1 Usuario

Campos sugeridos:

* id
* username
* email
* passwordHash
* role
* approved
* active
* createdAt
* updatedAt

### 10.2 Tarea

Campos sugeridos:

* id
* titulo
* descripcion
* fecha
* horaInicio opcional
* horaFin opcional
* prioridad
* estado
* etiquetas
* createdBy
* createdAt
* updatedAt

### 10.3 NotaLibre

Campos sugeridos:

* id
* titulo opcional
* contenido
* etiquetas
* fechaReferencia opcional
* createdBy
* createdAt
* updatedAt

### 10.4 JournalEntry

Campos sugeridos:

* id
* titulo opcional
* contenido
* moodId opcional
* etiquetas
* fechaReferencia
* createdBy
* createdAt
* updatedAt

### 10.5 LinkItem

Campos sugeridos:

* id
* titulo
* url
* descripcion opcional
* etiquetas
* fechaReferencia opcional
* createdBy
* createdAt
* updatedAt

### 10.6 Recordatorio

Campos sugeridos:

* id
* titulo
* descripcion opcional
* fecha
* hora opcional
* estado
* etiquetas
* createdBy
* createdAt
* updatedAt

### 10.7 Etiqueta

Campos sugeridos:

* id
* nombre
* color opcional
* createdAt

### 10.8 MoodEntry

Campos sugeridos:

* id
* fecha
* estadoAnimo
* notaOpcional
* createdBy
* createdAt

### 10.9 CollageEntry

Campos sugeridos:

* id
* titulo opcional
* imageUrl o imagePath
* gifUrl opcional
* quote
* fechaReferencia opcional
* createdBy
* createdAt
* updatedAt

---

## 12. Casos de uso principales

### UC-01 Iniciar sesión

El usuario ingresa credenciales válidas y, si está aprobado, accede al módulo.

### UC-02 Cerrar sesión

El usuario autenticado finaliza su sesión.

### UC-03 Cambiar contraseña

El usuario autenticado actualiza su contraseña.

### UC-04 Aprobar usuario

El administrador habilita a un usuario registrado para que pueda acceder al módulo.

### UC-05 Gestionar tareas

Crear, editar, eliminar y consultar tareas por día y horario.

### UC-06 Gestionar notas sueltas

Crear, editar, eliminar y consultar notas breves.

### UC-07 Gestionar journal

Crear, editar, eliminar y consultar entradas expresivas.

### UC-08 Gestionar calendario

Visualizar un día y consultar o registrar contenido asociado.

### UC-09 Gestionar recordatorios

Crear, editar, eliminar y consultar recordatorios.

### UC-10 Gestionar etiquetas

Crear y asociar etiquetas a distintos contenidos.

### UC-11 Registrar mood

Registrar estado de ánimo en una fecha.

### UC-12 Gestionar links

Crear, editar, eliminar y consultar links útiles.

### UC-13 Gestionar collage

Registrar un bloque visual simple con imagen, gif y quote.

### UC-14 Buscar histórico

Realizar búsqueda transversal por texto sobre contenidos persistidos.

---

## 15. Persistencia

El módulo utilizará la misma base PostgreSQL ya usada por el proyecto.

Tablas sugeridas:

* `users`
* `tasks`
* `notes`
* `journal_entries`
* `links`
* `reminders`
* `tags`
* `mood_entries`
* `collage_entries`
* tablas de relación para etiquetas si corresponde

---
