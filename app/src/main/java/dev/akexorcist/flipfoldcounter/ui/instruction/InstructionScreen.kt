package dev.akexorcist.flipfoldcounter.ui.instruction

import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import dev.akexorcist.flipfoldcounter.R
import dev.akexorcist.flipfoldcounter.ui.theme.FlipFoldCounterTheme
import kotlinx.coroutines.launch
import mx.platacard.pagerindicator.PagerIndicatorOrientation
import mx.platacard.pagerindicator.PagerWormIndicator

@Composable
fun InstructionRoute(backStack: NavBackStack) {
    val activity = LocalActivity.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    InstructionScreen(
        snackbarHostState = snackbarHostState,
        onBackClick = { backStack.removeLastOrNull() },
        onBottomAction = { action ->
            when (action) {
                Action.OpenRoutines -> {
                    activity?.let { context ->
                        val intent = Intent("com.samsung.android.app.routines.action.SETTINGS").apply {
                            setPackage("com.samsung.android.app.routines")
                            addCategory(Intent.CATEGORY_DEFAULT)
                            setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        } else {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.main_routines_app_not_found))
                            }
                        }
                    }
                }

                Action.Close -> {
                    backStack.removeLastOrNull()
                }
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun InstructionScreen(
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onBottomAction: (Action) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { instructionItems.size })
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            InstructionTopBar(
                onBackClick = onBackClick,
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(16.dp))
                PagerWormIndicator(
                    pagerState = pagerState,
                    activeDotColor = MaterialTheme.colorScheme.primary,
                    dotColor = MaterialTheme.colorScheme.primaryContainer,
                    dotCount = 6,
                    orientation = PagerIndicatorOrientation.Horizontal
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(32.dp))
            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerState,
                key = { it },
            ) { page ->
                InstructionPage(
                    item = instructionItems[page],
                    onBottomAction = onBottomAction,
                )
            }
        }
    }
}

@Composable
private fun InstructionPage(
    item: InstructionItem,
    onBottomAction: (Action) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .width(240.dp)
                .clip(RoundedCornerShape(16.dp)),
            painter = painterResource(id = item.image),
            contentDescription = stringResource(id = item.description)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(id = item.description),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        item.bottomAction?.let { bottomAction ->
            Spacer(modifier = Modifier.height(16.dp))
            bottomAction { action -> onBottomAction(action) }
        }
        Spacer(Modifier.height(16.dp))
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
                textAlign = TextAlign.Center,
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
            snackbarHostState = remember { SnackbarHostState() },
            onBackClick = {},
            onBottomAction = {},
        )
    }
}

private data class InstructionItem(
    @StringRes val description: Int,
    @DrawableRes val image: Int,
    val bottomAction: (@Composable (onActionClick: (Action) -> Unit) -> Unit)? = null,
)

private enum class Action {
    OpenRoutines, Close;
}

private val instructionItems = listOf(
    InstructionItem(
        description = R.string.instruction_step_1,
        image = R.drawable.instruction_01,
        bottomAction = { action ->
            Button(onClick = { action(Action.OpenRoutines) }) {
                Text(
                    text = stringResource(R.string.instruction_button_open_routines),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    ),
    InstructionItem(
        description = R.string.instruction_step_2,
        image = R.drawable.instruction_02,
    ),
    InstructionItem(
        description = R.string.instruction_step_3,
        image = R.drawable.instruction_03,
    ),
    InstructionItem(
        description = R.string.instruction_step_4,
        image = R.drawable.instruction_04,
    ),
    InstructionItem(
        description = R.string.instruction_step_5,
        image = R.drawable.instruction_05,
    ),
    InstructionItem(
        description = R.string.instruction_step_6,
        image = R.drawable.instruction_06,
    ),
    InstructionItem(
        description = R.string.instruction_step_7,
        image = R.drawable.instruction_07,
    ),
    InstructionItem(
        description = R.string.instruction_step_8,
        image = R.drawable.instruction_08,
    ),
    InstructionItem(
        description = R.string.instruction_step_9,
        image = R.drawable.instruction_09,
    ),
    InstructionItem(
        description = R.string.instruction_step_10,
        image = R.drawable.instruction_10,
    ),
    InstructionItem(
        description = R.string.instruction_step_11,
        image = R.drawable.instruction_11,
        bottomAction = { action ->
            Button(onClick = { action(Action.Close) }) {
                Text(
                    text = stringResource(R.string.instruction_button_close),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    ),
)
