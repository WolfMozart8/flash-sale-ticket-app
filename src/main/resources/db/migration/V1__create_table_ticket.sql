CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    event_name VARCHAR(255) NOT NULL,
    status VARCHAR(100) NOT NULL,
    price NUMERIC(19, 2) NOT NULL
);
