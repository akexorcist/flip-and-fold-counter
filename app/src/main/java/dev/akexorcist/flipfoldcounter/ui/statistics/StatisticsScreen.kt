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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

@Composable
fun StatisticsRoute(backStack: NavBackStack) {
    val viewModel: StatisticsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
//        viewModel.loadHourlyStats(LocalDate.now())
//        viewModel.loadDailyStats(YearMonth.now())
        viewModel.loadMonthlyStats()
    }

    StatisticsScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = { backStack.removeLastOrNull() }
        // TODO: Add event handlers to call viewModel.loadHourlyStats(date) or viewModel.loadDailyStats(yearMonth)
    )
}

@Composable
private fun StatisticsScreen(
    uiState: StatisticsUiState,
    snackbarHostState: SnackbarHostState,
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
                .padding(16.dp), // Added some general padding
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
                    Text(
                        text = when (graph) {
                            is GraphType.Hourly -> "Hourly Stats for ${graph.date.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
                            is GraphType.Daily -> "Daily Stats for ${graph.yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))}"
                            is GraphType.Monthly -> "Monthly Overview"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        when (graph) {
                            is GraphType.Hourly -> {
                                items(graph.data.entries.toList().sortedBy { it.key }) { (time, count) ->
                                    Text("${time.format(DateTimeFormatter.ofPattern("HH:mm"))}: $count counts")
                                }
                            }

                            is GraphType.Daily -> {
                                items(graph.data.entries.toList().sortedBy { it.key }) { (date, count) ->
                                    Text("${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}: $count counts")
                                }
                            }

                            is GraphType.Monthly -> {
                                items(graph.data.entries.toList().sortedBy { it.key }) { (month, count) ->
                                    Text("${month.format(DateTimeFormatter.ofPattern("MMMM yyyy"))}: $count counts")
                                }
                            }
                        }
                    }
                    // TODO: Here you could integrate a chart component like BarChart()
                    // You would need to adapt BarChart to accept the data from uiState.graphType
                }
            }
        }
    }
}

// BarChart composable remains here, but is not actively used in the main flow yet.
// It would need to be adapted to consume data from StatisticsUiState.Success.graphType
@Composable
private fun BarChart() {
    val modelProducer = remember { com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer() }
    LaunchedEffect(Unit) {
        modelProducer.runTransaction {
            columnSeries {
                series(
                    y = listOf(5, 6, 5, 2, 11, 8, 5, 2, 15, 11, 8, 13, 12, 10, 2, 7),
                    x = listOf(11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26),
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
        color = MaterialTheme.colorScheme.background,
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

@Preview
@Composable
private fun StatisticsScreenPreview_Loading() {
    FlipFoldCounterTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            StatisticsScreen(
                uiState = StatisticsUiState.Loading,
                snackbarHostState = remember { SnackbarHostState() },
                onBackClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun StatisticsScreenPreview_Error() {
    FlipFoldCounterTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            StatisticsScreen(
                uiState = StatisticsUiState.Error(IllegalStateException("Preview Error")),
                snackbarHostState = remember { SnackbarHostState() },
                onBackClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun StatisticsScreenPreview_Success_Monthly() {
    FlipFoldCounterTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            StatisticsScreen(
                uiState = StatisticsUiState.Success(
                    GraphType.Monthly(
                        data = mapOf(
                            YearMonth.of(2023, 1) to 100,
                            YearMonth.of(2023, 2) to 150
                        ),
                        max = 150,
                        average = 125,
                    )
                ),
                snackbarHostState = remember { SnackbarHostState() },
                onBackClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun StatisticsScreenPreview_Success_Daily() {
    FlipFoldCounterTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            StatisticsScreen(
                uiState = StatisticsUiState.Success(
                    GraphType.Daily(
                        yearMonth = YearMonth.of(2023, 1),
                        data = mapOf(
                            LocalDate.of(2023, 1, 1) to 10,
                            LocalDate.of(2023, 1, 2) to 15
                        ),
                        max = 15,
                        average = 12,
                    )
                ),
                snackbarHostState = remember { SnackbarHostState() },
                onBackClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun StatisticsScreenPreview_Success_Hourly() {
    FlipFoldCounterTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            StatisticsScreen(
                uiState = StatisticsUiState.Success(
                    GraphType.Hourly(
                        date = LocalDate.of(2023, 1, 1),
                        data = mapOf(
                            LocalTime.of(10, 0) to 5,
                            LocalTime.of(11, 0) to 8
                        ),
                        max = 8,
                        average = 6,
                    )
                ),
                snackbarHostState = remember { SnackbarHostState() },
                onBackClick = {},
            )
        }
    }
}
