# Verificación de consistencia · 2026-11-13

Rama feature/asistente-odontologico, worktree miportafolio-fase3. Cambios locales sin commit/push/merge/deploy. V4 conservado literal; adenda operativa Fase 3 v2, corpus operativo v1.1. Especificación/diseño privados en la carpeta original y política/operación/pruebas del worktree se actualizaron juntos.

## Pruebas automatizadas

164 pruebas ejecutadas, 52 del asistente, cero fallos/errores. Copia temporal sin application-local. Maven offline, forkCount=0 y heap acotado por limitación de memoria del equipo. El agente de Mockito usó jdk.attach.allowAttachSelf=true. Los tests con proveedores controlados no se presentan como prueba real.

Cobertura añadida: petición de efecto sin datos/resumen/consentimiento no invoca nada ni genera registrar fallido; elección no es consentimiento; cambiar un dato/horario invalida la conformidad; repeticiones en plan/redacción ejecutan como máximo un efecto, incluso tras timeout; reintento exige petición explícita y resultado indeterminado; estado/correo guardados se releen; coincidencia de 10 con inicio 10:00–10:30; referencias de días locales; ambigüedades; sábados/domingos/franjas fuera de atención no se ofrecen/preparan; proyección institucional revisable; citas limitadas a IDs reales y diagnóstico de ciclo sin datos personales.

## Prueba integrada real

Spring local con OpenAI/RAG y workflow n8n de disponibilidad real. No se inspeccionaron/copiaron valores de credenciales. El usuario autorizó iniciar/detener el servidor con su configuración local habitual. El entorno de herramientas requirió el fallback TCP de PipeImpl del JDK, únicamente mediante argumentos transitorios del proceso; no se modificó configuración de proyecto por ese problema del equipo.

En el recorrido final se verificó:

- Reprogramación recupera y cita INST-06.
- Consultas sucesivas de disponibilidad generan lecturas actuales y evidencia nueva.
- La elección de una hora numérica (10) se resuelve contra el inicio de la franja real.
- Nombre/correo/motivo se acumulan; se presenta resumen sin efecto.
- Una corrección de correo acompañada de conformidad no ejecuta registrar: exige el resumen nuevo.
- No hay efectos antes de conformidad vigente. Tras conformidad se produce un único intento autorizado, que no llegó a enviar POST por configuración ausente.

Bloqueo comprobado en la configuración efectiva de Spring (solo booleanos, sin mostrar valores): disponibilidad-url/key presentes; registro-url/key ausentes. Faltan LOS_TILOS_N8N_REGISTRO_WEBHOOK_URL y LOS_TILOS_N8N_REGISTRO_WEBHOOK_KEY en el entorno/configuración del Spring local; Render no configura localhost. Registro/consulta de estado reales quedan pendientes hasta configurarlas y repetir integrada-real.ps1. No marcar el recorrido completo aprobado.

## Auditoría real de pgends

El usuario informó que auditar-horarios.sql no devolvió filas. Se confirmó además con una comprobación JDBC de solo lectura que usa la configuración de conexión existente de Spring, sin extraer valores: base pgends, cero horarios incompatibles con días/franjas semanales y cero solicitudes ficticias coincidentes con esta prueba. La transacción de comprobación terminó con ROLLBACK. No se modificó ningún dato.

Por tanto no se identificaron IDs que requieran corrección semanal y no se aplicó la deshabilitación de demo. Una disponibilidad que muestra solo lunes no basta por sí sola para esta conclusión; se auditó la tabla completa. La cobertura de feriados/cierres aprobados es un paso distinto y no se presume verificada.

SQL de calendario, función de disponibilidad y endurecimiento del registro están preparados, NO aplicados desde el agente. Deben revisarse/instalarse en el orden de registro.md para que PostgreSQL/n8n también impidan futuros cupos contradictorios. El workflow POST conserva su contrato actual; la consulta GET se actualiza con disponibilidad-parametrizada.sql. No se ejecutó una migración ni se cambió el workflow publicado en esta tarea.
