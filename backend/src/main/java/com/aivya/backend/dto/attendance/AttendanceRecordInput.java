package com.aivya.backend.dto.attendance;

import com.aivya.backend.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AttendanceRecordInput(
        @NotNull UUID studentId,
        @NotNull AttendanceStatus status
) {
}
