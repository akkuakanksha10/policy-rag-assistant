package com.enterprise.rag.dto;

import jakarta.validation.constraints.NotBlank;

public record IngestRequest(
        @NotBlank String sourceName,
        @NotBlank String department,
        @NotBlank String accessLevel,
        @NotBlank String content
) {}
