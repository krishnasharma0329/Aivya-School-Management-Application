package com.aivya.backend.dto.attendance;

import java.time.LocalDate;
import java.util.UUID;

public record AttendanceSummaryResponse(
        UUID classId,
        LocalDate date,
        int total,
        int present,
        int absent,
        int late,
        int unmarked
) {
}
