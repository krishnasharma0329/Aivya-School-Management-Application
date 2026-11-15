package com.aivya.backend.controller;

import com.aivya.backend.dto.attendance.AttendanceHistoryItem;
import com.aivya.backend.dto.attendance.AttendanceSubmitRequest;
import com.aivya.backend.dto.attendance.AttendanceSummaryResponse;
import com.aivya.backend.dto.attendance.QrCheckInRequest;
import com.aivya.backend.dto.attendance.QrCheckInResponse;
import com.aivya.backend.dto.attendance.StudentAttendanceResponse;
import com.aivya.backend.service.AttendanceService;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/classes/{classId}")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/roster")
    public ResponseEntity<List<StudentAttendanceResponse>> getRoster(
            @PathVariable UUID classId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {

        return ResponseEntity.ok(
                attendanceService.getRoster(authentication.getName(), classId, date)
        );
    }

    @PostMapping("/attendance")
    public ResponseEntity<AttendanceSummaryResponse> submitAttendance(
            @PathVariable UUID classId,
            @Valid @RequestBody AttendanceSubmitRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                attendanceService.submitAttendance(authentication.getName(), classId, request)
        );
    }

    @PostMapping("/attendance/qr-checkin")
    public ResponseEntity<QrCheckInResponse> qrCheckIn(
            @PathVariable UUID classId,
            @Valid @RequestBody QrCheckInRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                attendanceService.qrCheckIn(authentication.getName(), classId, request)
        );
    }

    @GetMapping("/attendance/history")
    public ResponseEntity<List<AttendanceHistoryItem>> getHistory(
            @PathVariable UUID classId,
            @RequestParam(defaultValue = "7") int days,
            Authentication authentication) {

        return ResponseEntity.ok(
                attendanceService.getHistory(authentication.getName(), classId, days)
        );
    }
}
