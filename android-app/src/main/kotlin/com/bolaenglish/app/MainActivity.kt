package com.bolaenglish.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bolaenglish.app.ui.navigation.BolaNavigation
import com.bolaenglish.app.ui.theme.BolaEnglishTheme

import androidx.activity.viewModels
import com.bolaenglish.app.ui.onboarding.OnboardingViewModel

class MainActivity : ComponentActivity() {
    private val onboarding: OnboardingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BolaEnglishTheme { BolaNavigation(onboarding) }
        }
    }
}
