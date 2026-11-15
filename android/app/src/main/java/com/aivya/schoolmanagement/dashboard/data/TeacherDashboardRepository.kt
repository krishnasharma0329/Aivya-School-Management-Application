package com.aivya.schoolmanagement.dashboard.data

import com.aivya.schoolmanagement.dashboard.AssignedClass
import com.aivya.schoolmanagement.dashboard.AttendanceHistoryRecord
import com.aivya.schoolmanagement.dashboard.AttendanceSessionSummary
import com.aivya.schoolmanagement.dashboard.AttendanceSummaryState
import com.aivya.schoolmanagement.dashboard.ClassAttendanceStatus
import com.aivya.schoolmanagement.dashboard.DashboardUiState
import com.aivya.schoolmanagement.dashboard.SchoolNotice
import com.aivya.schoolmanagement.dashboard.StudentAttendanceRecord
import com.aivya.schoolmanagement.dashboard.StudentAttendanceStatus
import com.aivya.schoolmanagement.dashboard.StudentRosterItem
import com.aivya.schoolmanagement.dashboard.TeacherOverviewState
import com.aivya.schoolmanagement.dashboard.TeacherProfile
import com.aivya.schoolmanagement.network.NetworkResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Result of a single QR scan check-in, for the scanner screen to react to. */
data class QrCheckInOutcome(
    val studentName: String,
    val alreadyMarked: Boolean,
)

/**
 * Teacher Dashboard Repository Interface.
 *
 * Every operation that can fail over the network returns [NetworkResult] so
 * the UI always has a calm, specific message to show instead of crashing or
 * silently doing nothing. [dashboardState] is the latest known-good snapshot
 * (kept around for offline viewing); call [refresh] to pull a fresh copy.
 */
interface TeacherDashboardRepository {
    val dashboardState: StateFlow<DashboardUiState>

    suspend fun refresh(): NetworkResult<Unit>

    suspend fun getStudentAttendanceRecords(classId: String): NetworkResult<List<StudentAttendanceRecord>>

    suspend fun submitIndividualClassAttendance(
        classId: String,
        records: List<StudentAttendanceRecord>,
    ): NetworkResult<AttendanceSessionSummary>

    suspend fun checkInByQrCode(
        classId: String,
        qrCode: String,
    ): NetworkResult<QrCheckInOutcome>

    fun getClassRoster(classId: String): List<StudentRosterItem>
}

/**
 * ============================================================================
 * SYNTHETIC DEMO DATA REPOSITORY -- used only by @Preview composables.
 * ============================================================================
 * NO real teacher, student, or school personal data is stored or fetched
 * here. All entries use synthetic, made-up names and roll identifiers.
 * ============================================================================
 */
class SyntheticDemoDashboardRepository : TeacherDashboardRepository {

    private val initialProfile = TeacherProfile(
        id = "TCH-2026-DEMO",
        displayName = "Teacher",
        roleTitle = "Senior Faculty",
        department = "Department of Science & Mathematics",
        schoolName = "Aivya Model Academy",
        email = "teacher@aivya.edu",
        academicTerm = "Academic Year 2026-27 • Term 2",
    )

    private val initialClasses = listOf(
        AssignedClass(
            id = "CLS-10A", className = "Grade 10", section = "Section A",
            subject = "Advanced Mathematics", studentCount = 18,
            scheduleTime = "08:30 AM - 09:45 AM", roomNumber = "Room 204",
            attendanceStatus = ClassAttendanceStatus.MARKED,
            presentCount = 16, absentCount = 2, unmarkedCount = 0,
        ),
        AssignedClass(
            id = "CLS-09B", className = "Grade 9", section = "Section B",
            subject = "General Science", studentCount = 16,
            scheduleTime = "10:15 AM - 11:30 AM", roomNumber = "Lab 2",
            attendanceStatus = ClassAttendanceStatus.PENDING,
            presentCount = 0, absentCount = 0, unmarkedCount = 16,
        ),
    )

    private val _dashboardState = MutableStateFlow(
        DashboardUiState(
            profile = initialProfile,
            overview = TeacherOverviewState(
                totalAssignedClasses = initialClasses.size,
                totalStudents = initialClasses.sumOf { it.studentCount },
                todayAttendancePercentage = 88.9f,
                unmarkedStudentsCount = 16,
            ),
            classes = initialClasses,
            attendance = AttendanceSummaryState(
                classesMarkedToday = 1, totalClassesToday = 2,
                presentCount = 16, absentCount = 2, unmarkedCount = 16,
                lastUpdatedTime = "Just now",
            ),
            notices = listOf(
                SchoolNotice(
                    id = "NTC-01", title = "Quarterly Parent-Teacher Conference",
                    category = "Academic", dateText = "Tomorrow • 10:00 AM",
                    summary = "Schedules for academic consultation slots have been shared.",
                    isImportant = true,
                ),
            ),
            history = emptyList<AttendanceHistoryRecord>(),
        ),
    )

    override val dashboardState: StateFlow<DashboardUiState> = _dashboardState.asStateFlow()

    private val rosters = mutableMapOf<String, MutableList<StudentAttendanceRecord>>()

    private fun rosterFor(classId: String): MutableList<StudentAttendanceRecord> =
        rosters.getOrPut(classId) {
            val cls = _dashboardState.value.classes.find { it.id == classId }
            val total = cls?.studentCount ?: 0
            (1..total).map { i ->
                StudentAttendanceRecord(
                    id = "$classId-STD-%03d".format(i),
                    rollNumber = i,
                    studentName = "Student $i",
                    status = StudentAttendanceStatus.UNMARKED,
                )
            }.toMutableList()
        }

    override suspend fun refresh(): NetworkResult<Unit> = NetworkResult.Success(Unit)

    override suspend fun getStudentAttendanceRecords(classId: String) =
        NetworkResult.Success(rosterFor(classId).toList())

    override suspend fun submitIndividualClassAttendance(
        classId: String,
        records: List<StudentAttendanceRecord>,
    ): NetworkResult<AttendanceSessionSummary> {
        rosters[classId] = records.toMutableList()
        return NetworkResult.Success(summaryFor(classId, records))
    }

    override suspend fun checkInByQrCode(classId: String, qrCode: String): NetworkResult<QrCheckInOutcome> {
        val roster = rosterFor(classId)
        val student = roster.firstOrNull { it.id == qrCode } ?: roster.firstOrNull()
            ?: return NetworkResult.Error("No students in this class.")
        val already = student.status == StudentAttendanceStatus.PRESENT
        val index = roster.indexOf(student)
        roster[index] = student.copy(status = StudentAttendanceStatus.PRESENT)
        return NetworkResult.Success(QrCheckInOutcome(student.studentName, already))
    }

    override fun getClassRoster(classId: String): List<StudentRosterItem> =
        rosterFor(classId).map { StudentRosterItem(it.rollNumber, it.studentName, it.status.displayName) }

    private fun summaryFor(classId: String, records: List<StudentAttendanceRecord>): AttendanceSessionSummary {
        val cls = _dashboardState.value.classes.find { it.id == classId }
        return AttendanceSessionSummary(
            classId = classId,
            className = cls?.className ?: "",
            section = cls?.section ?: "",
            subject = cls?.subject ?: "",
            totalStudents = records.size,
            presentCount = records.count { it.status == StudentAttendanceStatus.PRESENT },
            absentCount = records.count { it.status == StudentAttendanceStatus.ABSENT },
            lateCount = records.count { it.status == StudentAttendanceStatus.LATE },
            unmarkedCount = records.count { it.status == StudentAttendanceStatus.UNMARKED },
        )
    }
}
