package com.aivya.backend.dto.attendance;

import jakarta.validation.constraints.NotBlank;

public record QrCheckInRequest(
        @NotBlank String qrCode
) {
}
