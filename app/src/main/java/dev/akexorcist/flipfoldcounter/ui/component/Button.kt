package dev.akexorcist.flipfoldcounter.ui.component

import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

object Buttons {
    @Composable
    fun filledIconButton() = IconButtonDefaults.filledIconButtonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        disabledContentColor = MaterialTheme.colorScheme.surface,
    )
}
