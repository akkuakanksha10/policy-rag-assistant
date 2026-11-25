package com.enterprise.rag.dto;

public record IngestResponse(
        String sourceName,
        String accessLevel,
        int chunksStored,
        long elapsedMs
) {}
