package dev.akexorcist.flipfoldcounter.ui.main

import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dev.akexorcist.flipfoldcounter.CounterActivity
import dev.akexorcist.flipfoldcounter.R
import dev.akexorcist.flipfoldcounter.ui.component.AppCard
import dev.akexorcist.flipfoldcounter.ui.navigation.Screen
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat

@Composable
fun MainRoute(backStack: SnapshotStateList<Any>) {
    val activity = LocalActivity.current
    val context = LocalContext.current
    val viewModel: MainViewModel = koinViewModel()
    val totalCount by viewModel.totalCount.collectAsState()
    val todayCount by viewModel.todayCount.collectAsState()
    val thisMonthCount by viewModel.thisMonthCount.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


    LaunchedEffect(Unit) {
        if (!context.packageManager.isRoutinesAppAvailable()) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.main_routines_app_not_found),
                    duration = SnackbarDuration.Indefinite,
                )
            }
        }
    }

    MainScreen(
        snackbarHostState = snackbarHostState,
        totalCount = totalCount,
        todayCount = todayCount,
        thisMonthCount = thisMonthCount,
        onInstructionClick = {
            backStack.add(Screen.Instruction)
        },
        onGitHubClick = {
            activity?.startActivity(Intent(Intent.ACTION_VIEW, "https://akexorcist.dev".toUri()))
        },
        onAddCountClick = {
            activity?.startActivity(Intent(activity, CounterActivity::class.java))
//            viewModel.addCount()
        },
        onOpenRoutinesClick = {
            activity?.let { context ->
                val intent = Intent("com.samsung.android.app.routines.action.SETTINGS").apply {
                    setPackage("com.samsung.android.app.routines")
                    addCategory(Intent.CATEGORY_DEFAULT)
                }
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar(context.getString(R.string.main_routines_app_not_found))
                    }
                }
            }
        }
    )
}

private fun PackageManager.isRoutinesAppAvailable(): Boolean {
    val intent = Intent("com.samsung.android.app.routines.action.SETTINGS").apply {
        setPackage("com.samsung.android.app.routines")
        addCategory(Intent.CATEGORY_DEFAULT)
    }
    return intent.resolveActivity(this) != null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    snackbarHostState: SnackbarHostState,
    totalCount: Int,
    todayCount: Int,
    thisMonthCount: Int,
    onInstructionClick: () -> Unit,
    onGitHubClick: () -> Unit,
    onAddCountClick: () -> Unit,
    onOpenRoutinesClick: () -> Unit,
) {
    val numberFormat = remember { NumberFormat.getInstance() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            MainTopBar(
                onInstructionClick = onInstructionClick,
                onGitHubClick = onGitHubClick,
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(32.dp))
            Header()
            Spacer(Modifier.height(64.dp))
            AppCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 320.dp)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.main_label_all),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = numberFormat.format(totalCount),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                AppCard(
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = 320.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.main_label_day),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = numberFormat.format(todayCount),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(Modifier.width(16.dp))
                AppCard(
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = 320.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.main_label_month),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = numberFormat.format(thisMonthCount),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
            Button(onClick = onAddCountClick) {
                Text(text = stringResource(R.string.button_count_me_in))
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = onOpenRoutinesClick) {
                Text(text = stringResource(R.string.button_open_routines))
            }
        }
    }
}

@Composable
private fun Header() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            modifier = Modifier
                .width(200.dp)
                .wrapContentHeight(),
            painter = painterResource(R.drawable.ic_flip_fold_devices),
            contentDescription = stringResource(R.string.content_description_flip_fold_devices),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.main_title),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MainTopBar(
    onInstructionClick: () -> Unit,
    onGitHubClick: () -> Unit,
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
            Column {
                Text(
                    modifier = Modifier.offset(y = 1.dp),
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    modifier = Modifier.offset(y = (-1).dp),
                    text = stringResource(R.string.app_name_suffix),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        },
        actions = {
            IconButton(onClick = onGitHubClick) {
                Icon(
                    painterResource(R.drawable.ic_graph),
                    contentDescription = stringResource(R.string.content_description_summary_graph),
                )
            }
            IconButton(onClick = onInstructionClick) {
                Icon(
                    painterResource(R.drawable.ic_instruction),
                    contentDescription = stringResource(R.string.content_description_instruction),
                )
            }
            IconButton(onClick = onGitHubClick) {
                Icon(
                    painterResource(R.drawable.ic_github),
                    contentDescription = stringResource(R.string.content_description_source_code),
                )
            }
        }
    )
}

@Preview
@Composable
private fun MainScreenPreview() {
    MaterialTheme {
        MainScreen(
            snackbarHostState = remember { SnackbarHostState() },
            totalCount = 38271,
            todayCount = 231,
            thisMonthCount = 6572,
            onInstructionClick = {},
            onGitHubClick = {},
            onAddCountClick = {},
            onOpenRoutinesClick = {},
        )
    }
}
