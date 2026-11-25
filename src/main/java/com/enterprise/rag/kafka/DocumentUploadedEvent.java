package com.enterprise.rag.kafka;

import java.time.Instant;

public record DocumentUploadedEvent(
        String eventId,
        String sourceName,
        String department,
        String accessLevel,
        String content,
        Instant submittedAt
) {}
