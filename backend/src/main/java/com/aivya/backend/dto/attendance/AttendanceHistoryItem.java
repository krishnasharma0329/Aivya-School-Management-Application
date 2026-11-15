package com.aivya.backend.dto.attendance;

import java.time.LocalDate;

public record AttendanceHistoryItem(
        LocalDate date,
        int total,
        int present,
        int absent,
        int late,
        int unmarked
) {
}
