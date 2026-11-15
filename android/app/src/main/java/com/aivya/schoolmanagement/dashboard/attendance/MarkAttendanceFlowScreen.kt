package com.aivya.schoolmanagement.dashboard.attendance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivya.schoolmanagement.dashboard.AssignedClass
import com.aivya.schoolmanagement.dashboard.AttendanceSessionSummary
import com.aivya.schoolmanagement.dashboard.ClassAttendanceStatus
import com.aivya.schoolmanagement.dashboard.StudentAttendanceRecord
import com.aivya.schoolmanagement.dashboard.StudentAttendanceStatus
import com.aivya.schoolmanagement.dashboard.data.SyntheticDemoDashboardRepository
import com.aivya.schoolmanagement.dashboard.data.TeacherDashboardRepository
import com.aivya.schoolmanagement.ui.theme.AivyaSchoolManagementTheme
import kotlinx.coroutines.launch

// Aivya Visual Design Tokens (indigo/violet brand palette)
private val AivyaNavy = Color(0xFF2D1B69)
private val AivyaNavyLight = Color(0xFF7C5CFC)
private val AivyaPastelBlue = Color(0xFFEFEAFB)
private val AivyaPastelBlueLight = Color(0xFFF6F3FD)
private val AivyaSlate = Color(0xFF6B6480)
private val AivyaTextPrimary = Color(0xFF241B4D)
private val AivyaBorder = Color(0xFFE6E1F5)
private val AivyaScreenBackground = Color(0xFFF8F6FD)

private val AivyaGreenAccent = Color(0xFF1C8254)
private val AivyaGreenSoft = Color(0xFFE8F6EE)
private val AivyaPinkAccent = Color(0xFFE24A74)
private val AivyaPinkSoft = Color(0xFFFDEAF0)
private val AivyaAmberAccent = Color(0xFFD97706)
private val AivyaAmberSoft = Color(0xFFFEF3C7)

/**
 * Full-screen Interactive Mark Class Attendance Workflow
 */
@Composable
fun MarkAttendanceFlowScreen(
    assignedClasses: List<AssignedClass>,
    initialClass: AssignedClass? = null,
    repository: TeacherDashboardRepository,
    onDismiss: () -> Unit,
    onAttendanceSubmitted: (AttendanceSessionSummary) -> Unit,
) {
    val stateHolder = rememberMarkAttendanceStateHolder(
        assignedClasses = assignedClasses,
        initialClass = initialClass,
        repository = repository,
    )
    val state = stateHolder.state
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AivyaScreenBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            MarkAttendanceFlowTopBar(
                currentStep = state.currentStep,
                selectedClass = state.selectedClass,
                onBack = {
                    when (state.currentStep) {
                        MarkAttendanceStep.SELECT_CLASS -> onDismiss()
                        MarkAttendanceStep.MARK_ROSTER -> {
                            if (initialClass != null) {
                                onDismiss()
                            } else {
                                stateHolder.backToClassSelection()
                            }
                        }
                        MarkAttendanceStep.REVIEW_SUBMIT -> stateHolder.backToRoster()
                        MarkAttendanceStep.SUBMISSION_SUCCESS -> onDismiss()
                    }
                },
                onClose = onDismiss,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp),
            ) {
                when (state.currentStep) {
                    MarkAttendanceStep.SELECT_CLASS -> {
                        ClassSelectionStepContent(
                            classes = state.assignedClasses,
                            onClassSelected = { cls ->
                                stateHolder.selectClass(cls)
                            },
                        )
                    }

                    MarkAttendanceStep.MARK_ROSTER -> {
                        state.selectedClass?.let { cls ->
                            StudentRosterStepContent(
                                selectedClass = cls,
                                roster = state.filteredRoster,
                                totalCount = state.totalStudents,
                                presentCount = state.presentCount,
                                absentCount = state.absentCount,
                                lateCount = state.lateCount,
                                unmarkedCount = state.unmarkedCount,
                                searchQuery = state.searchQuery,
                                canUndo = state.canUndoSelectAll,
                                validationError = state.validationErrorMessage,
                                isLoadingRoster = state.isLoadingRoster,
                                networkErrorMessage = state.networkErrorMessage,
                                onRetryLoad = { stateHolder.retryLoadRoster() },
                                entryMode = state.entryMode,
                                onEntryModeChange = { stateHolder.setEntryMode(it) },
                                lastQrScanMessage = state.lastQrScanMessage,
                                onQrCodeScanned = { code -> stateHolder.checkInByQrCode(code) },
                                onSearchQueryChange = { stateHolder.setSearchQuery(it) },
                                onStudentStatusChange = { studentId, newStatus ->
                                    stateHolder.updateStudentStatus(studentId, newStatus)
                                },
                                onSelectAllPresent = {
                                    stateHolder.selectAllPresent()
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("All students marked Present. You can undo if needed.")
                                    }
                                },
                                onUndoSelectAll = {
                                    stateHolder.undoSelectAll()
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Restored previous attendance statuses.")
                                    }
                                },
                                onProceedToReview = {
                                    stateHolder.proceedToReview()
                                },
                            )
                        }
                    }

                    MarkAttendanceStep.REVIEW_SUBMIT -> {
                        state.selectedClass?.let { cls ->
                            ReviewAttendanceStepContent(
                                selectedClass = cls,
                                roster = state.roster,
                                totalCount = state.totalStudents,
                                presentCount = state.presentCount,
                                absentCount = state.absentCount,
                                lateCount = state.lateCount,
                                isSubmitting = state.isSubmitting,
                                validationError = state.validationErrorMessage ?: state.networkErrorMessage,
                                onBackToEdit = { stateHolder.backToRoster() },
                                onSubmit = {
                                    stateHolder.submitAttendance { summary ->
                                        onAttendanceSubmitted(summary)
                                    }
                                },
                            )
                        }
                    }

                    MarkAttendanceStep.SUBMISSION_SUCCESS -> {
                        state.submissionSummary?.let { summary ->
                            SubmissionSuccessContent(
                                summary = summary,
                                onDone = onDismiss,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Flow Header / Top Bar with Navigation Affordances and Step Pill
 */
@Composable
private fun MarkAttendanceFlowTopBar(
    currentStep: MarkAttendanceStep,
    selectedClass: AssignedClass?,
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (currentStep != MarkAttendanceStep.SUBMISSION_SUCCESS) {
                        IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = AivyaNavy,
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Column {
                        Text(
                            text = when (currentStep) {
                                MarkAttendanceStep.SELECT_CLASS -> "Select Class for Roll-Call"
                                MarkAttendanceStep.MARK_ROSTER -> "Student Roll-Call"
                                MarkAttendanceStep.REVIEW_SUBMIT -> "Review Attendance"
                                MarkAttendanceStep.SUBMISSION_SUCCESS -> "Attendance Confirmed"
                            },
                            color = AivyaNavy,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        selectedClass?.let { cls ->
                            if (currentStep != MarkAttendanceStep.SELECT_CLASS) {
                                Text(
                                    text = "${cls.className} • ${cls.section} (${cls.subject})",
                                    color = AivyaSlate,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = onClose, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = AivyaSlate,
                    )
                }
            }

            // Step Progress Indicator
            if (currentStep != MarkAttendanceStep.SUBMISSION_SUCCESS) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StepProgressPill(
                        stepNumber = 1,
                        label = "Select Class",
                        isActive = currentStep == MarkAttendanceStep.SELECT_CLASS,
                        isCompleted = currentStep != MarkAttendanceStep.SELECT_CLASS,
                        modifier = Modifier.weight(1f),
                    )
                    StepProgressPill(
                        stepNumber = 2,
                        label = "Roster",
                        isActive = currentStep == MarkAttendanceStep.MARK_ROSTER,
                        isCompleted = currentStep == MarkAttendanceStep.REVIEW_SUBMIT,
                        modifier = Modifier.weight(1f),
                    )
                    StepProgressPill(
                        stepNumber = 3,
                        label = "Review & Submit",
                        isActive = currentStep == MarkAttendanceStep.REVIEW_SUBMIT,
                        isCompleted = false,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun StepProgressPill(
    stepNumber: Int,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean,
    modifier: Modifier = Modifier,
) {
    val bg = when {
        isActive -> AivyaNavy
        isCompleted -> AivyaGreenSoft
        else -> AivyaPastelBlue
    }
    val contentColor = when {
        isActive -> Color.White
        isCompleted -> AivyaGreenAccent
        else -> AivyaSlate
    }

    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(8.dp))
            .padding(vertical = 5.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(12.dp),
                )
                Spacer(modifier = Modifier.width(3.dp))
            } else {
                Text(
                    text = "$stepNumber.",
                    color = contentColor,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text(
                text = label,
                color = contentColor,
                fontSize = 10.5.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * REQUIREMENT 1: CLASS SELECTION
 * Display assigned classes with class name, section, subject and total enrolled students.
 */
@Composable
private fun ClassSelectionStepContent(
    classes: List<AssignedClass>,
    onClassSelected: (AssignedClass) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
    ) {
        item {
            Text(
                text = "Choose an Assigned Class",
                color = AivyaNavy,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Select a class to record daily student attendance:",
                color = AivyaSlate,
                fontSize = 13.sp,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(classes, key = { it.id }) { assignedClass ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onClassSelected(assignedClass) }
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = AivyaNavy.copy(alpha = 0.04f),
                        spotColor = AivyaNavy.copy(alpha = 0.08f),
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(AivyaBorder, AivyaBorder)),
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${assignedClass.className} • ${assignedClass.section}",
                                    color = AivyaNavy,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (assignedClass.attendanceStatus == ClassAttendanceStatus.PENDING) {
                                    Box(
                                        modifier = Modifier
                                            .background(AivyaAmberSoft, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 7.dp, vertical = 2.dp),
                                    ) {
                                        Text(
                                            text = "Roll-Call Pending",
                                            color = AivyaAmberAccent,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .background(AivyaGreenSoft, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 7.dp, vertical = 2.dp),
                                    ) {
                                        Text(
                                            text = "Marked Today",
                                            color = AivyaGreenAccent,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = assignedClass.subject,
                                color = AivyaSlate,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }

                        // Enrolled Students Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(AivyaScreenBackground, RoundedCornerShape(10.dp))
                                .border(1.dp, AivyaBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Groups,
                                contentDescription = null,
                                tint = AivyaNavyLight,
                                modifier = Modifier.size(15.dp),
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${assignedClass.studentCount} Students",
                                color = AivyaTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = AivyaBorder, thickness = 0.8.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = AivyaSlate,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = assignedClass.scheduleTime,
                                color = AivyaSlate,
                                fontSize = 11.5.sp,
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(
                                imageVector = Icons.Outlined.MeetingRoom,
                                contentDescription = null,
                                tint = AivyaSlate,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = assignedClass.roomNumber,
                                color = AivyaSlate,
                                fontSize = 11.5.sp,
                            )
                        }

                        Button(
                            onClick = { onClassSelected(assignedClass) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AivyaPastelBlue,
                                contentColor = AivyaNavy,
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp),
                        ) {
                            Text(text = "Start Roll-Call", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * REQUIREMENTS 2, 3, 4, 5:
 * Scrollable Student Roster, Individual Status (Present, Absent, Late),
 * Dynamic Summary, Search Filter, Select All Present with Undo.
 */
@Composable
private fun EntryModeTab(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(
                if (selected) AivyaNavy else Color.Transparent,
                RoundedCornerShape(10.dp),
            )
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else AivyaSlate,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun StudentRosterStepContent(
    selectedClass: AssignedClass,
    roster: List<StudentAttendanceRecord>,
    totalCount: Int,
    presentCount: Int,
    absentCount: Int,
    lateCount: Int,
    unmarkedCount: Int,
    searchQuery: String,
    canUndo: Boolean,
    validationError: String?,
    isLoadingRoster: Boolean,
    networkErrorMessage: String?,
    onRetryLoad: () -> Unit,
    entryMode: AttendanceEntryMode,
    onEntryModeChange: (AttendanceEntryMode) -> Unit,
    lastQrScanMessage: String?,
    onQrCodeScanned: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onStudentStatusChange: (String, StudentAttendanceStatus) -> Unit,
    onSelectAllPresent: () -> Unit,
    onUndoSelectAll: () -> Unit,
    onProceedToReview: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        // Sticky Top Summary & Search Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                // Dynamic Summary Badges (Requirement 4)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    AttendanceSummaryPill(
                        label = "Total",
                        count = totalCount,
                        accentColor = AivyaNavy,
                        bgColor = AivyaPastelBlue,
                        modifier = Modifier.weight(1f),
                    )
                    AttendanceSummaryPill(
                        label = "Present",
                        count = presentCount,
                        accentColor = AivyaGreenAccent,
                        bgColor = AivyaGreenSoft,
                        modifier = Modifier.weight(1f),
                    )
                    AttendanceSummaryPill(
                        label = "Absent",
                        count = absentCount,
                        accentColor = AivyaPinkAccent,
                        bgColor = AivyaPinkSoft,
                        modifier = Modifier.weight(1f),
                    )
                    AttendanceSummaryPill(
                        label = "Late",
                        count = lateCount,
                        accentColor = AivyaAmberAccent,
                        bgColor = AivyaAmberSoft,
                        modifier = Modifier.weight(1f),
                    )
                    AttendanceSummaryPill(
                        label = "Unmarked",
                        count = unmarkedCount,
                        accentColor = if (unmarkedCount > 0) AivyaPinkAccent else AivyaSlate,
                        bgColor = if (unmarkedCount > 0) AivyaPinkSoft else AivyaPastelBlueLight,
                        modifier = Modifier.weight(1.2f),
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Manual Roll-Call vs Scan QR Code toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AivyaScreenBackground, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    EntryModeTab(
                        label = "Manual Roll-Call",
                        selected = entryMode == AttendanceEntryMode.MANUAL,
                        modifier = Modifier.weight(1f),
                        onClick = { onEntryModeChange(AttendanceEntryMode.MANUAL) },
                    )
                    EntryModeTab(
                        label = "Scan QR Code",
                        selected = entryMode == AttendanceEntryMode.QR_SCAN,
                        modifier = Modifier.weight(1f),
                        onClick = { onEntryModeChange(AttendanceEntryMode.QR_SCAN) },
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar (Requirement 2) -- manual mode only
                if (entryMode == AttendanceEntryMode.MANUAL) OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Search by name or roll number...",
                            color = AivyaSlate.copy(alpha = 0.65f),
                            fontSize = 13.sp,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = AivyaNavyLight,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Outlined.Clear,
                                    contentDescription = "Clear",
                                    tint = AivyaSlate,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AivyaScreenBackground,
                        unfocusedContainerColor = AivyaScreenBackground,
                        focusedBorderColor = AivyaNavy,
                        unfocusedBorderColor = AivyaBorder,
                    ),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Select All Present & Undo Row (Requirement 5) -- manual mode only
                if (entryMode == AttendanceEntryMode.MANUAL) Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Students Roster (${roster.size} shown)",
                        color = AivyaSlate,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (canUndo) {
                            OutlinedButton(
                                onClick = onUndoSelectAll,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, AivyaPinkAccent),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AivyaPinkAccent),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Undo,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Undo", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = onSelectAllPresent,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AivyaPastelBlue,
                                contentColor = AivyaNavy,
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Select All Present", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Validation Error Alert Banner
        AnimatedVisibility(visible = validationError != null) {
            validationError?.let { err ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AivyaPinkSoft)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = AivyaPinkAccent,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = err,
                            color = AivyaPinkAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }

        // Network error banner (roster failed to load) with retry
        AnimatedVisibility(visible = networkErrorMessage != null) {
            networkErrorMessage?.let { err ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AivyaAmberSoft)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = err,
                        color = AivyaAmberAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onRetryLoad) {
                        Text(text = "Retry", color = AivyaNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        when {
            isLoadingRoster -> {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = AivyaNavy)
                }
            }

            entryMode == AttendanceEntryMode.QR_SCAN -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    com.aivya.schoolmanagement.dashboard.scanner.QrScannerScreen(
                        onCodeScanned = onQrCodeScanned,
                        statusMessage = lastQrScanMessage
                            ?: "Scan a student's ID card QR code to mark them present",
                    )
                }
            }

            else -> {
                // Scrollable Students Roster List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    if (roster.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 40.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "No students match '$searchQuery'",
                                    color = AivyaSlate,
                                    fontSize = 13.sp,
                                )
                            }
                        }
                    } else {
                        items(roster, key = { it.id }) { student ->
                            StudentAttendanceItemRow(
                                student = student,
                                onStatusSelected = { newStatus ->
                                    onStudentStatusChange(student.id, newStatus)
                                },
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                        }
                    }
                }
            }
        }

        // Bottom Action Bar to Proceed to Review
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = if (unmarkedCount == 0) "All students marked" else "$unmarkedCount students pending",
                        color = if (unmarkedCount == 0) AivyaGreenAccent else AivyaAmberAccent,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${selectedClass.className} (${selectedClass.section})",
                        color = AivyaSlate,
                        fontSize = 11.sp,
                    )
                }

                Button(
                    onClick = onProceedToReview,
                    modifier = Modifier.height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (unmarkedCount == 0) AivyaNavy else AivyaNavy.copy(alpha = 0.5f),
                        contentColor = Color.White,
                    ),
                ) {
                    Text(text = "Review Attendance →", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AttendanceSummaryPill(
    label: String,
    count: Int,
    accentColor: Color,
    bgColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(vertical = 5.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = count.toString(),
                color = accentColor,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = label,
                color = AivyaSlate,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
        }
    }
}

/**
 * REQUIREMENT 3: INDIVIDUAL ATTENDANCE ITEM
 * Renders individual student card with selectable chips for Present, Absent, Late.
 * Selected status is clearly highlighted.
 */
@Composable
private fun StudentAttendanceItemRow(
    student: StudentAttendanceRecord,
    onStatusSelected: (StudentAttendanceStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    when (student.status) {
                        StudentAttendanceStatus.PRESENT -> AivyaGreenAccent.copy(alpha = 0.35f)
                        StudentAttendanceStatus.ABSENT -> AivyaPinkAccent.copy(alpha = 0.35f)
                        StudentAttendanceStatus.LATE -> AivyaAmberAccent.copy(alpha = 0.35f)
                        StudentAttendanceStatus.UNMARKED -> AivyaBorder
                    },
                    AivyaBorder,
                ),
            ),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Student Identity (Roll & Name)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(AivyaPastelBlue, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "#%02d".format(student.rollNumber),
                        color = AivyaNavy,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = student.studentName,
                        color = AivyaNavy,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "ID: ${student.id}",
                        color = AivyaSlate,
                        fontSize = 10.5.sp,
                    )
                }
            }

            // Selectable Status Chips: Present, Absent, Late
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                AttendanceStatusChip(
                    statusText = "Present",
                    isSelected = student.status == StudentAttendanceStatus.PRESENT,
                    activeBg = AivyaGreenAccent,
                    activeTextColor = Color.White,
                    onClick = { onStatusSelected(StudentAttendanceStatus.PRESENT) },
                )

                AttendanceStatusChip(
                    statusText = "Absent",
                    isSelected = student.status == StudentAttendanceStatus.ABSENT,
                    activeBg = AivyaPinkAccent,
                    activeTextColor = Color.White,
                    onClick = { onStatusSelected(StudentAttendanceStatus.ABSENT) },
                )

                AttendanceStatusChip(
                    statusText = "Late",
                    isSelected = student.status == StudentAttendanceStatus.LATE,
                    activeBg = AivyaAmberAccent,
                    activeTextColor = Color.White,
                    onClick = { onStatusSelected(StudentAttendanceStatus.LATE) },
                )
            }
        }
    }
}

@Composable
private fun AttendanceStatusChip(
    statusText: String,
    isSelected: Boolean,
    activeBg: Color,
    activeTextColor: Color,
    onClick: () -> Unit,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) activeBg else Color.Transparent,
        label = "chip_bg",
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) activeTextColor else AivyaSlate,
        label = "chip_text",
    )
    val borderColor = if (isSelected) activeBg else AivyaBorder

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(11.dp),
                )
                Spacer(modifier = Modifier.width(3.dp))
            }
            Text(
                text = statusText,
                color = textColor,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            )
        }
    }
}

/**
 * REQUIREMENTS 6 & 7: REVIEW AND SUBMISSION SAFETY
 * Displays final summary, lists absentees/lates, allows editing,
 * requires all students marked, prevents duplicate taps, labels as demo/local.
 */
@Composable
private fun ReviewAttendanceStepContent(
    selectedClass: AssignedClass,
    roster: List<StudentAttendanceRecord>,
    totalCount: Int,
    presentCount: Int,
    absentCount: Int,
    lateCount: Int,
    isSubmitting: Boolean,
    validationError: String?,
    onBackToEdit: () -> Unit,
    onSubmit: () -> Unit,
) {
    val absentStudents = remember(roster) {
        roster.filter { it.status == StudentAttendanceStatus.ABSENT }
    }
    val lateStudents = remember(roster) {
        roster.filter { it.status == StudentAttendanceStatus.LATE }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .navigationBarsPadding(),
    ) {
        Text(
            text = "Review Attendance Roll-Call",
            color = AivyaNavy,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Verify the attendance breakdown before finalizing local submission:",
            color = AivyaSlate,
            fontSize = 12.5.sp,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Class Overview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(AivyaBorder, AivyaBorder)),
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "${selectedClass.className} • ${selectedClass.section}",
                            color = AivyaNavy,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = selectedClass.subject,
                            color = AivyaSlate,
                            fontSize = 13.sp,
                        )
                    }

                    OutlinedButton(
                        onClick = onBackToEdit,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, AivyaNavyLight),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = AivyaNavy,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Edit Roster", color = AivyaNavy, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = AivyaBorder, thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(14.dp))

                // Breakdown Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    ReviewMetricItem(label = "Total Enrolled", value = "$totalCount", color = AivyaNavy)
                    ReviewMetricItem(label = "Present", value = "$presentCount", color = AivyaGreenAccent)
                    ReviewMetricItem(label = "Late Arrivals", value = "$lateCount", color = AivyaAmberAccent)
                    ReviewMetricItem(label = "Absentees", value = "$absentCount", color = AivyaPinkAccent)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Absentees List
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(AivyaBorder, AivyaBorder)),
            ),
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(AivyaPinkAccent, CircleShape),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reported Absentees (${absentStudents.size})",
                        color = AivyaNavy,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (absentStudents.isEmpty()) {
                    Text(
                        text = "No absent students reported. Perfect attendance!",
                        color = AivyaGreenAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                } else {
                    absentStudents.forEach { student ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "#%02d  ${student.studentName}".format(student.rollNumber),
                                color = AivyaTextPrimary,
                                fontSize = 12.5.sp,
                            )
                            Text(
                                text = "Absent",
                                color = AivyaPinkAccent,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Late Arrivals List
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(AivyaBorder, AivyaBorder)),
            ),
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(AivyaAmberAccent, CircleShape),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Late Arrivals (${lateStudents.size})",
                        color = AivyaNavy,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (lateStudents.isEmpty()) {
                    Text(
                        text = "No late arrivals recorded.",
                        color = AivyaSlate,
                        fontSize = 12.sp,
                    )
                } else {
                    lateStudents.forEach { student ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "#%02d  ${student.studentName}".format(student.rollNumber),
                                color = AivyaTextPrimary,
                                fontSize = 12.5.sp,
                            )
                            Text(
                                text = "Late",
                                color = AivyaAmberAccent,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // REQUIREMENT 7: SUBMISSION SAFETY NOTICE
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AivyaPastelBlue, RoundedCornerShape(12.dp))
                .border(1.dp, AivyaBorder, RoundedCornerShape(12.dp))
                .padding(12.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = AivyaNavyLight,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Demo / Local Session Mode",
                        color = AivyaNavy,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Attendance records are updated in local application state. Backend REST API synchronization endpoints will be linked in the production deployment.",
                        color = AivyaSlate,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                    )
                }
            }
        }

        validationError?.let { err ->
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = err,
                color = AivyaPinkAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons: Edit or Submit
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            OutlinedButton(
                onClick = onBackToEdit,
                enabled = !isSubmitting,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, AivyaBorder),
            ) {
                Text(text = "← Edit Roster", color = AivyaNavy, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onSubmit,
                enabled = !isSubmitting,
                modifier = Modifier
                    .weight(1.4f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AivyaNavy,
                    contentColor = Color.White,
                ),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Saving...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Submit Attendance", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ReviewMetricItem(
    label: String,
    value: String,
    color: Color,
) {
    Column {
        Text(text = value, color = color, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        Text(text = label, color = AivyaSlate, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

/**
 * Step 4: Submission Confirmation Dialog
 */
@Composable
private fun SubmissionSuccessContent(
    summary: AttendanceSessionSummary,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(AivyaGreenSoft, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = AivyaGreenAccent,
                modifier = Modifier.size(44.dp),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Roll-Call Recorded",
            color = AivyaNavy,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Attendance for ${summary.className} (${summary.section}) has been recorded in the local demo session.",
            color = AivyaSlate,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 19.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(AivyaBorder, AivyaBorder)),
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${summary.presentCount}", color = AivyaGreenAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Present", color = AivyaSlate, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${summary.lateCount}", color = AivyaAmberAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Late", color = AivyaSlate, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${summary.absentCount}", color = AivyaPinkAccent, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Absent", color = AivyaSlate, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "%.1f%%".format(summary.markedAttendancePercentage),
                        color = AivyaNavy,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(text = "Marked Rate", color = AivyaSlate, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AivyaNavy,
                contentColor = Color.White,
            ),
        ) {
            Text(text = "Return to Dashboard", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun MarkAttendanceFlowPreview() {
    val repository = remember { SyntheticDemoDashboardRepository() }
    val dashboardState = remember { repository.dashboardState.value }
    AivyaSchoolManagementTheme {
        MarkAttendanceFlowScreen(
            assignedClasses = dashboardState.classes,
            initialClass = dashboardState.classes.firstOrNull(),
            repository = repository,
            onDismiss = {},
            onAttendanceSubmitted = {},
        )
    }
}
