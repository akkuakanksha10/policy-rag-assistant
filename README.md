# Event-Driven Policy & Compliance RAG Assistant (Step 1: Kafka + DLQ)

Upgrades the Java 25 + Spring Boot + LangChain4j + pgvector + Ollama RAG assistant with **asynchronous Apache Kafka (KRaft) ingestion**, partition-ordered publishing, automatic retries, and a **Dead-Letter Queue (`document.uploaded.dlq`)**.

## Event-Driven Flow
1. `POST /api/ingest/async` validates the payload, publishes a `DocumentUploadedEvent` keyed by `sourceName` to `document.uploaded` (3 partitions), and returns `202 Accepted` immediately.
2. `DocumentIngestionConsumer` (`rag-ingestion-workers`) consumes the event, chunks the policy, generates embeddings via Ollama (`nomic-embed-text`), and stores them in `pgvector`.
3. If processing fails, `DefaultErrorHandler` retries twice (2s backoff) and automatically routes the failed message to `document.uploaded.dlq`, where the DLQ auditor logs the failure reason.

## Step-by-Step Verification (PowerShell)

### 1. Start Kafka alongside your existing `pgvector` container
```powershell
docker compose up -d
```
*(Your `rag-pgvector` container and volume remain untouched; `rag-kafka` starts in KRaft mode capped at 384 MB heap.)*

### 2. Reload Maven in IntelliJ and restart `PolicyRagApplication`

### 3. Queue all 3 sample policies asynchronously via Kafka (`202 Accepted`)
```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/ingest/samples/async" | ConvertTo-Json -Depth 5
```
Watch the IntelliJ console show `Published -> Consuming -> Indexed` for all 3 documents.

### 4. Test the Dead-Letter Queue (DLQ) live
```powershell
$poison = @{
    sourceName = "corrupt-policy.txt"
    department = "LEGAL"
    accessLevel = "PUBLIC"
    content = "SIMULATE_FAILURE corrupt payload"
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/ingest/async" -ContentType "application/json" -Body $poison | ConvertTo-Json
```
Watch the console retry twice (2s apart) and then print `DLQ ALERT -> eventId=... source=corrupt-policy.txt`.
