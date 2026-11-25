package com.enterprise.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rag")
public record RagProperties(
        Kafka kafka,
        Ollama ollama,
        PgVector pgvector,
        Retrieval retrieval
) {
    public record Kafka(
            String ingestTopic,
            String dlqTopic,
            int retryAttempts,
            long retryBackoffMs
    ) {}

    public record Ollama(
            String baseUrl,
            String chatModel,
            String embeddingModel,
            double temperature,
            int timeoutSeconds
    ) {}

    public record PgVector(
            String host,
            int port,
            String database,
            String user,
            String password,
            String table,
            int dimension
    ) {}

    public record Retrieval(
            int maxResults,
            double minScore
    ) {}
}
