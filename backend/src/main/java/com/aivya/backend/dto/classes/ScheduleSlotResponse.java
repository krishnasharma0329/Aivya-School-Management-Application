package com.aivya.backend.dto.classes;

public record ScheduleSlotResponse(
        String dayOfWeek,
        String startTime,
        String endTime
) {
}
