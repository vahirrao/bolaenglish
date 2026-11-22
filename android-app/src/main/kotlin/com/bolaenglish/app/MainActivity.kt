package com.bolaenglish.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bolaenglish.app.ui.navigation.BolaNavigation
import com.bolaenglish.app.ui.theme.BolaEnglishTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BolaEnglishTheme { BolaNavigation() }
        }
    }
}
