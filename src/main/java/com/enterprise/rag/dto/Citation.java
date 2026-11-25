package com.enterprise.rag.dto;

public record Citation(
        String sourceName,
        String department,
        String accessLevel,
        double score,
        String excerpt
) {}
