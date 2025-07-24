package dev.akexorcist.flipfoldcounter.ui.instruction

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.akexorcist.flipfoldcounter.R
import dev.akexorcist.flipfoldcounter.ui.theme.FlipFoldCounterTheme

@Composable
fun InstructionRoute(backStack: SnapshotStateList<Any>) {
    InstructionScreen(
        onBackClick = { backStack.removeLastOrNull() },
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun InstructionScreen(
    onBackClick: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = {
            InstructionTopBar(
                onBackClick = onBackClick,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalPager(state = pagerState) {
                Text(text = stringResource(R.string.instruction_step, it))
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun InstructionTopBar(
    onBackClick: () -> Unit,
) {
    TopAppBar(
        expandedHeight = 72.dp,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        title = {
            Text(
                modifier = Modifier.offset(y = 1.dp),
                text = stringResource(R.string.instruction_topbar),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = stringResource(R.string.content_description_back)
                )
            }
        }
    )
}

@Preview
@Composable
private fun InstructionScreenPreview() {
    FlipFoldCounterTheme {
        InstructionScreen(
            onBackClick = {},
        )
    }
}
