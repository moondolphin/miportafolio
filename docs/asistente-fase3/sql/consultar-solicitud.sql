-- Ejecutar en pgends como propietario de las tablas.
BEGIN;

CREATE OR REPLACE FUNCTION los_tilos.consultar_solicitud_turno(
    p_clave_idempotencia uuid,
    p_correo text,
    p_comprobante_hash text
)
RETURNS jsonb
LANGUAGE plpgsql
STABLE
SECURITY DEFINER
SET search_path = pg_catalog, pg_temp
AS $func$
DECLARE
    v_resultado jsonb;
BEGIN
    SELECT jsonb_build_object(
        'resultado', 'encontrada',
        'solicitud', jsonb_build_object(
            'id', s.id,
            'horario_id', s.horario_id::text,
            'nombre', s.nombre,
            'correo', s.correo,
            'motivo', s.motivo,
            'estado', s.estado,
            'inicio', h.inicio,
            'fin', h.fin
        )
    )
    INTO v_resultado
    FROM los_tilos.solicitudes_turno AS s
    JOIN los_tilos.horarios AS h ON h.id = s.horario_id
    WHERE s.clave_idempotencia = p_clave_idempotencia
      AND s.correo = btrim(p_correo)
      AND s.comprobante_hash = p_comprobante_hash;

    RETURN coalesce(v_resultado, jsonb_build_object('resultado', 'no_encontrada'));
END;
$func$;

REVOKE ALL ON FUNCTION los_tilos.consultar_solicitud_turno(uuid, text, text) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION los_tilos.consultar_solicitud_turno(uuid, text, text) TO los_tilos_n8n;
COMMIT;
