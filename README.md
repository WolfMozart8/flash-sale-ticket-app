# Flash Sale Lab (Spring Boot) (WIP)

Proyecto de practica para simular una venta relampago de tickets y experimentar con tecnologias de backend en escenarios reales de concurrencia.

## Objetivo

Construir un laboratorio pequeno, incremental y facil de romper, para aprender haciendo sobre:

- Redis para locks temporales (evitar doble reserva)
- Kafka para procesos asincronos post-compra
- PostgreSQL + PL/pgSQL para logica transaccional en base de datos
- Tests de integracion con Testcontainers
- Buenas practicas de API y manejo de errores en Spring Boot

Este proyecto esta en fase temprana. La idea no es que este "terminado", sino que sirva como sandbox para probar y comparar herramientas.

## Stack actual

- Java 17
- Spring Boot 3.5
- Spring Web + Spring Data JPA
- Redis
- Apache Kafka
- PostgreSQL
- Flyway (migraciones SQL)
- JUnit 5 + Testcontainers
- Docker Compose

## Flujo actual de negocio

1. Un usuario intenta bloquear un ticket por 5 minutos (`Redis setIfAbsent` + TTL).
2. Si confirma la compra, se ejecuta un procedimiento almacenado en PostgreSQL (`confirmar_compra`).
3. Si la compra sale bien, se libera el lock en Redis y se publica un evento en Kafka.
4. Un consumidor Kafka simula un proceso desacoplado (ej: envio de confirmacion).

## API disponible hoy

### Tickets

- `GET /api/tickets/mock`: crea 3 tickets de ejemplo.
- `GET /api/tickets/all`: lista tickets (cacheable en Redis).
- `GET /api/tickets/ticket/{id}`: obtiene ticket por id.

### Reservas

- `POST /api/reservations/lock?ticketId={id}&userId={user}`: reserva temporal.
- `POST /api/reservations/confirm?ticketId={id}&userId={user}`: confirma compra.

### Errores de prueba

- `GET /api/errors/runtime`
- `GET /api/errors/illegal`

## Como levantar el proyecto

### Requisitos

- Java 17
- Docker Desktop

### 1) Levantar infraestructura

```bash
docker compose up -d
```

Servicios esperados:

- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`
- Kafka: `localhost:9092`

### 2) Ejecutar la app

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

En Linux/macOS:

```bash
./mvnw spring-boot:run
```

### 3) Correr tests

En Windows:

```powershell
.\mvnw.cmd test
```

En Linux/macOS:

```bash
./mvnw test
```

## Prueba rapida (manual)

1. Crear datos:

```bash
curl -X GET http://localhost:8080/api/tickets/mock
```

2. Reservar ticket:

```bash
curl -X POST "http://localhost:8080/api/reservations/lock?ticketId=1&userId=user-1"
```

3. Confirmar compra:

```bash
curl -X POST "http://localhost:8080/api/reservations/confirm?ticketId=1&userId=user-1"
```

## Estructura clave

- `src/main/java/.../controller`: endpoints REST
- `src/main/java/.../service`: reglas de reserva, confirmacion y eventos
- `src/main/java/.../repository`: JPA + llamadas a procedimientos SQL
- `src/main/resources/db/migration`: scripts Flyway (tabla y procedimientos)
- `src/test/java/...`: tests de integracion y concurrencia

## Roadmap corto

- [ ] Endpoint para devolucion de ticket
- [ ] Productor/consumidor Kafka con payload estructurado (JSON)
- [ ] Observabilidad basica (logs estructurados y metricas)
- [ ] Pruebas de carga para lock concurrente
- [ ] Versionar contratos API (OpenAPI/Swagger)

## Nota

Si quieres usar este repo para aprender, lo ideal es trabajar por ramas pequenas:

1. Tomas un tema (ej: solo Redis lock).
2. Agregas test que falle.
3. Implementas.
4. Refactorizas.

Ese ciclo es la esencia de este laboratorio.
