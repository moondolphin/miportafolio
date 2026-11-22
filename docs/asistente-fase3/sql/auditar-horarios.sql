-- SOLO LECTURA; funciona con las dos tablas originales, antes de instalar el calendario.
-- Devuelve los IDs exactos incompatibles con la regla recurrente actual de INST-01.
WITH locales AS (
 SELECT h.*, h.inicio AT TIME ZONE 'America/Argentina/Buenos_Aires' AS inicio_local,
        h.fin AT TIME ZONE 'America/Argentina/Buenos_Aires' AS fin_local
 FROM los_tilos.horarios h
), inconsistentes AS (
 SELECT l.*, CASE
   WHEN extract(isodow FROM inicio_local) IN (6,7) THEN 'sábado/domingo: cerrado'
   WHEN inicio_local::date <> fin_local::date THEN 'franja cruza fecha local'
   WHEN inicio_local::time < time '09:00' OR fin_local::time > time '18:00' THEN 'fuera de 09:00–18:00'
   WHEN fin <= inicio THEN 'duración inválida'
 END AS inconsistencia
 FROM locales l
)
SELECT h.id,h.inicio_local,h.fin_local,h.habilitado,h.inconsistencia,
       count(s.id) FILTER (WHERE s.estado='pendiente') AS pendientes,
       count(s.id) FILTER (WHERE s.estado='confirmada') AS confirmadas,
       coalesce(array_agg(s.id) FILTER (WHERE s.estado='confirmada'),ARRAY[]::uuid[]) AS solicitudes_confirmadas
FROM inconsistentes h LEFT JOIN los_tilos.solicitudes_turno s ON s.horario_id=h.id
WHERE h.inconsistencia IS NOT NULL
GROUP BY h.id,h.inicio_local,h.fin_local,h.habilitado,h.inconsistencia ORDER BY h.inicio_local,h.id;
-- Los IDs de este resultado son las filas a revisar. No confundir este informe con disponibilidad.
-- Tras instalar calendario-institucional.sql, auditar también feriados/cierres y divergencias de configuración:
-- SELECT id,inicio,fin,habilitado FROM los_tilos.horarios
-- WHERE NOT los_tilos.horario_institucional_valido(inicio,fin) ORDER BY inicio;
