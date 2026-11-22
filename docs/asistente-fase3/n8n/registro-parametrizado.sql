SELECT los_tilos.registrar_solicitud_turno(
    $1::bigint, $2::text, $3::text, $4::text, $5::uuid, $6::text
) AS respuesta;
