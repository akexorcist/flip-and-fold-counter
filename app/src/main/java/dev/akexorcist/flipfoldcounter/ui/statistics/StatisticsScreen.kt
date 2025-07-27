package dev.akexorcist.flipfoldcounter.ui.statistics

import android.text.Layout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.fixed
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.insets
import com.patrykandpatrick.vico.compose.common.shape.markerCorneredShape
import com.patrykandpatrick.vico.compose.common.shape.rounded
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import dev.akexorcist.flipfoldcounter.R
import dev.akexorcist.flipfoldcounter.ui.theme.FlipFoldCounterTheme
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.max

@Composable
fun StatisticsRoute(backStack: NavBackStack) {
    val viewModel: StatisticsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTabIndex by remember { mutableStateOf(0) }

    LaunchedEffect(selectedTabIndex) {
        when (selectedTabIndex) {
            0 -> viewModel.loadHourlyStats(LocalDate.now())
            1 -> viewModel.loadDailyStats(YearMonth.now())
            2 -> viewModel.loadMonthlyStats()
        }
    }

    StatisticsScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        selectedTabIndex = selectedTabIndex,
        onTabSelected = { index -> selectedTabIndex = index },
        onBackClick = { backStack.removeLastOrNull() }
    )
}

@Composable
private fun StatisticsScreen(
    uiState: StatisticsUiState,
    snackbarHostState: SnackbarHostState,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            StatisticsTopBar(
                onBackClick = onBackClick,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            StatisticsTabs(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = onTabSelected
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (uiState) {
                    is StatisticsUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    }

                    is StatisticsUiState.Error -> {
                        Text(
                            text = "Error: ${uiState.error.localizedMessage ?: "Unknown error"}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }

                    is StatisticsUiState.Success -> {
                        val graph = uiState.graphType
                        val data = when (graph) {
                            is GraphType.Hourly -> graph.toSeries()
                            is GraphType.Daily -> graph.toSeries()
                            is GraphType.Monthly -> graph.toSeries()
                        }
                        ChartContent(data)
                        Text(
                            text = when (graph) {
                                is GraphType.Hourly -> "Hourly Stats for ${graph.date.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
                                is GraphType.Daily -> "Daily Stats for ${graph.yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))}"
                                is GraphType.Monthly -> "Monthly Overview"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatisticsTabs(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf(
        stringResource(R.string.statistics_tab_day),
        stringResource(R.string.statistics_tab_month),
        stringResource(R.string.statistics_tab_overall),
    )
    TabRow(selectedTabIndex = selectedTabIndex) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = { Text(text = title) }
            )
        }
    }
}


private fun GraphType.Hourly.toSeries(): Map<Int, Int> {
    return this.data.mapKeys { it.key.hour }
}

private fun GraphType.Daily.toSeries(): Map<Int, Int> {
    return this.data.mapKeys { it.key.dayOfMonth }
}

private fun GraphType.Monthly.toSeries(): Map<Int, Int> {
    return this.data.mapKeys { it.key.monthValue }
}

@Composable
private fun ChartContent(data: Map<Int, Int>) {
    if (data.isEmpty()) {
        Text(stringResource(R.string.statistics_empty_data))
    } else {
        BarChart(data)
    }
}

@Composable
private fun BarChart(data: Map<Int, Int>) {
    val modelProducer = remember { CartesianChartModelProducer() }
    LaunchedEffect(data) {
        modelProducer.runTransaction {
            columnSeries {
                series(
                    y = data.map { it.value },
                    x = data.map { it.key },
                )
            }
        }
    }
    val labelBackgroundShape = markerCorneredShape(CorneredShape.rounded(4.dp))
    val labelBackground =
        rememberShapeComponent(
            fill = fill(MaterialTheme.colorScheme.background),
            shape = labelBackgroundShape,
            strokeThickness = 1.dp,
            strokeFill = fill(MaterialTheme.colorScheme.outline),
        )
    val label = rememberTextComponent(
        color = MaterialTheme.colorScheme.onBackground,
        textAlignment = Layout.Alignment.ALIGN_CENTER,
        padding = insets(8.dp, 4.dp),
        background = labelBackground,
        minWidth = TextComponent.MinWidth.fixed(40.dp),
    )
    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(),
            startAxis = VerticalAxis.rememberStart(
                guideline = null,
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                guideline = null,
            ),
            marker = com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker(label),
        ),
        modelProducer = modelProducer,
    )
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun StatisticsTopBar(
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
                text = stringResource(R.string.statistics_topbar),
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

@Preview(showBackground = true)
@Composable
private fun StatisticsScreenLoadingPreview() {
    FlipFoldCounterTheme {
        StatisticsScreen(
            uiState = StatisticsUiState.Loading,
            snackbarHostState = remember { SnackbarHostState() },
            selectedTabIndex = 0,
            onTabSelected = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatisticsScreenErrorPreview() {
    FlipFoldCounterTheme {
        StatisticsScreen(
            uiState = StatisticsUiState.Error(Exception("Something went wrong")),
            snackbarHostState = remember { SnackbarHostState() },
            selectedTabIndex = 0,
            onTabSelected = {},
            onBackClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StatisticsScreenSuccessPreview() {
    FlipFoldCounterTheme {
        StatisticsScreen(
            uiState = StatisticsUiState.Success(
                graphType = GraphType.Monthly(
                    data = mapOf(
                        YearMonth.of(2023, 1) to 100,
                        YearMonth.of(2023, 2) to 120,
                        YearMonth.of(2023, 3) to 150,
                    ),
                    max = 150,
                    average = 120,
                )
            ),
            snackbarHostState = remember { SnackbarHostState() },
            selectedTabIndex = 2,
            onTabSelected = {},
            onBackClick = {}
        )
    }
}
