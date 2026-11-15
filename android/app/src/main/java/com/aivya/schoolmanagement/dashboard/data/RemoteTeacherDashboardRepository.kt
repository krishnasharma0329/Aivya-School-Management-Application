package com.aivya.schoolmanagement.dashboard.data

import com.aivya.schoolmanagement.auth.TokenStore
import com.aivya.schoolmanagement.dashboard.AssignedClass
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
import com.aivya.schoolmanagement.network.AivyaApi
import com.aivya.schoolmanagement.network.AttendanceRecordInput
import com.aivya.schoolmanagement.network.AttendanceSubmitRequest
import com.aivya.schoolmanagement.network.NetworkResult
import com.aivya.schoolmanagement.network.QrCheckInRequest
import com.aivya.schoolmanagement.network.StudentAttendanceDto
import com.aivya.schoolmanagement.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Real, backend-backed implementation. Every class's "today" roster is
 * fetched to build the dashboard's attendance summary, so what the teacher
 * sees on Home always matches what the server actually has recorded --
 * never a locally-invented success state.
 */
class RemoteTeacherDashboardRepository(
    private val api: AivyaApi,
    private val tokenStore: TokenStore,
) : TeacherDashboardRepository {

    private val _dashboardState = MutableStateFlow(emptyDashboardState())
    override val dashboardState: StateFlow<DashboardUiState> = _dashboardState.asStateFlow()

    private val rosterCache = mutableMapOf<String, List<StudentRosterItem>>()

    override suspend fun refresh(): NetworkResult<Unit> {
        val session = tokenStore.load()
            ?: return NetworkResult.Error("You're signed out. Please sign in again.", isAuthExpired = true)

        val classesResult = safeApiCall { api.getMyClasses() }
        val classDtos = when (classesResult) {
            is NetworkResult.Success -> classesResult.data
            is NetworkResult.Error -> return NetworkResult.Error(classesResult.message, classesResult.isAuthExpired)
        }

        val assignedClasses = mutableListOf<AssignedClass>()
        for (dto in classDtos) {
            val rosterResult = safeApiCall { api.getRoster(dto.id) }
            val roster = (rosterResult as? NetworkResult.Success)?.data.orEmpty()
            rosterCache[dto.id] = roster.map {
                StudentRosterItem(it.rollNumber.toIntOrNull() ?: 0, it.name, it.status ?: "Unmarked")
            }

            val present = roster.count { it.status == "PRESENT" }
            val absent = roster.count { it.status == "ABSENT" }
            val late = roster.count { it.status == "LATE" }
            val unmarked = roster.size - present - absent - late

            assignedClasses += AssignedClass(
                id = dto.id,
                className = dto.grade,
                section = dto.section,
                subject = dto.subject,
                studentCount = dto.studentCount.toInt(),
                scheduleTime = "",
                roomNumber = dto.room ?: "Not assigned",
                attendanceStatus = if (roster.isNotEmpty() && unmarked == 0) {
                    ClassAttendanceStatus.MARKED
                } else {
                    ClassAttendanceStatus.PENDING
                },
                presentCount = present,
                absentCount = absent,
                lateCount = late,
                unmarkedCount = unmarked,
            )
        }

        val totalStudents = assignedClasses.sumOf { it.studentCount }
        val totalPresent = assignedClasses.sumOf { it.presentCount }
        val totalAbsent = assignedClasses.sumOf { it.absentCount }
        val totalLate = assignedClasses.sumOf { it.lateCount }
        val totalUnmarked = assignedClasses.sumOf { it.unmarkedCount }
        val totalMarked = totalPresent + totalAbsent + totalLate
        val percentage = if (totalMarked > 0) (totalPresent.toFloat() / totalMarked) * 100f else 0f

        _dashboardState.value = DashboardUiState(
            profile = TeacherProfile(
                id = session.teacherId,
                displayName = session.fullName,
                roleTitle = "Teacher",
                department = "Aivya School Management",
                schoolName = "Aivya School",
                email = session.email,
            ),
            overview = TeacherOverviewState(
                totalAssignedClasses = assignedClasses.size,
                totalStudents = totalStudents,
                todayAttendancePercentage = percentage,
                unmarkedStudentsCount = totalUnmarked,
            ),
            classes = assignedClasses,
            attendance = AttendanceSummaryState(
                classesMarkedToday = assignedClasses.count { it.attendanceStatus == ClassAttendanceStatus.MARKED },
                totalClassesToday = assignedClasses.size,
                presentCount = totalPresent,
                absentCount = totalAbsent,
                lateCount = totalLate,
                unmarkedCount = totalUnmarked,
                lastUpdatedTime = "Just now",
            ),
            notices = emptyList<SchoolNotice>(),
            history = emptyList(),
        )

        return NetworkResult.Success(Unit)
    }

    override suspend fun getStudentAttendanceRecords(classId: String): NetworkResult<List<StudentAttendanceRecord>> {
        return when (val result = safeApiCall { api.getRoster(classId) }) {
            is NetworkResult.Success -> NetworkResult.Success(result.data.map { it.toRecord() })
            is NetworkResult.Error -> result
        }
    }

    override suspend fun submitIndividualClassAttendance(
        classId: String,
        records: List<StudentAttendanceRecord>,
    ): NetworkResult<AttendanceSessionSummary> {
        val request = AttendanceSubmitRequest(
            records = records
                .filter { it.status != StudentAttendanceStatus.UNMARKED }
                .map { AttendanceRecordInput(it.id, it.status.name) },
        )

        return when (val result = safeApiCall { api.submitAttendance(classId, request) }) {
            is NetworkResult.Success -> {
                val dto = result.data
                val cls = _dashboardState.value.classes.find { it.id == classId }
                NetworkResult.Success(
                    AttendanceSessionSummary(
                        classId = dto.classId,
                        className = cls?.className ?: "",
                        section = cls?.section ?: "",
                        subject = cls?.subject ?: "",
                        totalStudents = dto.total,
                        presentCount = dto.present,
                        absentCount = dto.absent,
                        lateCount = dto.late,
                        unmarkedCount = dto.unmarked,
                    ),
                )
            }
            is NetworkResult.Error -> result
        }
    }

    override suspend fun checkInByQrCode(classId: String, qrCode: String): NetworkResult<QrCheckInOutcome> {
        return when (val result = safeApiCall { api.qrCheckIn(classId, QrCheckInRequest(qrCode)) }) {
            is NetworkResult.Success -> NetworkResult.Success(
                QrCheckInOutcome(
                    studentName = result.data.student.name,
                    alreadyMarked = result.data.alreadyMarked,
                ),
            )
            is NetworkResult.Error -> result
        }
    }

    override fun getClassRoster(classId: String): List<StudentRosterItem> =
        rosterCache[classId].orEmpty()

    private fun StudentAttendanceDto.toRecord(): StudentAttendanceRecord {
        val status = when (this.status) {
            "PRESENT" -> StudentAttendanceStatus.PRESENT
            "ABSENT" -> StudentAttendanceStatus.ABSENT
            "LATE" -> StudentAttendanceStatus.LATE
            else -> StudentAttendanceStatus.UNMARKED
        }
        return StudentAttendanceRecord(
            id = this.studentId,
            rollNumber = this.rollNumber.toIntOrNull() ?: 0,
            studentName = this.name,
            status = status,
        )
    }
}

private fun emptyDashboardState(): DashboardUiState = DashboardUiState(
    profile = TeacherProfile(
        id = "", displayName = "Teacher", roleTitle = "Teacher",
        department = "", schoolName = "Aivya School", email = "",
    ),
    overview = TeacherOverviewState(0, 0, 0f, 0),
    classes = emptyList(),
    attendance = AttendanceSummaryState(0, 0, 0, 0, 0, "Never"),
    notices = emptyList(),
    history = emptyList(),
)
