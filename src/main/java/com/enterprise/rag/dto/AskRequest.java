package com.enterprise.rag.dto;

import jakarta.validation.constraints.NotBlank;

public record AskRequest(
        @NotBlank String question,
        @NotBlank String userRole
) {}
