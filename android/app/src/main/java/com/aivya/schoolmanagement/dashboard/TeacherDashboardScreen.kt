package com.aivya.schoolmanagement.dashboard

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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.aivya.schoolmanagement.dashboard.data.SyntheticDemoDashboardRepository
import com.aivya.schoolmanagement.dashboard.data.TeacherDashboardRepository
import com.aivya.schoolmanagement.ui.theme.AivyaSchoolManagementTheme
import kotlinx.coroutines.launch

// Aivya Design System Palette
// Indigo/violet brand palette (names kept for compatibility across screens).
private val AivyaNavy = Color(0xFF2D1B69)
private val AivyaNavyDark = Color(0xFF1E1147)
private val AivyaNavyLight = Color(0xFF7C5CFC)
private val AivyaPastelBlue = Color(0xFFEFEAFB)
private val AivyaPastelBlueLight = Color(0xFFF6F3FD)
private val AivyaSlate = Color(0xFF6B6480)
private val AivyaTextPrimary = Color(0xFF241B4D)
private val AivyaBorder = Color(0xFFE6E1F5)
private val AivyaCardBackground = Color(0xFFFFFFFF)
private val AivyaScreenBackground = Color(0xFFF8F6FD)

private val AivyaPinkAccent = Color(0xFFE24A74)
private val AivyaPinkSoft = Color(0xFFFDEAF0)
private val AivyaGreenAccent = Color(0xFF1C8254)
private val AivyaGreenSoft = Color(0xFFE8F6EE)
private val AivyaAmberAccent = Color(0xFFD97706)
private val AivyaAmberSoft = Color(0xFFFEF3C7)

/**
 * Main Teacher Dashboard Screen.
 * Accepts a [TeacherDashboardRepository] interface to cleanly separate synthetic
 * demo data from real production backend services.
 */
@Composable
fun TeacherDashboardScreen(
    repository: TeacherDashboardRepository = remember { SyntheticDemoDashboardRepository() },
    onLogoutClick: () -> Unit = {},
) {
    val state by repository.dashboardState.collectAsState()
    var selectedTab by remember { mutableStateOf(DashboardTab.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Interactive Dialog States
    var selectedClassForDetails by remember { mutableStateOf<AssignedClass?>(null) }
    var showMarkAttendanceFlow by remember { mutableStateOf(false) }
    var markAttendanceInitialClass by remember { mutableStateOf<AssignedClass?>(null) }
    var isAttendanceHistoryDialogOpen by remember { mutableStateOf(false) }

    var isRefreshing by remember { mutableStateOf(true) }
    var refreshError by remember { mutableStateOf<String?>(null) }

    suspend fun refreshDashboard() {
        isRefreshing = true
        when (val result = repository.refresh()) {
            is com.aivya.schoolmanagement.network.NetworkResult.Success -> refreshError = null
            is com.aivya.schoolmanagement.network.NetworkResult.Error -> refreshError = result.message
        }
        isRefreshing = false
    }

    LaunchedEffect(Unit) { refreshDashboard() }

    LaunchedEffect(refreshError) {
        val message = refreshError ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(message = message, actionLabel = "Retry")
        if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
            refreshDashboard()
        }
    }

    fun openMarkAttendance(initialClass: AssignedClass?) {
        markAttendanceInitialClass = initialClass
        showMarkAttendanceFlow = true
    }

    if (showMarkAttendanceFlow) {
        com.aivya.schoolmanagement.dashboard.attendance.MarkAttendanceFlowScreen(
            assignedClasses = state.classes,
            initialClass = markAttendanceInitialClass,
            repository = repository,
            onDismiss = { showMarkAttendanceFlow = false },
            onAttendanceSubmitted = {
                showMarkAttendanceFlow = false
                coroutineScope.launch { refreshDashboard() }
            },
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AivyaScreenBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            DashboardBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            if (isRefreshing && state.classes.isEmpty()) {
                androidx.compose.material3.CircularProgressIndicator(color = AivyaNavy)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp),
            ) {
                when (selectedTab) {
                    DashboardTab.HOME -> {
                        DashboardHomeContent(
                            state = state,
                            onViewClass = { assignedClass ->
                                selectedClassForDetails = assignedClass
                            },
                            onMarkAttendance = {
                                openMarkAttendance(null)
                            },
                            onAttendanceHistory = {
                                isAttendanceHistoryDialogOpen = true
                            },
                            onNoticeClick = { notice ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Notice: ${notice.title}")
                                }
                            },
                        )
                    }

                    DashboardTab.CLASSES -> {
                        ClassesTabContent(
                            classes = state.classes,
                            onViewClass = { assignedClass ->
                                selectedClassForDetails = assignedClass
                            },
                        )
                    }

                    DashboardTab.ATTENDANCE -> {
                        AttendanceTabContent(
                            attendance = state.attendance,
                            classes = state.classes,
                            onMarkAttendanceClick = {
                                openMarkAttendance(null)
                            },
                            onHistoryClick = {
                                isAttendanceHistoryDialogOpen = true
                            },
                            onClassClick = { assignedClass ->
                                selectedClassForDetails = assignedClass
                            },
                        )
                    }

                    DashboardTab.PROFILE -> {
                        ProfileTabContent(
                            profile = state.profile,
                            onLogoutClick = onLogoutClick,
                        )
                    }
                }
            }
        }
    }

    // Modal 1: Functional View Class Details Dialog
    selectedClassForDetails?.let { assignedClass ->
        val roster = remember(assignedClass.id) {
            repository.getClassRoster(assignedClass.id)
        }
        ClassDetailsDialog(
            assignedClass = assignedClass,
            roster = roster,
            onDismiss = { selectedClassForDetails = null },
            onMarkAttendanceClick = {
                selectedClassForDetails = null
                openMarkAttendance(assignedClass)
            },
        )
    }

    // Modal 2: Functional Attendance History Dialog
    if (isAttendanceHistoryDialogOpen) {
        AttendanceHistoryDialog(
            history = state.history,
            currentRate = state.attendance.markedAttendancePercentage,
            onDismiss = { isAttendanceHistoryDialogOpen = false },
        )
    }
}

/**
 * 1. Home Tab:
 * Displays Header, Today's Overview, Attendance summary card, My Classes cards, School Notices.
 */
@Composable
private fun DashboardHomeContent(
    state: DashboardUiState,
    onViewClass: (AssignedClass) -> Unit,
    onMarkAttendance: () -> Unit,
    onAttendanceHistory: () -> Unit,
    onNoticeClick: (SchoolNotice) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        // Section 1: Header
        item {
            DashboardHeaderSection(profile = state.profile)
        }

        // Section 2: Today's Overview (Accurately calculated from marked students)
        item {
            TodayOverviewSection(
                overview = state.overview,
                attendance = state.attendance,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
        }

        // Section 3: Attendance Summary with 35 Unmarked Students highlighted
        item {
            AttendanceSection(
                summary = state.attendance,
                onMarkAttendance = onMarkAttendance,
                onAttendanceHistory = onAttendanceHistory,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
        }

        // Section 4: My Classes with scrollable cards
        item {
            MyClassesHeader(
                totalClasses = state.classes.size,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
            )
        }

        items(state.classes, key = { it.id }) { assignedClass ->
            ClassCardItem(
                assignedClass = assignedClass,
                onViewClass = { onViewClass(assignedClass) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
            )
        }

        // Section 5: School Notices
        item {
            SchoolNoticesSection(
                notices = state.notices,
                onNoticeClick = onNoticeClick,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            )
        }
    }
}

/**
 * 2. Header Section
 */
@Composable
fun DashboardHeaderSection(
    profile: TeacherProfile,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AivyaPastelBlue, AivyaPastelBlueLight),
                ),
            )
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Aivya Brand Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(20.dp),
                        )
                        .border(1.dp, AivyaBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.School,
                        contentDescription = null,
                        tint = AivyaNavy,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AIVYA",
                        color = AivyaNavy,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                    )
                    Text(
                        text = "  •  TEACHER PORTAL",
                        color = AivyaSlate,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                // Profile Avatar & Notification Bell
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, AivyaBorder, CircleShape),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = AivyaNavy,
                            modifier = Modifier.size(19.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(2.dp, CircleShape)
                            .background(AivyaNavy, CircleShape)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "T",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Good morning, Teacher!",
                color = AivyaNavyDark,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${profile.department}  •  ${profile.schoolName}",
                color = AivyaSlate,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/**
 * 3. Today's Overview Section:
 * Accurate attendance percentage calculated strictly from marked students.
 */
@Composable
fun TodayOverviewSection(
    overview: TeacherOverviewState,
    attendance: AttendanceSummaryState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Today's Overview",
                color = AivyaNavy,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )

            if (attendance.unmarkedCount > 0) {
                Box(
                    modifier = Modifier
                        .background(AivyaAmberSoft, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = "${attendance.unmarkedCount} Unmarked",
                        color = AivyaAmberAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // 1. Total Assigned Classes
            OverviewMetricCard(
                title = "Classes",
                value = overview.totalAssignedClasses.toString(),
                subtitle = "Assigned",
                icon = Icons.Outlined.School,
                accentColor = AivyaNavyLight,
                backgroundColor = AivyaPastelBlue,
                modifier = Modifier.weight(1f),
            )

            // 2. Total Students
            OverviewMetricCard(
                title = "Students",
                value = overview.totalStudents.toString(),
                subtitle = "Enrolled",
                icon = Icons.Outlined.Groups,
                accentColor = AivyaPinkAccent,
                backgroundColor = AivyaPinkSoft,
                modifier = Modifier.weight(1f),
            )

            // 3. Attendance Percentage: Strictly calculated from marked students (101/107 = 94.4%)
            val rateText = if (attendance.totalMarkedStudents > 0) {
                "%.1f%%".format(attendance.markedAttendancePercentage)
            } else {
                "0.0%"
            }
            OverviewMetricCard(
                title = "Attendance",
                value = rateText,
                subtitle = "Marked (${attendance.presentCount}/${attendance.totalMarkedStudents})",
                icon = Icons.Outlined.CheckCircle,
                accentColor = AivyaGreenAccent,
                backgroundColor = AivyaGreenSoft,
                modifier = Modifier.weight(1.2f),
            )
        }
    }
}

@Composable
private fun OverviewMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.shadow(
            elevation = 2.dp,
            shape = RoundedCornerShape(16.dp),
            ambientColor = AivyaNavy.copy(alpha = 0.05f),
            spotColor = AivyaNavy.copy(alpha = 0.1f),
        ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AivyaCardBackground),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(AivyaBorder, AivyaBorder)),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(backgroundColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = value,
                color = AivyaNavy,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "$title ($subtitle)",
                color = AivyaSlate,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * 4. Attendance Section:
 * Accurately shows 101 Present, 6 Absent, 35 Unmarked, and accurate percentage based on marked students.
 */
@Composable
fun AttendanceSection(
    summary: AttendanceSummaryState,
    onMarkAttendance: () -> Unit,
    onAttendanceHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = AivyaNavy.copy(alpha = 0.05f),
                spotColor = AivyaNavy.copy(alpha = 0.12f),
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AivyaCardBackground),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(AivyaBorder, AivyaBorder)),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(AivyaPastelBlue, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.HowToReg,
                            contentDescription = null,
                            tint = AivyaNavy,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Daily Attendance",
                            color = AivyaNavy,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "Today's summary: ${summary.lastUpdatedTime}",
                            color = AivyaSlate,
                            fontSize = 11.5.sp,
                        )
                    }
                }

                Text(
                    text = "${summary.classesMarkedToday}/${summary.totalClassesToday} Classes Marked",
                    color = if (summary.isAllMarked) AivyaGreenAccent else AivyaPinkAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar: Marked Classes / Total Classes
            val progressFraction = summary.classesMarkedToday.toFloat() / summary.totalClassesToday.coerceAtLeast(1)
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = AivyaNavy,
                trackColor = AivyaPastelBlue,
                strokeCap = StrokeCap.Round,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Detailed Attendance Breakdown: 101 Present, 6 Absent, 35 Unmarked
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AivyaScreenBackground, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 1. Present
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(AivyaGreenAccent, CircleShape),
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${summary.presentCount} Present",
                        color = AivyaTextPrimary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                // 2. Absent
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(AivyaPinkAccent, CircleShape),
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${summary.absentCount} Absent",
                        color = AivyaTextPrimary,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                // 3. 35 Unmarked Students (Explicitly shown)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(AivyaAmberAccent, CircleShape),
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${summary.unmarkedCount} Unmarked",
                        color = AivyaAmberAccent,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Accurate Rate Description
            Text(
                text = "Attendance rate: %.1f%% based on %d marked students (%d pending roll-call)".format(
                    summary.markedAttendancePercentage,
                    summary.totalMarkedStudents,
                    summary.unmarkedCount,
                ),
                color = AivyaSlate,
                fontSize = 11.sp,
                lineHeight = 16.sp,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = onMarkAttendance,
                    modifier = Modifier
                        .weight(1.3f)
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AivyaNavy,
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.HowToReg,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mark Attendance",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                OutlinedButton(
                    onClick = onAttendanceHistory,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AivyaBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AivyaNavy,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "History",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

/**
 * 5. My Classes Section:
 * Header and Class card item displaying class name, section, student count, and functional View Class action.
 */
@Composable
fun MyClassesHeader(
    totalClasses: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "My Classes",
                color = AivyaNavy,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .background(AivyaPastelBlue, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    text = "$totalClasses Active",
                    color = AivyaNavy,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Text(
            text = "Academic Schedule",
            color = AivyaSlate,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun ClassStatusBadge(assignedClass: AssignedClass) {
    if (assignedClass.attendanceStatus == ClassAttendanceStatus.MARKED) {
        StatusPill(text = "Marked", background = AivyaGreenSoft, color = AivyaGreenAccent)
        return
    }

    when (assignedClass.computeTimeStatus()) {
        com.aivya.schoolmanagement.dashboard.ClassTimeStatus.ONGOING ->
            StatusPill(text = "Ongoing", background = AivyaPastelBlue, color = AivyaNavyLight)
        com.aivya.schoolmanagement.dashboard.ClassTimeStatus.UPCOMING ->
            StatusPill(text = "Upcoming", background = AivyaPastelBlueLight, color = AivyaSlate)
        com.aivya.schoolmanagement.dashboard.ClassTimeStatus.MISSED ->
            StatusPill(text = "Missed", background = AivyaPinkSoft, color = AivyaPinkAccent)
        com.aivya.schoolmanagement.dashboard.ClassTimeStatus.NONE ->
            StatusPill(text = "Pending", background = AivyaAmberSoft, color = AivyaAmberAccent)
    }
}

@Composable
private fun StatusPill(text: String, background: Color, color: Color) {
    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(text = text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ClassCardItem(
    assignedClass: AssignedClass,
    onViewClass: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onViewClass)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = AivyaNavy.copy(alpha = 0.04f),
                spotColor = AivyaNavy.copy(alpha = 0.08f),
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AivyaCardBackground),
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
                            text = assignedClass.className,
                            color = AivyaNavy,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(AivyaPastelBlue, RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = assignedClass.section,
                                color = AivyaNavy,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        // Status badge: Marked, or a time-aware Ongoing/Upcoming/Missed/Pending
                        Spacer(modifier = Modifier.width(6.dp))
                        ClassStatusBadge(assignedClass = assignedClass)
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = assignedClass.subject,
                        color = AivyaSlate,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                // Student count pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(AivyaScreenBackground, RoundedCornerShape(10.dp))
                        .border(1.dp, AivyaBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Groups,
                        contentDescription = null,
                        tint = AivyaNavyLight,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = assignedClass.studentCount.toString(),
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

                // Functional View Class Action Button
                Button(
                    onClick = onViewClass,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AivyaPastelBlue,
                        contentColor = AivyaNavy,
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp),
                ) {
                    Text(
                        text = "View Class",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }
    }
}

/**
 * 6. School Notices Section
 */
@Composable
fun SchoolNoticesSection(
    notices: List<SchoolNotice>,
    onNoticeClick: (SchoolNotice) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "School Notices",
                    color = AivyaNavy,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.Campaign,
                    contentDescription = null,
                    tint = AivyaPinkAccent,
                    modifier = Modifier.size(18.dp),
                )
            }

            Text(
                text = "Recent Updates",
                color = AivyaSlate,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(18.dp),
                    ambientColor = AivyaNavy.copy(alpha = 0.04f),
                    spotColor = AivyaNavy.copy(alpha = 0.08f),
                ),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = AivyaCardBackground),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(AivyaBorder, AivyaBorder)),
            ),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                notices.forEachIndexed { index, notice ->
                    NoticeItemRow(
                        notice = notice,
                        onClick = { onNoticeClick(notice) },
                    )
                    if (index < notices.lastIndex) {
                        HorizontalDivider(
                            color = AivyaBorder,
                            thickness = 0.8.dp,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoticeItemRow(
    notice: SchoolNotice,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(
                            if (notice.isImportant) AivyaPinkSoft else AivyaPastelBlue,
                            RoundedCornerShape(6.dp),
                        )
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = notice.category,
                        color = if (notice.isImportant) AivyaPinkAccent else AivyaNavy,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = notice.dateText,
                    color = AivyaSlate,
                    fontSize = 11.5.sp,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = notice.title,
                color = AivyaNavy,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = notice.summary,
                color = AivyaSlate,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = AivyaSlate.copy(alpha = 0.7f),
            modifier = Modifier
                .padding(top = 18.dp)
                .size(18.dp),
        )
    }
}

/**
 * 7. Bottom Navigation Bar (Functional)
 */
@Composable
fun DashboardBottomNavigation(
    selectedTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                ambientColor = AivyaNavy.copy(alpha = 0.08f),
                spotColor = AivyaNavy.copy(alpha = 0.16f),
            )
            .navigationBarsPadding(),
        containerColor = Color.White,
        tonalElevation = 6.dp,
    ) {
        DashboardTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        modifier = Modifier.size(22.dp),
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AivyaNavy,
                    selectedTextColor = AivyaNavy,
                    unselectedIconColor = AivyaSlate,
                    unselectedTextColor = AivyaSlate,
                    indicatorColor = AivyaPastelBlue,
                ),
            )
        }
    }
}

/**
 * Functional Content for Tab: CLASSES
 */
@Composable
private fun ClassesTabContent(
    classes: List<AssignedClass>,
    onViewClass: (AssignedClass) -> Unit,
) {
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredClasses = remember(classes, selectedFilter) {
        when (selectedFilter) {
            "Marked" -> classes.filter { it.attendanceStatus == ClassAttendanceStatus.MARKED }
            "Pending" -> classes.filter { it.attendanceStatus == ClassAttendanceStatus.PENDING }
            else -> classes
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
    ) {
        item {
            Text(
                text = "My Assigned Classes",
                color = AivyaNavy,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Manage schedules, student rosters, and roll-calls",
                color = AivyaSlate,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("All", "Marked", "Pending").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            val count = when (filter) {
                                "Marked" -> classes.count { it.attendanceStatus == ClassAttendanceStatus.MARKED }
                                "Pending" -> classes.count { it.attendanceStatus == ClassAttendanceStatus.PENDING }
                                else -> classes.size
                            }
                            Text(text = "$filter ($count)")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AivyaNavy,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = AivyaNavy,
                        ),
                        border = BorderStroke(1.dp, if (isSelected) AivyaNavy else AivyaBorder),
                        shape = RoundedCornerShape(12.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        items(filteredClasses, key = { it.id }) { assignedClass ->
            ClassCardItem(
                assignedClass = assignedClass,
                onViewClass = { onViewClass(assignedClass) },
                modifier = Modifier.padding(vertical = 6.dp),
            )
        }
    }
}

/**
 * Functional Content for Tab: ATTENDANCE HUB
 */
@Composable
private fun AttendanceTabContent(
    attendance: AttendanceSummaryState,
    classes: List<AssignedClass>,
    onMarkAttendanceClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onClassClick: (AssignedClass) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
    ) {
        item {
            Text(
                text = "Attendance Management",
                color = AivyaNavy,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Daily attendance overview & student roll-call records",
                color = AivyaSlate,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Attendance Section Card
            AttendanceSection(
                summary = attendance,
                onMarkAttendance = onMarkAttendanceClick,
                onAttendanceHistory = onHistoryClick,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Class Roll-Call Status",
                color = AivyaNavy,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        items(classes, key = { it.id }) { assignedClass ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onClassClick(assignedClass) },
                shape = RoundedCornerShape(16.dp),
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
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "${assignedClass.className} • ${assignedClass.section}",
                            color = AivyaNavy,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "${assignedClass.subject}  •  ${assignedClass.studentCount} Students",
                            color = AivyaSlate,
                            fontSize = 12.sp,
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        if (assignedClass.attendanceStatus == ClassAttendanceStatus.MARKED) {
                            Text(
                                text = "Marked: ${assignedClass.presentCount} Present, ${assignedClass.absentCount} Absent",
                                color = AivyaGreenAccent,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        } else {
                            Text(
                                text = "Pending Roll-Call: ${assignedClass.unmarkedCount} Unmarked Students",
                                color = AivyaAmberAccent,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    if (assignedClass.attendanceStatus == ClassAttendanceStatus.PENDING) {
                        Button(
                            onClick = onMarkAttendanceClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AivyaNavy,
                                contentColor = Color.White,
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp),
                        ) {
                            Text(text = "Mark Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onClassClick(assignedClass) },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, AivyaBorder),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp),
                        ) {
                            Text(text = "View", color = AivyaNavy, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Functional Content for Tab: PROFILE
 */
@Composable
private fun ProfileTabContent(
    profile: TeacherProfile,
    onLogoutClick: () -> Unit,
) {
    var notificationEnabled by remember { mutableStateOf(true) }
    var classAlertsEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(4.dp, CircleShape)
                .background(AivyaNavy, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "T",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = profile.displayName,
            color = AivyaNavy,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = profile.roleTitle,
            color = AivyaSlate,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Faculty Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(AivyaBorder, AivyaBorder)),
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "FACULTY DETAILS",
                    color = AivyaSlate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))
                ProfileInfoRow(label = "Employee ID", value = profile.id)
                HorizontalDivider(color = AivyaBorder, modifier = Modifier.padding(vertical = 10.dp))
                ProfileInfoRow(label = "Email Address", value = profile.email)
                HorizontalDivider(color = AivyaBorder, modifier = Modifier.padding(vertical = 10.dp))
                ProfileInfoRow(label = "Department", value = profile.department)
                HorizontalDivider(color = AivyaBorder, modifier = Modifier.padding(vertical = 10.dp))
                ProfileInfoRow(label = "Institution", value = profile.schoolName)
                HorizontalDivider(color = AivyaBorder, modifier = Modifier.padding(vertical = 10.dp))
                ProfileInfoRow(label = "Active Session", value = profile.academicTerm)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Academic Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(AivyaBorder, AivyaBorder)),
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PREFERENCES",
                    color = AivyaSlate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Daily Roll-Call Reminders",
                        color = AivyaNavy,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = { notificationEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AivyaNavy,
                        ),
                    )
                }

                HorizontalDivider(color = AivyaBorder, modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "School Announcement Alerts",
                        color = AivyaNavy,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Switch(
                        checked = classAlertsEnabled,
                        onCheckedChange = { classAlertsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AivyaNavy,
                        ),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign Out Button
        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, AivyaPinkAccent.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AivyaPinkAccent),
        ) {
            Text(
                text = "Sign Out from Teacher Portal",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, color = AivyaSlate, fontSize = 12.5.sp)
        Text(
            text = value,
            color = AivyaNavy,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun WeeklyTimetableSection(assignedClass: AssignedClass) {
    val today = java.time.DayOfWeek.from(java.time.LocalDate.now())
    val timetable = assignedClass.weeklyTimetable()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AivyaScreenBackground, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        java.time.DayOfWeek.entries.forEach { day ->
            val slots = timetable[day].orEmpty()
            val isToday = day == today
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = day.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = if (isToday) AivyaNavy else AivyaSlate,
                    fontSize = 12.sp,
                    fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                )
                Text(
                    text = if (slots.isEmpty()) {
                        "No class"
                    } else {
                        slots.joinToString(" • ") { "${it.startTime} - ${it.endTime}" }
                    },
                    color = if (slots.isEmpty()) AivyaSlate.copy(alpha = 0.6f) else AivyaTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = if (slots.isEmpty()) FontWeight.Normal else FontWeight.Bold,
                )
            }
        }
    }
}

/**
 * ============================================================================
 * INTERACTIVE DIALOG 1: Class Details Dialog
 * ============================================================================
 */
@Composable
private fun ClassDetailsDialog(
    assignedClass: AssignedClass,
    roster: List<StudentRosterItem>,
    onDismiss: () -> Unit,
    onMarkAttendanceClick: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .widthIn(max = 520.dp)
            .heightIn(max = 680.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "${assignedClass.className} • ${assignedClass.section}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AivyaNavy,
                    )
                    Text(
                        text = assignedClass.subject,
                        fontSize = 13.sp,
                        color = AivyaSlate,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = "Close", tint = AivyaSlate)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                // Class Metadata Summary
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AivyaPastelBlueLight, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(text = "Schedule", color = AivyaSlate, fontSize = 11.sp)
                        Text(text = assignedClass.scheduleTime, color = AivyaNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(text = "Room", color = AivyaSlate, fontSize = 11.sp)
                        Text(text = assignedClass.roomNumber, color = AivyaNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(text = "Roster", color = AivyaSlate, fontSize = 11.sp)
                        Text(text = "${assignedClass.studentCount} Students", color = AivyaNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status Banner
                if (assignedClass.attendanceStatus == ClassAttendanceStatus.MARKED) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AivyaGreenSoft, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                    ) {
                        Text(
                            text = "Today's Roll-Call: ${assignedClass.presentCount} Present  •  ${assignedClass.absentCount} Absent",
                            color = AivyaGreenAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AivyaAmberSoft, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                    ) {
                        Text(
                            text = "Today's Roll-Call: Pending (${assignedClass.unmarkedCount} Unmarked)",
                            color = AivyaAmberAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "WEEKLY TIMETABLE",
                    color = AivyaSlate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                )

                Spacer(modifier = Modifier.height(8.dp))

                WeeklyTimetableSection(assignedClass)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "STUDENT ROSTER PREVIEW (${roster.size} Enrolled)",
                    color = AivyaSlate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable roster preview
                roster.take(15).forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "#%02d".format(item.rollNumber),
                                color = AivyaSlate,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(36.dp),
                            )
                            Text(
                                text = item.studentName,
                                color = AivyaTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }

                        val badgeBg = when (item.status) {
                            "Present" -> AivyaGreenSoft
                            "Absent" -> AivyaPinkSoft
                            else -> AivyaPastelBlue
                        }
                        val badgeColor = when (item.status) {
                            "Present" -> AivyaGreenAccent
                            "Absent" -> AivyaPinkAccent
                            else -> AivyaSlate
                        }

                        Box(
                            modifier = Modifier
                                .background(badgeBg, RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = item.status,
                                color = badgeColor,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                if (roster.size > 15) {
                    Text(
                        text = "+ ${roster.size - 15} more students enrolled",
                        color = AivyaSlate,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        },
        confirmButton = {
            if (assignedClass.attendanceStatus == ClassAttendanceStatus.PENDING) {
                Button(
                    onClick = onMarkAttendanceClick,
                    colors = ButtonDefaults.buttonColors(containerColor = AivyaNavy),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(text = "Mark Roll-Call Now", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AivyaNavy),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(text = "Done", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, AivyaBorder),
            ) {
                Text(text = "Close", color = AivyaNavy, fontSize = 12.5.sp)
            }
        },
    )
}

/**
 * ============================================================================
 * INTERACTIVE DIALOG 3: Attendance History Dialog
 * ============================================================================
 */
@Composable
private fun AttendanceHistoryDialog(
    history: List<AttendanceHistoryRecord>,
    currentRate: Float,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Attendance History",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AivyaNavy,
                    )
                    Text(
                        text = "Current week attendance logs",
                        fontSize = 12.sp,
                        color = AivyaSlate,
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = "Close", tint = AivyaSlate)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                // Average summary box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AivyaPastelBlue, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(text = "Today's Marked Rate", color = AivyaSlate, fontSize = 11.sp)
                            Text(
                                text = "%.1f%% Attendance".format(currentRate),
                                color = AivyaNavy,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = AivyaGreenAccent,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "PREVIOUS SESSIONS",
                    color = AivyaSlate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                )

                Spacer(modifier = Modifier.height(8.dp))

                history.forEach { record ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(AivyaBorder, AivyaBorder)),
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text(
                                    text = "${record.dayName} (${record.dateText})",
                                    color = AivyaNavy,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "${record.presentCount} of ${record.totalStudents} Students Present",
                                    color = AivyaSlate,
                                    fontSize = 11.5.sp,
                                )
                            }

                            Text(
                                text = "${record.percentage}%",
                                color = AivyaGreenAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AivyaNavy),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(text = "Close", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        },
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun TeacherDashboardPreview() {
    AivyaSchoolManagementTheme {
        TeacherDashboardScreen()
    }
}
