package com.aivya.schoolmanagement.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Teacher Profile Model (Prepared for future REST API integration)
 */
data class TeacherProfile(
    val id: String,
    val displayName: String,
    val roleTitle: String,
    val department: String,
    val schoolName: String,
    val email: String = "teacher@aivya.edu",
    val academicTerm: String = "Academic Year 2026-27 • Term 2",
)

/**
 * Metric summary for "Today's Overview" section
 * Attendance percentage is calculated strictly from marked students.
 */
data class TeacherOverviewState(
    val totalAssignedClasses: Int,
    val totalStudents: Int,
    val todayAttendancePercentage: Float,
    val unmarkedStudentsCount: Int = 35,
)

/**
 * Class Attendance Status for the current academic day
 */
enum class ClassAttendanceStatus {
    MARKED,
    PENDING,
}

/**
 * Assigned Class Card Model
 */
data class AssignedClass(
    val id: String,
    val className: String,
    val section: String,
    val subject: String,
    val studentCount: Int,
    val scheduleTime: String,
    val roomNumber: String,
    val attendanceStatus: ClassAttendanceStatus = ClassAttendanceStatus.MARKED,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val lateCount: Int = 0,
    val unmarkedCount: Int = 0,
)

/**
 * Individual Student Attendance Status for Roll-Call Workflow
 */
enum class StudentAttendanceStatus(val displayName: String) {
    PRESENT("Present"),
    ABSENT("Absent"),
    LATE("Late"),
    UNMARKED("Unmarked"),
}

/**
 * Individual Student Attendance Record
 */
data class StudentAttendanceRecord(
    val id: String,
    val rollNumber: Int,
    val studentName: String,
    val status: StudentAttendanceStatus = StudentAttendanceStatus.UNMARKED,
)

/**
 * Roll-Call Session Calculation Summary
 */
data class AttendanceSessionSummary(
    val classId: String,
    val className: String,
    val section: String,
    val subject: String,
    val totalStudents: Int,
    val presentCount: Int,
    val absentCount: Int,
    val lateCount: Int,
    val unmarkedCount: Int,
) {
    val isComplete: Boolean
        get() = unmarkedCount == 0

    val markedStudentsCount: Int
        get() = presentCount + absentCount + lateCount

    val markedAttendancePercentage: Float
        get() = if (markedStudentsCount > 0) {
            (presentCount.toFloat() / markedStudentsCount.toFloat()) * 100f
        } else {
            0f
        }
}

/**
 * Daily Attendance summary state
 *
 * Accuracy invariants:
 * - totalStudents = presentCount + absentCount + lateCount + unmarkedCount
 * - totalMarked = presentCount + absentCount + lateCount
 * - attendancePercentage = (presentCount / totalMarked) * 100 (when totalMarked > 0)
 */
data class AttendanceSummaryState(
    val classesMarkedToday: Int,
    val totalClassesToday: Int,
    val presentCount: Int,
    val absentCount: Int,
    val unmarkedCount: Int,
    val lastUpdatedTime: String,
    val lateCount: Int = 0,
) {
    val totalMarkedStudents: Int
        get() = presentCount + absentCount

    val totalStudents: Int
        get() = presentCount + absentCount + unmarkedCount

    /**
     * Calculated attendance rate based strictly on marked students.
     * Example: 101 present out of 107 marked = 94.4%
     */
    val markedAttendancePercentage: Float
        get() = if (totalMarkedStudents > 0) {
            (presentCount.toFloat() / totalMarkedStudents.toFloat()) * 100f
        } else {
            0f
        }

    val isAllMarked: Boolean
        get() = (unmarkedCount == 0) && (classesMarkedToday == totalClassesToday)
}

/**
 * School Notice / Announcement Model
 */
data class SchoolNotice(
    val id: String,
    val title: String,
    val category: String,
    val dateText: String,
    val summary: String,
    val isImportant: Boolean = false,
)

/**
 * Attendance History Item for previous days
 */
data class AttendanceHistoryRecord(
    val dateText: String,
    val dayName: String,
    val presentCount: Int,
    val totalStudents: Int,
    val percentage: Float,
    val classesCount: Int = 4,
)

/**
 * Generic Student Roster item for Class details view
 */
data class StudentRosterItem(
    val rollNumber: Int,
    val studentName: String,
    val status: String, // "Present", "Absent", "Unmarked"
)

/**
 * Bottom Navigation Tab Definition
 */
enum class DashboardTab(
    val title: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Outlined.Home),
    CLASSES("Classes", Icons.Outlined.Class),
    ATTENDANCE("Attendance", Icons.Outlined.CalendarMonth),
    PROFILE("Profile", Icons.Outlined.Person),
}

/**
 * Full UI State wrapper for the Teacher Dashboard
 */
data class DashboardUiState(
    val profile: TeacherProfile,
    val overview: TeacherOverviewState,
    val classes: List<AssignedClass>,
    val attendance: AttendanceSummaryState,
    val notices: List<SchoolNotice>,
    val history: List<AttendanceHistoryRecord> = emptyList(),
)
