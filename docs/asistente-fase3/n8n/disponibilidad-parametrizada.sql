-- Nodo PostgreSQL del workflow GET existente; mantiene path y Header Auth.
-- Instalar antes sql/calendario-institucional.sql y sql/consultar-disponibilidad.sql.
SELECT los_tilos.consultar_disponibilidad() AS respuesta;
-- Respond to Webhook: {{ $json.respuesta }} como objeto JSON {horarios: []}.
