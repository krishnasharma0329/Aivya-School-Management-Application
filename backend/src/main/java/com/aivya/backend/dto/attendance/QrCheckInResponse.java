package com.aivya.backend.dto.attendance;

public record QrCheckInResponse(
        StudentAttendanceResponse student,
        AttendanceSummaryResponse summary,
        boolean alreadyMarked
) {
}
