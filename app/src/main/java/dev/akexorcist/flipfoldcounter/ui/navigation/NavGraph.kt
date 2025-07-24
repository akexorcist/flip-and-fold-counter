package dev.akexorcist.flipfoldcounter.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import dev.akexorcist.flipfoldcounter.R
import dev.akexorcist.flipfoldcounter.ui.instruction.InstructionRoute
import dev.akexorcist.flipfoldcounter.ui.main.MainRoute

sealed class Screen {
    data object Main : Screen()
    data object Instruction : Screen()
}

@Composable
fun NavGraph() {
    val context = LocalContext.current
    val backStack = remember { mutableStateListOf<Any>(Screen.Main) }
    NavDisplay(
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
