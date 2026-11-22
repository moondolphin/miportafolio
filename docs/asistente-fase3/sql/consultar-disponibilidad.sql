-- Instalar después de calendario-institucional.sql como propietario de las tablas en pgends.
-- n8n no necesita SELECT directo de los datos personales de solicitudes.
BEGIN;
CREATE OR REPLACE FUNCTION los_tilos.consultar_disponibilidad()
RETURNS jsonb LANGUAGE sql STABLE SECURITY DEFINER
SET search_path = pg_catalog, pg_temp
AS $f$
    SELECT jsonb_build_object('horarios',coalesce(jsonb_agg(jsonb_build_object(
      'id',h.id::text,'inicio',h.inicio,'fin',h.fin) ORDER BY h.inicio), '[]'::jsonb))
    FROM los_tilos.horarios h
    WHERE h.habilitado AND h.inicio > statement_timestamp()
      AND los_tilos.horario_institucional_valido(h.inicio,h.fin)
      AND NOT EXISTS (SELECT 1 FROM los_tilos.solicitudes_turno s
                      WHERE s.horario_id=h.id AND s.estado='confirmada');
$f$;
REVOKE ALL ON FUNCTION los_tilos.consultar_disponibilidad() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION los_tilos.consultar_disponibilidad() TO los_tilos_n8n;
COMMIT;
