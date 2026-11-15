package com.aivya.backend.dto.attendance;

import com.aivya.backend.entity.AttendanceStatus;

import java.util.UUID;

public record StudentAttendanceResponse(
        UUID studentId,
        String name,
        String rollNumber,
        boolean hasQrCode,
        AttendanceStatus status
) {
}
