# Kafka Integration Service

A minimal, production-ready integration service demonstrating **REST API -> Kafka -> Consumer -> PostgreSQL** flow with Error Handling (DLQ) and Idempotency.

## What it does
1.  **integration-api**: Receives JSON events via REST and publishes them to Kafka.
2.  **event-processor**: Consumes events, validates them, and idempotently saves to PostgreSQL. Failed messages are sent to a Dead Letter Queue (DLQ).

## Architecture
```
[Client] -> POST /events -> [Integration API] -> (Topic: events.v1) -> [Event Processor] -> [PostgreSQL]
                                                                             |
                                                                          (Error)
                                                                             v
                                                                      (Topic: events.dlq)
```

## How to Run

### Prerequisites
*   Docker & Docker Compose
*   Java 17+ & Maven

### 1. Start Infrastructure
```bash
docker-compose up -d
```
*Wait a few seconds for Kafka and Postgres to be ready.*

### 2. Start Services
Open two terminals:

**Terminal 1 (Producer):**
```bash
cd integration-api
mvn spring-boot:run
```

**Terminal 2 (Consumer):**
```bash
cd event-processor
mvn spring-boot:run
```

## Test It

### 1. Send Valid Event
```bash
curl -X POST http://localhost:8080/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "evt-001",
    "type": "ORDER_CREATED",
    "payload": { "amount": 100, "currency": "USD" }
  }'
```
*   **Result**: 
    *   API returns 200 OK.
    *   Consumer logs: `Successfully processed event: evt-001`.
    *   DB: Row inserted in `processed_events`.

### 2. Test Idempotency (Send same event again)
Run the same curl command again.
*   **Result**: 
    *   API returns 200 OK.
    *   Consumer logs: `Event evt-001 already processed. Skipping.`

### 3. Failure & DLQ Demo (Send Invalid Event)
Send an event without `type`:
```bash
curl -X POST http://localhost:8080/events \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "evt-error-1",
    "payload": { "reason": "missing type" }
  }'
```
*   **Result**: 
    *   Consumer logs error and sends to DLQ.
    *   **Verify DLQ**:
        You can consume the DLQ topic to see the error message:
        ```bash
        docker exec -it kafka kafka-console-consumer.sh \
          --bootstrap-server localhost:9092 \
          --topic events.dlq \
          --from-beginning
        ```
        You will see a JSON with `originalMessage` and `error` details.

## CI/CD
*   GitHub Actions workflow located in `.github/workflows/ci.yml`.
*   Runs `mvn package` for both services on every push.
