package com.aivya.schoolmanagement.network

data class LoginRequest(
    val email: String,
    val password: String,
)

data class LoginResponse(
    val token: String,
    val tokenType: String,
    val expiresIn: Long,
    val teacherId: String,
    val fullName: String,
    val email: String,
    val role: String,
)

data class ClassDto(
    val id: String,
    val grade: String,
    val section: String,
    val subject: String,
    val room: String?,
    val studentCount: Long,
    val schedules: List<ScheduleSlotDto> = emptyList(),
)

data class ScheduleSlotDto(
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
)

data class StudentAttendanceDto(
    val studentId: String,
    val name: String,
    val rollNumber: String,
    val hasQrCode: Boolean,
    val status: String?,
)

data class AttendanceRecordInput(
    val studentId: String,
    val status: String,
)

data class AttendanceSubmitRequest(
    val date: String? = null,
    val records: List<AttendanceRecordInput>,
)

data class AttendanceSummaryDto(
    val classId: String,
    val date: String,
    val total: Int,
    val present: Int,
    val absent: Int,
    val late: Int,
    val unmarked: Int,
)

data class QrCheckInRequest(
    val qrCode: String,
)

data class QrCheckInResponseDto(
    val student: StudentAttendanceDto,
    val summary: AttendanceSummaryDto,
    val alreadyMarked: Boolean,
)

data class AttendanceHistoryItemDto(
    val date: String,
    val total: Int,
    val present: Int,
    val absent: Int,
    val late: Int,
    val unmarked: Int,
)

data class ApiErrorBody(
    val status: Int? = null,
    val message: String? = null,
)
