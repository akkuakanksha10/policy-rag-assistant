package com.enterprise.rag.dto;

import java.util.List;

public record AskResponse(
        String answer,
        String userRole,
        List<Citation> citations,
        long retrievalMs,
        long llmMs
) {}
