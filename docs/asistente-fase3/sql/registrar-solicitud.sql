-- Ejecutar sobre pgends como propietario de las tablas.
-- Instalar antes calendario-institucional.sql. Comparar la definición instalada antes de reemplazarla.
BEGIN;

CREATE OR REPLACE FUNCTION los_tilos.registrar_solicitud_turno(
    p_horario_id bigint,
    p_nombre text,
    p_correo text,
    p_motivo text,
    p_clave_idempotencia uuid,
    p_comprobante_hash text
)
RETURNS jsonb
LANGUAGE plpgsql
VOLATILE
SECURITY DEFINER
SET search_path = pg_catalog, pg_temp
AS $func$
DECLARE
    v_nombre text := btrim(p_nombre);
    v_correo text := btrim(p_correo);
    v_motivo text := btrim(p_motivo);
    v_inicio timestamptz;
    v_fin timestamptz;
    v_habilitado boolean;
    v_existente los_tilos.solicitudes_turno%ROWTYPE;
    v_solicitud_id uuid;
    v_estado text;
BEGIN
    IF p_horario_id IS NULL OR p_horario_id <= 0
       OR p_clave_idempotencia IS NULL
       OR p_comprobante_hash IS NULL
       OR p_comprobante_hash !~ '^[0-9a-f]{64}$'
       OR v_nombre IS NULL OR char_length(v_nombre) NOT BETWEEN 1 AND 120
       OR v_nombre !~ '[^[:space:]]'
       OR v_correo IS NULL OR char_length(v_correo) NOT BETWEEN 3 AND 254
       OR v_correo !~ '^[^[:space:]@]+@[^[:space:]@]+[.][^[:space:]@]+$'
       OR v_motivo IS NULL OR char_length(v_motivo) NOT BETWEEN 1 AND 500
       OR v_motivo !~ '[^[:space:]]'
    THEN
        RETURN jsonb_build_object('resultado', 'datos_invalidos');
    END IF;

    PERFORM pg_advisory_xact_lock(hashtextextended(p_clave_idempotencia::text, 482017));

    SELECT s.* INTO v_existente
    FROM los_tilos.solicitudes_turno AS s
    WHERE s.clave_idempotencia = p_clave_idempotencia;

    IF FOUND THEN
        IF v_existente.horario_id IS DISTINCT FROM p_horario_id
           OR v_existente.nombre IS DISTINCT FROM v_nombre
           OR v_existente.correo IS DISTINCT FROM v_correo
           OR v_existente.motivo IS DISTINCT FROM v_motivo
           OR v_existente.comprobante_hash IS DISTINCT FROM p_comprobante_hash
        THEN
            RETURN jsonb_build_object('resultado', 'conflicto_idempotencia');
        END IF;
        RETURN jsonb_build_object(
            'resultado', 'ya_registrada',
            'solicitud_id', v_existente.id,
            'estado', v_existente.estado
        );
    END IF;

    SELECT h.inicio, h.fin, h.habilitado INTO v_inicio, v_fin, v_habilitado
    FROM los_tilos.horarios AS h
    WHERE h.id = p_horario_id
    FOR UPDATE;

    IF NOT FOUND OR NOT v_habilitado OR v_inicio <= clock_timestamp()
       OR NOT los_tilos.horario_institucional_valido(v_inicio,v_fin) THEN
        RETURN jsonb_build_object('resultado', 'horario_no_disponible');
    END IF;

    IF EXISTS (
        SELECT 1 FROM los_tilos.solicitudes_turno AS s
        WHERE s.horario_id = p_horario_id AND s.estado = 'confirmada'
    ) THEN
        RETURN jsonb_build_object('resultado', 'horario_no_disponible');
    END IF;

    INSERT INTO los_tilos.solicitudes_turno AS s (
        horario_id, nombre, correo, motivo, clave_idempotencia, comprobante_hash
    ) VALUES (
        p_horario_id, v_nombre, v_correo, v_motivo, p_clave_idempotencia, p_comprobante_hash
    )
    RETURNING s.id, s.estado INTO v_solicitud_id, v_estado;

    RETURN jsonb_build_object(
        'resultado', 'registrada',
        'solicitud_id', v_solicitud_id,
        'estado', v_estado
    );
END;
$func$;

REVOKE ALL ON FUNCTION los_tilos.registrar_solicitud_turno(
    bigint, text, text, text, uuid, text
) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION los_tilos.registrar_solicitud_turno(
    bigint, text, text, text, uuid, text
) TO los_tilos_n8n;
COMMIT;
