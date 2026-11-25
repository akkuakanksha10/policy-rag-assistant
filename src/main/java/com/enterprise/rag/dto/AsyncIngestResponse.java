package com.enterprise.rag.dto;

public record AsyncIngestResponse(
        String eventId,
        String sourceName,
        String status,
        String topic
) {}
