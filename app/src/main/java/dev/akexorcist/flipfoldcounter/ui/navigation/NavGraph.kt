package dev.akexorcist.flipfoldcounter.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import dev.akexorcist.flipfoldcounter.R
import dev.akexorcist.flipfoldcounter.ui.instruction.InstructionRoute
import dev.akexorcist.flipfoldcounter.ui.main.MainRoute
import kotlinx.serialization.Serializable

@Serializable
sealed class Screen: NavKey {
    @Serializable
    data object Main : Screen()

    @Serializable
    data object Instruction : Screen()
}

@Composable
fun NavGraph() {
    val context = LocalContext.current
    val backStack = rememberNavBackStack(Screen.Main)
    NavDisplay(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { key ->
            when (key) {
                is Screen.Main -> NavEntry(key) {
                    MainRoute(backStack)
                }

                is Screen.Instruction -> NavEntry(key) {
                    InstructionRoute(backStack)
                }

                else -> {
                    error(context.getString(R.string.main_error_unknown_route, key))
                }
            }
        }
    )
}
