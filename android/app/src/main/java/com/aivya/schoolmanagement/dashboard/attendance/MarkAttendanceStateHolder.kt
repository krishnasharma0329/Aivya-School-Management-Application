package com.aivya.schoolmanagement.dashboard.attendance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.aivya.schoolmanagement.dashboard.AssignedClass
import com.aivya.schoolmanagement.dashboard.AttendanceSessionSummary
import com.aivya.schoolmanagement.dashboard.StudentAttendanceRecord
import com.aivya.schoolmanagement.dashboard.StudentAttendanceStatus
import com.aivya.schoolmanagement.dashboard.data.TeacherDashboardRepository
import com.aivya.schoolmanagement.network.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Step progression for the Mark Class Attendance flow */
enum class MarkAttendanceStep {
    SELECT_CLASS,
    MARK_ROSTER,
    REVIEW_SUBMIT,
    SUBMISSION_SUCCESS,
}

/** How the teacher is taking attendance for the current roster. */
enum class AttendanceEntryMode {
    MANUAL,
    QR_SCAN,
}

/**
 * Immutable UI State for the Mark Attendance workflow
 */
data class MarkAttendanceUiState(
    val assignedClasses: List<AssignedClass> = emptyList(),
    val selectedClass: AssignedClass? = null,
    val roster: List<StudentAttendanceRecord> = emptyList(),
    val searchQuery: String = "",
    val currentStep: MarkAttendanceStep = MarkAttendanceStep.SELECT_CLASS,
    val entryMode: AttendanceEntryMode = AttendanceEntryMode.MANUAL,
    val isLoadingRoster: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSubmittedLocally: Boolean = false,
    val canUndoSelectAll: Boolean = false,
    val validationErrorMessage: String? = null,
    val networkErrorMessage: String? = null,
    val lastQrScanMessage: String? = null,
    val submissionSummary: AttendanceSessionSummary? = null,
) {
    val totalStudents: Int
        get() = roster.size

    val presentCount: Int
        get() = roster.count { it.status == StudentAttendanceStatus.PRESENT }

    val absentCount: Int
        get() = roster.count { it.status == StudentAttendanceStatus.ABSENT }

    val lateCount: Int
        get() = roster.count { it.status == StudentAttendanceStatus.LATE }

    val unmarkedCount: Int
        get() = roster.count { it.status == StudentAttendanceStatus.UNMARKED }

    val isAllMarked: Boolean
        get() = roster.isNotEmpty() && unmarkedCount == 0

    val filteredRoster: List<StudentAttendanceRecord>
        get() {
            if (searchQuery.isBlank()) return roster
            val query = searchQuery.trim().lowercase()
            return roster.filter { student ->
                student.studentName.lowercase().contains(query) ||
                    student.rollNumber.toString().contains(query) ||
                    "#%02d".format(student.rollNumber).contains(query) ||
                    student.id.lowercase().contains(query)
            }
        }
}

/**
 * State Holder for the interactive individual roll-call flow.
 * Manages class selection, individual status changes, search filtering,
 * Select-All with Undo, QR check-in, review validation, and backend submission.
 */
@Stable
class MarkAttendanceStateHolder(
    initialClasses: List<AssignedClass>,
    initialClass: AssignedClass? = null,
    private val repository: TeacherDashboardRepository,
    private val scope: CoroutineScope,
) {
    var state by mutableStateOf(
        MarkAttendanceUiState(
            assignedClasses = initialClasses,
            selectedClass = initialClass,
            currentStep = if (initialClass != null) MarkAttendanceStep.MARK_ROSTER else MarkAttendanceStep.SELECT_CLASS,
        ),
    )
        private set

    private var preSelectAllSnapshot: List<StudentAttendanceRecord>? = null

    init {
        initialClass?.let { cls -> loadClassRoster(cls) }
    }

    fun selectClass(cls: AssignedClass) {
        state = state.copy(
            selectedClass = cls,
            currentStep = MarkAttendanceStep.MARK_ROSTER,
            searchQuery = "",
            validationErrorMessage = null,
        )
        loadClassRoster(cls)
    }

    private fun loadClassRoster(cls: AssignedClass) {
        state = state.copy(isLoadingRoster = true, networkErrorMessage = null)
        scope.launch {
            when (val result = repository.getStudentAttendanceRecords(cls.id)) {
                is NetworkResult.Success -> {
                    state = state.copy(
                        roster = result.data.distinctBy { it.id },
                        canUndoSelectAll = false,
                        isLoadingRoster = false,
                    )
                    preSelectAllSnapshot = null
                }
                is NetworkResult.Error -> {
                    state = state.copy(isLoadingRoster = false, networkErrorMessage = result.message)
                }
            }
        }
    }

    fun retryLoadRoster() {
        state.selectedClass?.let { loadClassRoster(it) }
    }

    fun setEntryMode(mode: AttendanceEntryMode) {
        state = state.copy(entryMode = mode, lastQrScanMessage = null)
    }

    fun setSearchQuery(query: String) {
        state = state.copy(searchQuery = query)
    }

    fun updateStudentStatus(studentId: String, newStatus: StudentAttendanceStatus) {
        val updatedRoster = state.roster.map { student ->
            if (student.id == studentId) student.copy(status = newStatus) else student
        }
        state = state.copy(roster = updatedRoster, validationErrorMessage = null)
    }

    /** Called with the raw text decoded from a scanned QR code. */
    fun checkInByQrCode(qrCode: String) {
        val classId = state.selectedClass?.id ?: return
        if (state.isSubmitting) return

        scope.launch {
            when (val result = repository.checkInByQrCode(classId, qrCode)) {
                is NetworkResult.Success -> {
                    val outcome = result.data
                    val message = if (outcome.alreadyMarked) {
                        "${outcome.studentName} was already marked present today."
                    } else {
                        "${outcome.studentName} marked present."
                    }
                    state = state.copy(lastQrScanMessage = message)
                    // Re-sync the roster so the manual list reflects the scan too.
                    loadClassRoster(state.selectedClass!!)
                }
                is NetworkResult.Error -> {
                    state = state.copy(lastQrScanMessage = result.message)
                }
            }
        }
    }

    fun selectAllPresent() {
        if (state.roster.isEmpty()) return
        preSelectAllSnapshot = state.roster
        val updatedRoster = state.roster.map { it.copy(status = StudentAttendanceStatus.PRESENT) }
        state = state.copy(roster = updatedRoster, canUndoSelectAll = true, validationErrorMessage = null)
    }

    fun undoSelectAll() {
        preSelectAllSnapshot?.let { previous ->
            state = state.copy(roster = previous, canUndoSelectAll = false)
            preSelectAllSnapshot = null
        }
    }

    fun proceedToReview(): Boolean {
        if (!state.isAllMarked) {
            val remaining = state.unmarkedCount
            state = state.copy(
                validationErrorMessage = "$remaining student(s) still unmarked. Every student must be marked Present, Absent, or Late before reviewing.",
            )
            return false
        }
        state = state.copy(currentStep = MarkAttendanceStep.REVIEW_SUBMIT, validationErrorMessage = null)
        return true
    }

    fun backToRoster() {
        state = state.copy(currentStep = MarkAttendanceStep.MARK_ROSTER, validationErrorMessage = null)
    }

    fun backToClassSelection() {
        state = state.copy(currentStep = MarkAttendanceStep.SELECT_CLASS, validationErrorMessage = null)
    }

    fun submitAttendance(onCompleted: (AttendanceSessionSummary) -> Unit) {
        // Submission safety: prevent duplicate taps.
        if (state.isSubmitting || state.isSubmittedLocally) return
        val currentClass = state.selectedClass ?: return

        if (!state.isAllMarked) {
            state = state.copy(validationErrorMessage = "Cannot submit: All students must be assigned a status.")
            return
        }

        state = state.copy(isSubmitting = true, networkErrorMessage = null)

        scope.launch {
            when (
                val result = repository.submitIndividualClassAttendance(
                    classId = currentClass.id,
                    records = state.roster,
                )
            ) {
                is NetworkResult.Success -> {
                    state = state.copy(
                        isSubmitting = false,
                        isSubmittedLocally = true,
                        currentStep = MarkAttendanceStep.SUBMISSION_SUCCESS,
                        submissionSummary = result.data,
                    )
                    onCompleted(result.data)
                }
                is NetworkResult.Error -> {
                    // Stay on the review step -- never show a success state
                    // before the server has actually confirmed it.
                    state = state.copy(isSubmitting = false, networkErrorMessage = result.message)
                }
            }
        }
    }
}

/** Remember helper for [MarkAttendanceStateHolder] */
@Composable
fun rememberMarkAttendanceStateHolder(
    assignedClasses: List<AssignedClass>,
    initialClass: AssignedClass? = null,
    repository: TeacherDashboardRepository,
): MarkAttendanceStateHolder {
    val scope = rememberCoroutineScope()
    return remember(assignedClasses, initialClass?.id) {
        MarkAttendanceStateHolder(
            initialClasses = assignedClasses,
            initialClass = initialClass,
            repository = repository,
            scope = scope,
        )
    }
}
