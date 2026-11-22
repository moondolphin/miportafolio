-- SOLO base ficticia pgends. Ejecutar primero la auditoría y revisar los IDs.
-- Requiere calendario-institucional.sql. Vista previa transaccional; no elimina ni mueve reservas.
BEGIN;
SELECT id,inicio,fin,habilitado FROM los_tilos.horarios
WHERE habilitado AND inicio > clock_timestamp()
  AND NOT los_tilos.horario_institucional_valido(inicio,fin)
FOR UPDATE;

UPDATE los_tilos.horarios h SET habilitado=false
WHERE h.habilitado AND h.inicio > clock_timestamp()
  AND NOT los_tilos.horario_institucional_valido(h.inicio,h.fin)
  AND NOT EXISTS (SELECT 1 FROM los_tilos.solicitudes_turno s
                  WHERE s.horario_id=h.id AND s.estado='confirmada')
RETURNING h.id,h.inicio,h.fin,h.habilitado;

-- Estos casos requieren recepción: no deshabilitar, mover ni cancelar automáticamente una confirmada.
SELECT h.id,h.inicio,h.fin,s.id AS solicitud_confirmada
FROM los_tilos.horarios h JOIN los_tilos.solicitudes_turno s ON s.horario_id=h.id AND s.estado='confirmada'
WHERE NOT los_tilos.horario_institucional_valido(h.inicio,h.fin);
ROLLBACK; -- Tras revisar RETURNING y las confirmadas, volver a ejecutar sustituyendo por COMMIT.
-- Las pendientes asociadas se conservan y requieren resolución de recepción.
