package com.bolaenglish.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bolaenglish.app.R
import com.bolaenglish.app.ui.LessonScreen

@Composable
fun BolaNavigation() {
    val controller = rememberNavController()
    Scaffold { padding ->
        NavHost(controller, startDestination = "lessonScreen", modifier = Modifier.padding(padding)) {
            composable("lessonScreen") { LessonScreen() }
            composable("progressScreen") { Text(stringResource(R.string.progress_screen)) }
            composable("onboardingScreen") { Text(stringResource(R.string.onboarding_screen)) }
            composable("settingsScreen") { Text(stringResource(R.string.settings_screen)) }
        }
    }
}
