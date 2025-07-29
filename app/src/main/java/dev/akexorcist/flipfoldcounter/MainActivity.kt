package dev.akexorcist.flipfoldcounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dev.akexorcist.flipfoldcounter.ui.navigation.NavGraph
import dev.akexorcist.flipfoldcounter.ui.theme.FlipFoldCounterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlipFoldCounterTheme {
                NavGraph()
            }
        }
    }
}
