package com.example.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalStrings
import com.example.ui.i18n.stringsFor
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BlinkDetectionScreen
import com.example.ui.screens.ColorVisionScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EyeCareScreen
import com.example.ui.screens.EyeMovementScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.VisualAcuityScreen
import com.example.ui.theme.VisionScanTheme

object VisionScanRoutes {
    const val DASHBOARD = "dashboard"
    const val HISTORY = "history"
    const val EYE_CARE = "eye_care"
    const val PROFILE = "profile"
    const val VISUAL_ACUITY = "visual_acuity"
    const val COLOR_VISION = "color_vision"
    const val BLINK_DETECTION = "blink_detection"
    const val EYE_MOVEMENT = "eye_movement"
    const val SCREENING_REPORT = "screening_report"
}

private data class NavTabItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun VisionScanApp(viewModel: VisionScanViewModel) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val history by viewModel.screeningHistory.collectAsStateWithLifecycle()
    val selectedReportId by viewModel.selectedReportSessionId.collectAsStateWithLifecycle()
    val aiReportState by viewModel.aiReportState.collectAsStateWithLifecycle()

    val strings = stringsFor(preferences.language)

    VisionScanTheme(
        darkTheme = preferences.isDarkMode,
        largeTextMode = preferences.largeTextMode
    ) {
        CompositionLocalProvider(LocalStrings provides strings) {
            val toggleLang = {
                val next = if (preferences.language == AppLanguage.ENGLISH) AppLanguage.TAMIL else AppLanguage.ENGLISH
                viewModel.setLanguage(next)
            }
            val toggleDark = { viewModel.toggleDarkMode(!preferences.isDarkMode) }

            val profile = activeProfile
            if (profile == null) {
                AuthScreen(
                    profiles = allProfiles,
                    language = preferences.language,
                    isDarkMode = preferences.isDarkMode,
                    onToggleLanguage = toggleLang,
                    onToggleDarkMode = toggleDark,
                    onRegisterProfile = { name, age, email, glasses ->
                        viewModel.registerProfile(name, age, email, glasses)
                    },
                    onSelectProfile = { viewModel.selectProfile(it) },
                    onEnterDemoMode = { viewModel.enterDemoMode() }
                )
            } else {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route ?: VisionScanRoutes.DASHBOARD

                val mainTabs = listOf(
                    NavTabItem(
                        route = VisionScanRoutes.DASHBOARD,
                        label = strings.navDashboard,
                        selectedIcon = Icons.Rounded.Dashboard,
                        unselectedIcon = Icons.Outlined.Dashboard,
                        testTag = "nav_tab_dashboard"
                    ),
                    NavTabItem(
                        route = VisionScanRoutes.HISTORY,
                        label = strings.navHistory,
                        selectedIcon = Icons.Rounded.History,
                        unselectedIcon = Icons.Outlined.History,
                        testTag = "nav_tab_history"
                    ),
                    NavTabItem(
                        route = VisionScanRoutes.EYE_CARE,
                        label = strings.navEyeCare,
                        selectedIcon = Icons.Rounded.HealthAndSafety,
                        unselectedIcon = Icons.Outlined.HealthAndSafety,
                        testTag = "nav_tab_eye_care"
                    ),
                    NavTabItem(
                        route = VisionScanRoutes.PROFILE,
                        label = strings.navProfile,
                        selectedIcon = Icons.Rounded.Person,
                        unselectedIcon = Icons.Outlined.Person,
                        testTag = "nav_tab_profile"
                    )
                )

                val showChrome = currentRoute in setOf(
                    VisionScanRoutes.DASHBOARD,
                    VisionScanRoutes.HISTORY,
                    VisionScanRoutes.EYE_CARE,
                    VisionScanRoutes.PROFILE
                )

                val activeSession = history.firstOrNull()
                val reportSession = selectedReportId?.let { id -> history.firstOrNull { it.id == id } } ?: activeSession

                fun navigateToTab(route: String) {
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val useNavigationRail = maxWidth >= 700.dp

                    Row(modifier = Modifier.fillMaxSize()) {
                        if (showChrome && useNavigationRail) {
                            NavigationRail(
                                containerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                            ) {
                                mainTabs.forEach { tab ->
                                    val selected = currentRoute == tab.route
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { navigateToTab(tab.route) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                contentDescription = tab.label
                                            )
                                        },
                                        label = { Text(tab.label) },
                                        modifier = Modifier.testTag(tab.testTag)
                                    )
                                }
                            }
                        }

                        Scaffold(
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.background,
                            contentWindowInsets = WindowInsets.statusBars,
                            bottomBar = {
                                if (showChrome && !useNavigationRail) {
                                    NavigationBar(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        windowInsets = WindowInsets.navigationBars
                                    ) {
                                        mainTabs.forEach { tab ->
                                            val selected = currentRoute == tab.route
                                            NavigationBarItem(
                                                selected = selected,
                                                onClick = { navigateToTab(tab.route) },
                                                icon = {
                                                    Icon(
                                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                                        contentDescription = tab.label
                                                    )
                                                },
                                                label = { Text(tab.label, maxLines = 1) },
                                                modifier = Modifier.testTag(tab.testTag)
                                            )
                                        }
                                    }
                                }
                            }
                        ) { innerPadding ->
                            NavHost(
                                navController = navController,
                                startDestination = VisionScanRoutes.DASHBOARD,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(if (showChrome) innerPadding else androidx.compose.foundation.layout.PaddingValues(0.dp))
                            ) {
                                composable(VisionScanRoutes.DASHBOARD) {
                                    DashboardScreen(
                                        profile = profile,
                                        activeSession = activeSession,
                                        history = history,
                                        language = preferences.language,
                                        isDarkMode = preferences.isDarkMode,
                                        onToggleLanguage = toggleLang,
                                        onToggleDarkMode = toggleDark,
                                        onStartNewSession = { viewModel.startNewSession() },
                                        onOpenAcuityTest = { navController.navigate(VisionScanRoutes.VISUAL_ACUITY) },
                                        onOpenColorTest = { navController.navigate(VisionScanRoutes.COLOR_VISION) },
                                        onOpenBlinkTest = { navController.navigate(VisionScanRoutes.BLINK_DETECTION) },
                                        onOpenMovementTest = { navController.navigate(VisionScanRoutes.EYE_MOVEMENT) },
                                        onOpenReport = {
                                            viewModel.openReportForSession(activeSession?.id)
                                            navController.navigate(VisionScanRoutes.SCREENING_REPORT)
                                        }
                                    )
                                }

                                composable(VisionScanRoutes.HISTORY) {
                                    HistoryScreen(
                                        sessions = history,
                                        onOpenReport = { sessionId ->
                                            viewModel.openReportForSession(sessionId)
                                            navController.navigate(VisionScanRoutes.SCREENING_REPORT)
                                        },
                                        onDeleteSession = { viewModel.deleteSession(it) },
                                        onClearAllHistory = { viewModel.clearAllHistory() }
                                    )
                                }

                                composable(VisionScanRoutes.EYE_CARE) {
                                    EyeCareScreen(language = preferences.language)
                                }

                                composable(VisionScanRoutes.PROFILE) {
                                    ProfileScreen(
                                        profile = profile,
                                        preferences = preferences,
                                        onUpdateProfile = { name, age, email, glasses ->
                                            viewModel.updateProfile(name, age, email, glasses)
                                        },
                                        onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                                        onSelectLanguage = { viewModel.setLanguage(it) },
                                        onToggleLargeText = { viewModel.toggleLargeText(it) },
                                        onToggleCameraConsent = { viewModel.setCameraConsent(it) },
                                        onToggleScreenBreakReminder = { viewModel.setScreenBreakReminder(it) },
                                        onClearAllHistory = { viewModel.clearAllHistory() },
                                        onLogout = { viewModel.logout() }
                                    )
                                }

                                composable(VisionScanRoutes.VISUAL_ACUITY) {
                                    VisualAcuityScreen(
                                        onBack = { navController.popBackStack() },
                                        onSaveResult = { rLvl, rCor, rTot, lLvl, lCor, lTot, scale, label ->
                                            viewModel.saveVisualAcuityResult(
                                                rLvl, rCor, rTot, lLvl, lCor, lTot, scale, label
                                            )
                                        }
                                    )
                                }

                                composable(VisionScanRoutes.COLOR_VISION) {
                                    ColorVisionScreen(
                                        onBack = { navController.popBackStack() },
                                        onSaveResult = { cor, tot, details, label ->
                                            viewModel.saveColorVisionResult(cor, tot, details, label)
                                        }
                                    )
                                }

                                composable(VisionScanRoutes.BLINK_DETECTION) {
                                    BlinkDetectionScreen(
                                        cameraConsentGranted = preferences.cameraConsentGranted,
                                        onGrantConsent = { viewModel.setCameraConsent(true) },
                                        onBack = { navController.popBackStack() },
                                        onSaveResult = { blinks, dur, bpm, avgOpen, label ->
                                            viewModel.saveBlinkResult(blinks, dur, bpm, avgOpen, label)
                                        }
                                    )
                                }

                                composable(VisionScanRoutes.EYE_MOVEMENT) {
                                    EyeMovementScreen(
                                        cameraConsentGranted = preferences.cameraConsentGranted,
                                        onGrantConsent = { viewModel.setCameraConsent(true) },
                                        onBack = { navController.popBackStack() },
                                        onSaveResult = { tracked, total, ratio, label ->
                                            viewModel.saveEyeMovementResult(tracked, total, ratio, label)
                                        }
                                    )
                                }

                                composable(VisionScanRoutes.SCREENING_REPORT) {
                                    ReportScreen(
                                        profile = profile,
                                        session = reportSession,
                                        aiState = aiReportState,
                                        onGenerateAiReport = { viewModel.generateAiReport(it) },
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
