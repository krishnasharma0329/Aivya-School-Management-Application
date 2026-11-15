package com.aivya.backend.dto.attendance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;

public record AttendanceSubmitRequest(
        LocalDate date,
        @NotEmpty @Valid List<AttendanceRecordInput> records
) {
    public LocalDate dateOrToday() {
        return date != null ? date : LocalDate.now();
    }
}
