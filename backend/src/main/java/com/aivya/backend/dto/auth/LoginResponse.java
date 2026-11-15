package com.aivya.backend.dto.auth;

import java.util.UUID;

public record LoginResponse(
    String token,
    String tokenType,
    long expiresIn,
    UUID teacherId,
    String fullName,
    String email,
    String role
) {}

