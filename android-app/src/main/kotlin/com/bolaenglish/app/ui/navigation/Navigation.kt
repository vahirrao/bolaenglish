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
import com.bolaenglish.app.ui.LessonScreen
import com.bolaenglish.app.ui.onboarding.*

@Composable
fun BolaNavigation(viewModel: OnboardingViewModel) {
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
                    NavHost(rememberNavController(), startDestination = "lessonScreen") {
                        composable("lessonScreen") { LessonScreen() }
                        composable("progressScreen") { Text(stringResource(R.string.progress_screen)) }
                        composable("settingsScreen") { Text(stringResource(R.string.settings_screen)) }
                    }
                }
            }
        }
    }
}
