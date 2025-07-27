package dev.akexorcist.flipfoldcounter.ui.statistics

import android.text.Layout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.runtime.mutableIntStateOf
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
import dev.akexorcist.flipfoldcounter.ui.component.AppCard
import dev.akexorcist.flipfoldcounter.ui.theme.FlipFoldCounterTheme
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.text.NumberFormat

@Composable
fun StatisticsRoute(backStack: NavBackStack) {
    val viewModel: StatisticsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedTabIndex) {
        viewModel.onGraphTypeSelected(selectedTabIndex)
    }

    StatisticsScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        selectedTabIndex = selectedTabIndex,
        onTabSelected = { index -> selectedTabIndex = index },
        onNextDay = viewModel::onNextDay,
        onPreviousDay = viewModel::onPreviousDay,
        onNextMonth = viewModel::onNextMonth,
        onPreviousMonth = viewModel::onPreviousMonth,
        onBackClick = { backStack.removeLastOrNull() }
    )
}

@Composable
private fun StatisticsScreen(
    uiState: StatisticsUiState,
    snackbarHostState: SnackbarHostState,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onNextDay: () -> Unit,
    onPreviousDay: () -> Unit,
    onNextMonth: () -> Unit,
    onPreviousMonth: () -> Unit,
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
                        CircularProgressIndicator(
                            modifier = Modifier.padding(16.dp),
                            trackColor = MaterialTheme.colorScheme.primary,
                        )
                    }

                    is StatisticsUiState.Error -> {
                        Text(
                            text = "Error: ${uiState.error.localizedMessage ?: "Unknown error"}",
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    }

                    is StatisticsUiState.Success -> {
                        DateNavigator(
                            graphType = uiState.graphType,
                            isPreviousDayEnabled = uiState.isPreviousDayEnabled,
                            isPreviousMonthEnabled = uiState.isPreviousMonthEnabled,
                            isNextDayEnabled = uiState.isNextDayEnabled,
                            isNextMonthEnabled = uiState.isNextMonthEnabled,
                            onNextDay = onNextDay,
                            onPreviousDay = onPreviousDay,
                            onNextMonth = onNextMonth,
                            onPreviousMonth = onPreviousMonth
                        )
                        Spacer(Modifier.height(16.dp))
                        ChartContent(graphType = uiState.graphType)
                        Spacer(Modifier.height(8.dp))
                        SummaryContent(graphType = uiState.graphType)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryContent(
    graphType: GraphType,
) {
    val summary = when (graphType) {
        is GraphType.Hourly -> Pair(graphType.max, graphType.average)
        is GraphType.Daily -> Pair(graphType.max, graphType.average)
        is GraphType.Monthly -> Pair(graphType.max, graphType.average)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        AppCard(modifier = Modifier.width(140.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.statistics_summary_max),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = NumberFormat.getInstance().format(summary.first),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        AppCard(
            modifier = Modifier.width(140.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.statistics_summary_average),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = NumberFormat.getInstance().format(summary.second),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun DateNavigator(
    graphType: GraphType,
    isPreviousDayEnabled: Boolean,
    isPreviousMonthEnabled: Boolean,
    isNextDayEnabled: Boolean,
    isNextMonthEnabled: Boolean,
    onNextDay: () -> Unit,
    onPreviousDay: () -> Unit,
    onNextMonth: () -> Unit,
    onPreviousMonth: () -> Unit,
) {
    if (graphType is GraphType.Monthly) return
    val isPreviousEnabled = when (graphType) {
        is GraphType.Hourly -> isPreviousDayEnabled
        is GraphType.Daily -> isPreviousMonthEnabled
        else -> false
    }
    val isNextEnabled = when (graphType) {
        is GraphType.Hourly -> isNextDayEnabled
        is GraphType.Daily -> isNextMonthEnabled
        else -> false
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (isPreviousEnabled) {
            IconButton(
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    disabledContentColor = MaterialTheme.colorScheme.surface,
                ),
                onClick = { if (graphType is GraphType.Hourly) onPreviousDay() else onPreviousMonth() },
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.ic_previous),
                    contentDescription = stringResource(R.string.content_description_previous),
                )
            }
        } else {
            Spacer(Modifier.size(36.dp))
        }
        Text(
            modifier = Modifier.width(160.dp),
            text = when (val graph = graphType) {
                is GraphType.Hourly -> graph.date.toDisplayDate()
                is GraphType.Daily -> graph.yearMonth.toDisplayMonth()
                else -> ""
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        if (isNextEnabled) {
            IconButton(
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    disabledContentColor = MaterialTheme.colorScheme.surface,
                ),
                onClick = { if (graphType is GraphType.Hourly) onNextDay() else onNextMonth() },
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.ic_next),
                    contentDescription = stringResource(R.string.content_description_next),
                )
            }
        } else {
            Spacer(Modifier.size(36.dp))
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
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            )
        }
    }
}

@Composable
private fun ChartContent(graphType: GraphType) {
    val data = when (graphType) {
        is GraphType.Hourly -> graphType.toSeries()
        is GraphType.Daily -> graphType.toSeries()
        is GraphType.Monthly -> graphType.toSeries()
    }
    AppCard {
        if (data.isEmpty()) {
            Text(stringResource(R.string.statistics_empty_data))
        } else {
            BarChart(data)
        }
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
        modifier = Modifier
            .height(300.dp)
            .fillMaxWidth(),
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

private fun GraphType.Hourly.toSeries(): Map<Int, Int> = this.data.mapKeys { it.key.hour }

private fun GraphType.Daily.toSeries(): Map<Int, Int> = this.data.mapKeys { it.key.dayOfMonth }

private fun GraphType.Monthly.toSeries(): Map<Int, Int> = this.data.mapKeys { it.key.monthValue }

private fun LocalDate.toDisplayDate() = this.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))

private fun YearMonth.toDisplayMonth() = this.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

@Preview(showBackground = true)
@Composable
private fun StatisticsScreenLoadingPreview() {
    FlipFoldCounterTheme {
        StatisticsScreen(
            uiState = StatisticsUiState.Loading,
            snackbarHostState = remember { SnackbarHostState() },
            selectedTabIndex = 0,
            onTabSelected = {},
            onNextDay = {},
            onPreviousDay = {},
            onNextMonth = {},
            onPreviousMonth = {},
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
            onNextDay = {},
            onPreviousDay = {},
            onNextMonth = {},
            onPreviousMonth = {},
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
                ),
                selectedDate = LocalDate.now(),
                selectedMonth = YearMonth.now(),
                isNextDayEnabled = false,
                isPreviousDayEnabled = true,
                isNextMonthEnabled = false,
                isPreviousMonthEnabled = true,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            selectedTabIndex = 2,
            onTabSelected = {},
            onNextDay = {},
            onPreviousDay = {},
            onNextMonth = {},
            onPreviousMonth = {},
            onBackClick = {}
        )
    }
}
