package com.aivya.backend.dto.classes;

import java.util.UUID;

public record ClassResponse(
        UUID id,
        String grade,
        String section,
        String subject,
        String room,
        long studentCount
) {
}
