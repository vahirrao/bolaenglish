package com.bolaenglish.app.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bolaenglish.app.R
import com.bolaenglish.app.ui.*
import com.bolaenglish.app.ui.onboarding.*

@Composable
fun BolaNavigation(viewModel: OnboardingViewModel, lessons: LessonViewModel) {
    val launchState by viewModel.launchState.collectAsStateWithLifecycle()
    Scaffold { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (launchState) {
                LaunchState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                LaunchState.Onboarding -> {
                    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
                    OnboardingScreen(saveState, viewModel::finish)
                }
                LaunchState.Lessons -> {
                    val state by lessons.state.collectAsStateWithLifecycle()
                    val data = state
                    if (data == null || data.profile == null) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                        return@Box
                    }
                    val error by lessons.error.collectAsStateWithLifecycle()
                    if (error != null) {
                        androidx.compose.material3.AlertDialog(onDismissRequest = lessons::clearError,
                            text = { Text(error.orEmpty()) }, confirmButton = {
                                androidx.compose.material3.TextButton(onClick = lessons::clearError) { Text("OK") }
                            })
                    }
                    val controller = rememberNavController()
                    NavHost(controller, startDestination = "lessonScreen") {
                        composable("lessonScreen") {
                            LessonHub(data, lessons.modules, { controller.navigate("lesson/$it") }, { controller.navigate("review") })
                        }
                        composable("lesson/{id}") { entry ->
                            val module = lessons.modules.first { it.id == entry.arguments?.getString("id") }
                            LessonFlowScreen(module, data, lessons) { controller.popBackStack() }
                        }
                        composable("review") {
                            ReviewScreen(data.due, lessons, data.profile.nativeLanguage) { controller.popBackStack() }
                        }
                        composable("progressScreen") { Text(stringResource(R.string.progress_screen)) }
                        composable("settingsScreen") { Text(stringResource(R.string.settings_screen)) }
                    }
                }
            }
        }
    }
}
