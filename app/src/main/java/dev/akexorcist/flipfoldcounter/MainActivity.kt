package dev.akexorcist.flipfoldcounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.akexorcist.flipfoldcounter.ui.navigation.NavGraph
import dev.akexorcist.flipfoldcounter.ui.theme.FlipFoldCounterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlipFoldCounterTheme {
                NavGraph()
            }
        }
    }
}