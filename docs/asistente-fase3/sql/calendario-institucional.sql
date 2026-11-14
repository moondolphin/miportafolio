-- Ejecutar como propietario de las tablas en pgends. Proyección de INST-01 v1.1.
-- Los feriados/cierres se cargan con fechas y fuente aprobadas, no se inventan.
BEGIN;
CREATE TABLE IF NOT EXISTS los_tilos.politica_agenda (
    id boolean PRIMARY KEY DEFAULT true CHECK (id),
    version text NOT NULL,
    zona text NOT NULL,
    dias integer[] NOT NULL,
    apertura time NOT NULL,
    cierre time NOT NULL CHECK (cierre > apertura)
);
INSERT INTO los_tilos.politica_agenda(id,version,zona,dias,apertura,cierre)
VALUES(true,'INST-01/v1.1-calendario1','America/Argentina/Buenos_Aires',ARRAY[1,2,3,4,5],'09:00','18:00')
ON CONFLICT (id) DO NOTHING; -- No sobreescribir una configuración existente sin revisión.

CREATE TABLE IF NOT EXISTS los_tilos.cierres_institucionales (
    fecha date PRIMARY KEY,
    motivo text NOT NULL,
    fuente text NOT NULL
);
CREATE OR REPLACE FUNCTION los_tilos.horario_institucional_valido(p_inicio timestamptz, p_fin timestamptz)
RETURNS boolean LANGUAGE sql STABLE SECURITY DEFINER
SET search_path = pg_catalog, pg_temp
AS $f$
    SELECT coalesce((SELECT
        p_fin > p_inicio
        AND (p_inicio AT TIME ZONE r.zona)::date = (p_fin AT TIME ZONE r.zona)::date
        AND extract(isodow FROM p_inicio AT TIME ZONE r.zona)::integer = ANY(r.dias)
        AND (p_inicio AT TIME ZONE r.zona)::time >= r.apertura
        AND (p_fin AT TIME ZONE r.zona)::time <= r.cierre
        AND NOT EXISTS (SELECT 1 FROM los_tilos.cierres_institucionales c
                        WHERE c.fecha = (p_inicio AT TIME ZONE r.zona)::date)
        FROM los_tilos.politica_agenda r WHERE r.id), false);
$f$;
REVOKE ALL ON TABLE los_tilos.politica_agenda, los_tilos.cierres_institucionales FROM PUBLIC;
REVOKE ALL ON FUNCTION los_tilos.horario_institucional_valido(timestamptz,timestamptz) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION los_tilos.horario_institucional_valido(timestamptz,timestamptz) TO los_tilos_n8n;
COMMIT;

-- Revisar que esta fila coincida con INST-01 y institutional-calendar.json.
SELECT version,zona,dias,apertura,cierre FROM los_tilos.politica_agenda;
-- Cargar aquí solo fechas de cierre aprobadas (incluidos feriados); tabla vacía NO acredita cobertura.
SELECT fecha,motivo,fuente FROM los_tilos.cierres_institucionales ORDER BY fecha;
