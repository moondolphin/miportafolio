# Operación vigente del asistente · Fase 3

Esta guía reemplaza los recorridos anteriores. Reglas normativas: implementacion_fase3_codex.md y adenda operativa Fase 3 v3 en src/main/resources/asistente/tool-policy.txt. Ante un cambio, actualizar conjuntamente especificación, diseño técnico, política, operación, implementación y pruebas; retirar el texto superado.

## Arquitectura, fuentes y límites

LLM interpreta → HttpSession aporta memoria → Spring valida habilitación → n8n consulta/ejecuta → resultados vuelven al LLM → respuesta natural. V4 permanece literal en system-prompt-v4.txt; se envía seguido de la adenda operativa que aclara lecturas frente a efectos. No existe una secuencia obligatoria de captura. Nombre, correo, motivo y selección pueden aportarse/corregirse en cualquier orden.

RAG solo contiene información institucional; no es memoria ni agenda. El corpus actual v1.1 tiene siete secciones y cantidad variable; reprogramación es INST-06. Consultas institucionales limpias usan embeddings OpenAI e índice en memoria. El chat no envía datos personales a embeddings. El LLM recibe memoria/historial necesarios, pero nunca comprobantes, hashes ni claves de autenticación.

Las afirmaciones sobre políticas, servicios o canales requieren recuperación institucional actual. Si el LLM detecta esa necesidad al redactar, solicita consulta_institucional y recibe el fragmento antes de responder. Una política no se inventa ni se transforma en permiso para realizar un trámite no implementado. La recuperación semántica aporta hasta tres fragmentos pertinentes, conservando IDs/versiones. El LLM identifica en fuentes_institucionales los fragmentos usados; Spring comprueba que hayan sido recuperados y conserva la fuente citada en la respuesta técnica; la interfaz no muestra fuentes ni consultas. Las políticas afirmadas requieren fuentes institucionales válidas. Esto no cambia el corpus ni convierte RAG en memoria.

| Información | Responsable | Validación |
| --- | --- | --- |
| Atención, servicios y políticas | RAG/INST-01 y V4 | Agenda no puede ampliar esos permisos. |
| Días y franjas ejecutables | institutional-calendar.json y politica_agenda | Proyecciones de INST-01; revisar ambas al cambiar la política. Spring exige coincidencia del texto de origen. |
| Feriados/cierres fechados | cierres_institucionales, fechas/fuentes aprobadas | n8n excluye estos días. Verificar cobertura; una tabla vacía no acredita que no haya feriados. |
| Cupos y solicitudes actuales | PostgreSQL mediante n8n | Lectura actual más restricciones institucionales. |
| Datos conversados, horarios ofrecidos e IDs | HttpSession | Antecedentes/borrador, no prueba de vigencia ni persistencia. |

INST-01 vigente: lunes a viernes 09:00–18:00, America/Argentina/Buenos_Aires; cerrado sábados, domingos y feriados. La franja completa debe caer en el mismo día y terminar a más tardar a las 18:00. Si n8n devuelve un sábado o una franja fuera de atención, Spring la excluye; además se debe auditar y corregir el dato de demo. No inventar excepciones. Las fechas/hora relativas se resuelven con la fecha local de Buenos Aires; una hora numérica sin minutos significa HH:00 y se compara con el inicio de franja. 10 coincide con 10:00–10:30. Coincidencia única selecciona, múltiples/ninguna requieren aclaración. Un ID anterior no invalida una fecha/hora explícita inequívoca. El hecho seleccion_validada del backend se devuelve al LLM para evitar negar un cupo y ofrecerlo después.

## Lecturas y efectos

| Herramienta | Clase | Habilitación |
| --- | --- | --- |
| disponibilidad | Lectura | Pedido del usuario/necesidad actual interpretada por LLM; sin confirmación de registro previa. |
| consultar_solicitud | Lectura | Intento originado por esta sesión y credenciales internas asociadas. Repetible para estado y datos realmente guardados. |
| registrar | Efecto | Datos válidos completos + cupo institucional válido elegido + resumen presentado antes de este turno + conformidad explícita sobre ese mismo resumen sin cambios. |
| reintentar | Efecto | Petición explícita; primero verificar el intento indeterminado. Solo reenviar si no_encontrada, con datos, UUID y comprobante/hash originales. |

Lecturas pueden repetirse en cualquier turno o dentro de él cuando haga falta. Resultados anteriores no sustituyen una consulta actual. Registrar no se usa para comprobar estado o correo. Seleccionar un cupo, querer reservar o aceptar un motivo no autoriza el envío.

Spring comprueba precondiciones ANTES de invocar un efecto. Un pedido bloqueado no genera llamada ni evidencia registrar fallida; no creó una solicitud. El LLM recibe el motivo del bloqueo y continúa naturalmente. Como máximo un efecto por turno, incluso si el LLM lo repite en plan, herramientas adicionales o acciones anunciadas. Tras un timeout no se reintenta automáticamente en ese mismo turno.

La evidencia solo refleja llamadas ejecutadas: ID, herramienta, clase lectura/efecto, fecha/hora y resultado. Una llamada fallida después del envío puede ser indeterminada: no demuestra ausencia de fila. Ante falta de autorización se retira cualquier anuncio de envío; nunca ejecutar por haberlo prometido. Hasta cuatro rondas internas para resolver herramientas y redactar; si el proveedor insiste incoherentemente, devolver un error técnico sin ejecutar efectos indebidos.

Los IDs de fuentes permitidos se restringen en el esquema de salida a los realmente presentes. Los errores de límite de rondas incluyen códigos de ruta sin texto, datos personales ni secretos, únicamente en la respuesta técnica; el chat muestra el mensaje comprensible. No repetir pedidos ya satisfechos dentro del turno sin una nueva necesidad.

Antes de pedir conformidad y al informar un registro pendiente: «pendiente de confirmación por recepción». Varias pendientes pueden coexistir por horario; solo una confirmada. Recepción resuelve las demás. Una pendiente no consume el cupo. No editar datos persistidos desde un borrador: esa herramienta aún no existe.

## 1. Auditar y corregir la base ficticia en pgAdmin

Conectar a pgends como propietario. Ejecutar primero sql/auditar-horarios.sql: es solo lectura y funciona con las tablas originales. Devuelve ID exacto del horario, inicio/fin locales, habilitación, motivo de inconsistencia y cantidad/IDs de confirmadas asociadas. Esas son las filas reales a revisar; sin su resultado no se puede afirmar que existan sábados específicos. Guardar el resultado con fecha para el informe, sin datos personales.

Instalar sql/calendario-institucional.sql. Crea proyección semanal, tabla de cierres aprobados y función horario_institucional_valido. Comparar politica_agenda con INST-01 e institutional-calendar.json. El script no sobreescribe una política existente ni inventa fechas de feriados. Cargar y verificar cierres/feriados aprobados antes de dar el calendario por validado. Revisar también las filas que la función rechaza.

Ejecutar sql/corregir-horarios-demo.sql para una vista previa transaccional: bloquea y deshabilita cupos futuros incompatibles sin confirmadas; RETURNING muestra IDs y fechas. Termina con ROLLBACK. Revisar esos resultados y luego volver a ejecutar sustituyendo ROLLBACK por COMMIT para aplicar. No elimina ni mueve reservas. Las confirmadas incompatibles salen en otro resultado y requieren recepción; las pendientes se conservan y recepción debe resolverlas. Volver a auditar después de aplicar. No confundir script preparado con corrección ejecutada.

## 2. Actualizar funciones y los workflows existentes

1. Instalar sql/consultar-disponibilidad.sql e instalar/revisar sql/registrar-solicitud.sql después del calendario. Esta versión también valida política institucional dentro de la transacción. Instalar sql/consultar-solicitud.sql si aún no está.
2. En el workflow GET de disponibilidad existente, sustituir la consulta PostgreSQL por n8n/disponibilidad-parametrizada.sql. Respond to Webhook devuelve {{ $json.respuesta }} como objeto JSON. Mantener GET, path los-tilos/disponibilidad, Header Auth X-Los-Tilos-Key y Production URL existentes. La consulta exige futuro, habilitación, política institucional y ausencia de confirmada; no excluye pendientes.
3. En el MISMO workflow POST de solicitudes existente, importar/reemplazar con n8n/registro-solicitudes.json. No publicar otro workflow con el mismo path. Mantener POST, path los-tilos/solicitudes-turno, Production URL y Header Auth existentes. La plantilla es inactiva y no contiene credenciales; elegir las credenciales ya configuradas en el webhook y ambos nodos PostgreSQL.
4. Validar datos enruta a Datos validos y luego Es consulta. Registrar en PostgreSQL ejecuta registrar_solicitud_turno con seis parámetros; Consultar en PostgreSQL ejecuta consultar_solicitud_turno con tres. Mantener Query Parameters como arreglos de la plantilla; no interpolar valores en SQL. Éxito y error deben llegar a sus respectivos Respond to Webhook.
5. Publicar ambas versiones. No sustituir las Production URL por Test URL; esta última requiere Listen for test event y no sirve para uso normal. Desactivar conservación de ejecuciones exitosas/fallidas/manuales con datos personales o secretos; las capturas solo usan datos ficticios sin encabezados ni comprobantes.

Verificación de permisos como propietario:
```sql
SELECT has_function_privilege('los_tilos_n8n', 'los_tilos.registrar_solicitud_turno(bigint,text,text,text,uuid,text)', 'EXECUTE') AS registrar,
       has_function_privilege('los_tilos_n8n', 'los_tilos.consultar_solicitud_turno(uuid,text,text)', 'EXECUTE') AS consultar,
       has_function_privilege('los_tilos_n8n', 'los_tilos.horario_institucional_valido(timestamptz,timestamptz)', 'EXECUTE') AS calendario,
       has_function_privilege('los_tilos_n8n', 'los_tilos.consultar_disponibilidad()', 'EXECUTE') AS disponibilidad;
```

Las funciones tienen SECURITY DEFINER y search_path restringido, pertenecen al propietario de las tablas y revocan PUBLIC. n8n usa los_tilos_n8n con EXECUTE sobre las funciones. La consulta de disponibilidad nueva tampoco necesita SELECT directo de solicitudes (evita ampliar acceso a nombres/correos/motivos); no conceder INSERT/UPDATE directo para registrar. Los permisos de lectura anteriores no se amplían ni revocan automáticamente. READ COMMITTED. Registro serializa por clave de idempotencia y bloquea la fila del horario. Recepción debe bloquear el mismo horario antes de confirmar; índice parcial garantiza una sola confirmada. Los cambios de calendario durante operaciones se coordinan operativamente; no existe automatización de recepción todavía.

## 3. Contratos del mismo POST

Registrar (omitir operacion conserva compatibilidad anterior):
```json
{"operacion":"registrar","horario_id":"2","nombre":"Ana Demo","correo":"ana@example.com","motivo":"Control","clave_idempotencia":"681a1f4d-680a-4d1d-96ed-fb418e313329","comprobante_hash":"64 caracteres hexadecimales en minúscula"}
```
HTTP 201: resultado registrada, solicitud_id, estado pendiente. HTTP 200: ya_registrada, mismo UUID y estado actual. HTTP 400: datos_invalidos. HTTP 409: horario_no_disponible o conflicto_idempotencia. HTTP 500/error/timeout: indeterminado si el envío pudo haberse ejecutado; verificar antes de repetir.

Consultar:
```json
{"operacion":"consultar","clave_idempotencia":"UUID del intento original","correo":"correo asociado al intento","comprobante_hash":"hash SHA-256 original"}
```
HTTP 200: resultado encontrada y solicitud con id, horario_id, nombre, correo, motivo, estado, inicio y fin leídos en PostgreSQL. HTTP 404: no_encontrada sin campos almacenados. Esta lectura no inserta ni actualiza. Workflow antiguo incompatible/fallo no se convierte en éxito desde memoria.

horario_id viaja como texto para conservar bigint. Backend, n8n y función validan. Nombre hasta 120, correo hasta 254 con formato básico, motivo hasta 500; espacios exteriores se eliminan. El UUID y comprobante aleatorio de 32 bytes Base64URL los genera Spring. comprobante_hash es SHA-256 del comprobante codificado UTF-8, hexadecimal minúscula. Solo se muestra el comprobante original tras resultado validado. El modelo no recibe comprobantes/hash/idempotency ni Header Auth.

## 4. Configuración y arranque

Mantener únicamente referencias de entorno en application.properties. Los valores quedan fuera del repositorio y frontend:

- OPENAI_API_KEY
- LOS_TILOS_N8N_WEBHOOK_URL
- LOS_TILOS_N8N_WEBHOOK_KEY
- LOS_TILOS_N8N_REGISTRO_WEBHOOK_URL
- LOS_TILOS_N8N_REGISTRO_WEBHOOK_KEY

No copiar ni inspeccionar valores de application-local para suplir variables del proceso de prueba. El usuario puede autorizar el arranque habitual de Spring con su perfil local ya configurado: es Spring quien carga esa configuración privada; el agente no extrae ni muestra sus valores. Spring necesita estas variables en SU proceso, además de la conexión existente de la aplicación. Render y la terminal local tienen entornos distintos.

Desde miportafolio-fase3, con la configuración existente de conexión local y variables del servidor:
```powershell
mvn spring-boot:run '-Dspring-boot.run.profiles=local'
```
Abrir http://localhost:8080/asistente/index.html y recargar archivos. Para tests automatizados: mvn test (sin proveedores reales). En equipos con poca memoria puede ejecutarse mvn '-DforkCount=0' test con heap acotado del proceso Maven.

## 5. Prueba integrada real y evidencia

Con los workflows/SQL publicados y Spring ejecutando el código actual, ejecutar en otra terminal:
```powershell
powershell -NoProfile -File docs/asistente-fase3/pruebas/integrada-real.ps1
```
El script habla solo con Spring y usa cookie de una sesión; no lee secretos ni properties. Informa el paso que falló y, si existe, los códigos de ruta sanitizados del backend. Puede recibir -BaseUrl para un entorno de demo autorizado. Crea una solicitud ficticia si hay cupo y el consentimiento se procesa; no borra filas. Se detiene sin declarar aprobación si falta servidor, proveedor, cupo o el contrato publicado. Conserva salida de UUIDs de ejecución/solicitud y resultados, sin comprobante.

Comprobar desde la página, en la misma sesión:
1. Consulta institucional de reprogramación → INST-06. Consultar disponibilidad y volver a consultarla con otro criterio → llamadas de lectura nuevas.
2. Elegir una franja real (si existe 10:00–10:30, decir «a las 10») → selección exacta, sin afirmar que no existe. Usar fecha si hay más de una coincidencia. Verificar referencias relativas con reloj de Buenos Aires.
3. Aportar datos ficticios en cualquier orden. Ni intención, selección ni resumen deben producir un registrar, ni siquiera una línea registrar fallida.
4. Ver resumen y aviso de recepción. Corregir un dato junto con «confirmo» → nuevo resumen, cero escrituras hasta una conformidad posterior sobre ese resumen.
5. Confirmar → como máximo una llamada con efecto y un UUID real pendiente. Consultar después estado/correo → nuevas lecturas; jamás registrar para comprobar.
6. Cambiar un borrador y pedir verificar lo guardado → leer datos reales y distinguirlos del borrador. Editar un registro persistido sigue pendiente.
7. En un workflow de prueba aislado, demorar respuesta tras INSERT más del timeout. Resultado indeterminado; no reintento automático en el mismo turno. Verificar por lectura y, si la persona lo pide, reintentar en otro turno con el mismo payload/clave. Confirmar una sola fila por clave. No introducir demoras en el workflow normal.
8. Auditar la tabla de horarios y contrastar que ningún cupo incompatible llegue al chat; repetir auditoría tras corregir datos. Si hay confirmadas incompatibles, recepción debe resolverlas.

Comprobar la fila como propietario en pgAdmin (UUID mostrado):
```sql
SELECT id,horario_id,nombre,correo,motivo,estado,creada_en
FROM los_tilos.solicitudes_turno WHERE id = '<UUID de la demo>'::uuid;
```
Para idempotencia transaccional sin dejar filas, BEGIN, llamar dos veces la función con idénticos parámetros y ROLLBACK: mismo UUID. Datos/hash diferentes con la misma clave → conflicto. Concurrencia real requiere dos conexiones; una pendiente no bloquea otra pendiente, mientras una confirmada impide solicitudes nuevas. No afirmar estas pruebas como realizadas si solo se ejecutaron dobles de proveedor.

Evidencia: fecha local, rama/commit, versión V4/adenda/corpus, caso, modo automatizado o real, esperado/observado, llamada/UUID y fila cuando aplique. Capturar resumen previo, resultado pendiente y auditoría sin secretos/comprobantes/datos reales. Resultado real y corrección SQL son pasos separados.

## 6. Sesión, duplicados y pendientes

Datos corregidos invalidan la conformidad. Un intento indeterminado no se reemplaza; se conserva aunque el borrador cambie. Una repetición HTTP de confirmación ya resuelta devuelve el resultado local del envío sin escribir otra vez; NO acredita estado externo actual, para eso se llama consultar_solicitud. Un reintento explícito indeterminado consulta primero n8n: encontrada recupera el comprobante; no_encontrada permite reenviar el payload original; una consulta fallida impide reenviar. La concurrencia de la misma sesión se serializa; idempotencia SQL protege una sola fila incluso si el cliente repite tras perder la respuesta.

HttpSession expira a 30 minutos de inactividad; historial de 40 mensajes y memoria estructurada. Misma cookie permite recargar. Reinicio, expiración o pérdida de cookie elimina el comprobante original, que no puede reconstruirse desde su hash. No hay recuperación entre sesiones. Guardarlo en privado al recibirlo; no publicarlo en capturas. Operación de demo con una instancia; múltiples instancias requieren sesión compartida.

Pendientes: recuperación independiente con correo/comprobante, edición de registros, automatización de confirmación por recepción, purga a siete días y protocolo clínico profesional aprobado. La cobertura de feriados requiere carga/verificación explícita. La disponibilidad semanal filtrada no prueba que esos pendientes estén resueltos.

Si el borrador afirma una política sin fragmentos recuperados, el backend pide al LLM elegir un título del corpus institucional vigente. El esquema de salida y el backend restringen la elección a esos títulos; únicamente ese texto institucional se usa como consulta semántica. No se construye una consulta libre a partir de síntomas o datos personales. Se ejecuta RAG y se devuelven los fragmentos antes de redactar nuevamente. No se repite solo una orden de citar fuentes inexistentes. Las solicitudes de contexto institucional y de herramientas de un mismo borrador se procesan antes de validar el texto final; una validación provisional no debe bloquear las lecturas. La reparación automática de consulta ocurre como máximo una vez por turno y no aumenta el límite de cuatro rondas ni habilita escrituras.

## Continuación semántica y llamadas ya resueltas (adenda v3)

Usuario → LLM interpreta → herramientas según necesidad → resultados → LLM continúa interpretando → otras herramientas si hacen falta → respuesta natural. Spring ejecuta, conserva memoria y valida seguridad; no clasifica frases del usuario ni impone una secuencia de preguntas.

El registro de lecturas pertenece únicamente al mensaje actual, nunca a toda la sesión. Identifica herramienta, argumentos reales e intento de escritura intermedio. Una lectura idéntica ya resuelta no se envía otra vez; se aporta su resultado y su ID al LLM. Los fallos tampoco se reintentan automáticamente. Otro mensaje comienza con lecturas nuevas; otra solicitud o una escritura intermedia permite otra lectura en el mismo mensaje. Los esquemas enviados al modelo excluyen únicamente las peticiones ya resueltas y los efectos sin autorización, conservando las demás herramientas disponibles.

El GET de disponibilidad actual devuelve el catálogo completo de cupos, sin parámetros de fecha. Dentro de un mismo mensaje, distintas preferencias se interpretan sobre esa consulta actual. Si el usuario cambia de fecha en otro mensaje, el LLM puede pedir una nueva lectura real. Una actualización de nombre/correo no exige refrescar la lista. Las opciones de memoria se describen como previamente ofrecidas, sin afirmar vigencia actual hasta consultar. La ausencia de cupos para una fecha no significa cierre institucional.

Las consultas RAG idénticas dentro del mensaje reutilizan su contexto; nuevas consultas acumulan fuentes para que el LLM pueda fundamentar distintos temas. No se reemplaza el contexto anterior por un único fragmento genérico. El LLM debe elegir fuentes pertinentes a sus afirmaciones; un fragmento de horarios no respalda una recomendación clínica. Sin protocolo profesional aprobado, no recomendar guardias ni clasificar urgencia mediante intensidad del dolor o fiebre; mantener el límite administrativo y el contacto institucional.

Se conserva el límite técnico de cuatro rondas. No se añade finishOnly ni se aumenta a ocho. Una petición repetida no se considera información nueva ni obliga a otra ronda por sí sola. Los efectos conservan datos válidos, horario seleccionado, resumen previo, conformidad explícita e idempotencia; como máximo uno por mensaje. Las pruebas manuales quedan a cargo del usuario: este cambio no acredita una prueba integrada ejecutada.


## Recuperación de errores en Fase 4

La política vigente se documenta en [resiliencia.md](../asistente-fase4/resiliencia.md). El reintento de un registro indeterminado ahora verifica obligatoriamente en n8n antes de reenviar; las instrucciones de reintento anteriores se mantienen alineadas con esta verificación. No modifica SQL ni el workflow publicado.
