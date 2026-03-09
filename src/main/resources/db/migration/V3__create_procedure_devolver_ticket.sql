CREATE OR REPLACE PROCEDURE devolver_ticket(
       p_ticket_id IN BIGINT
)

LANGUAGE plpgsql
AS $$
BEGIN

UPDATE tickets
SET status = 'DISPONIBLE'
WHERE id = p_ticket_id AND status = 'VENDIDO';

IF NOT FOUND THEN
       RAISE EXCEPTION 'Error: El ticket % aún no está vendido', p_ticket_id;
END IF;

END;
$$;
CREATE OR REPLACE PROCEDURE devolver_ticket(
       p_ticket_id IN BIGINT
)

LANGUAGE plpgsql
AS $$
BEGIN

UPDATE tickets
SET status = 'DISPONIBLE'
WHERE id = p_ticket_id AND status = 'VENDIDO';

IF NOT FOUND THEN
       RAISE EXCEPTION 'Error: El ticket % aún no está vendido', p_ticket_id;
END IF;

END;
$$;
