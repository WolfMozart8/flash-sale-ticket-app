CREATE OR REPLACE PROCEDURE confirmar_compra(
    p_ticket_id IN BIGINT,
    p_usuario_id IN VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
    -- 1. Intentamos actualizar el estado usando un UPDATE clásico
UPDATE tickets
SET status = 'VENDIDO'
WHERE id = p_ticket_id AND status = 'DISPONIBLE';

-- 2. "FOUND" es una variable especial de Postgres.
-- Si el UPDATE anterior afectó a 0 filas, FOUND será falso.
IF NOT FOUND THEN
        RAISE EXCEPTION 'Error: El ticket % ya no está disponible o no existe', p_ticket_id;
END IF;

    -- Aquí (si tuviéramos una tabla de "Ventas") haríamos el INSERT:
    -- INSERT INTO ventas (ticket_id, usuario_id, fecha) VALUES (p_ticket_id, p_usuario_id, NOW());

    -- Si llegamos hasta aquí sin errores, Postgres guarda los cambios automáticamente.
END;
$$;