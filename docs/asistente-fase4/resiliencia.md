# Fase 4: recuperación de errores

El botón Reintentar aparece en el encabezado del compositor, a la derecha de Tu consulta. Recupera chat, lecturas fallidas de herramientas, restauración y cierre de sesión. También se ofrece cuando HTTP 200 contiene evidencia de una herramienta fallida o un registro indeterminado. No duplica el mensaje del usuario. Durante el envío bloquea doble clic y muestra carga; otro fallo conserva la opción.

Cada mensaje lleva un UUID de consulta distinto de la clave de idempotencia de registro. HttpSession retiene únicamente el último turno, su plan, operaciones, resultados, recuperación institucional y respuesta completada. El mismo UUID con distinto texto se rechaza. Un reintento de un turno anterior tras una nueva consulta se rechaza. Si la petición inicial nunca llegó al servidor, puede ejecutarse por primera vez. La pérdida/expiración de sesión no permite recuperar el comprobante ni el consentimiento anterior; hay que reconstruir la preparación sin inferir un registro nuevo.

El LLM mantiene interpretación y redacción; no se aumentan las cuatro rondas ni se agregan reglas por frases. La recuperación técnica ejecuta las operaciones fallidas ya elegidas por el LLM. Resultados exitosos del turno se conservan. La respuesta completada se reutiliza para un envío duplicado del mismo UUID, sin duplicar historial ni escrituras. Cada mensaje nuevo sigue pudiendo consultar las fuentes vivas.

Registro inicial: siguen vigentes datos, selección válida, resumen previo y conformidad explícita. Un fallo posterior de generación no vuelve a ejecutar una escritura completada. Las respuestas de error del chat incluyen el intento actual de esa sesión para mostrar el comprobante incluso si OpenAI no pudo redactar. Para estado indeterminado, consultar con la clave, correo y hash originales: encontrada recupera el comprobante original; no_encontrada permite reenviar los mismos datos, clave y hash; cualquier error de consulta impide el reenvío. Una consulta concurrente que todavía no vea la fila no rompe la seguridad: el reenvío conserva la clave única y la función transaccional de PostgreSQL resuelve la idempotencia. El bloqueo por sesión serializa peticiones de la misma sesión.

Errores de validación, datos rechazados o horarios ocupados pueden necesitar correcciones: el botón no omite validaciones ni fuerza un registro. No hay reintentos automáticos ni recursivos. Un registro rechazado requiere retomar la conversación y corregir su causa.

Logs: operación, UUID técnico, clase de error y código HTTP; los clientes n8n ya registran duración y resultado. Nunca registrar texto del paciente, correo, comprobante, hashes ni credenciales. El navegador comunica solo una categoría permitida a client-error; es best effort, imposible durante una desconexión total. No se alteran SQL ni n8n ni se requiere nueva configuración.

Pruebas acotadas con fallos controlados, sin proveedores reales:

- `node --test src/test/js/assistant-retry.test.cjs`: dos casos, OpenAI con doble clic y lectura n8n seguida de fallo de red.
- `mvn -Dtest=AssistantRecoveryTest test`: cuatro casos, fallo de OpenAI y lectura n8n recuperados sin duplicados de historial, registro indeterminado verificado antes de reenviar, registro persistido sin otra escritura y verificación fallida que impide reenvío.

Estos tests no constituyen una prueba integrada con OpenAI o n8n Cloud reales. El protocolo del webhook existente no cambia.
