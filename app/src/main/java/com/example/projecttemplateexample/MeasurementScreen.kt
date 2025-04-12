package com.example.projecttemplateexample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.projecttemplateexample.models.MeasurementState
import com.example.projecttemplateexample.vm.MeasurementViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MeasurementScreenRoot(modifier: Modifier = Modifier, vm: MeasurementViewModel) {

    val measurementState by vm.measurementState.collectAsStateWithLifecycle()
    val labelState by vm.labelState.collectAsStateWithLifecycle()
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(measurementState.data) {
        modelProducer.runTransaction {
            // Learn more: https://patrykandpatrick.com/vmml6t.
            lineSeries { series(measurementState.data.values) }
            extras { it[labelState] = measurementState.data.keys.toList() }


        }
    }


    MeasurementScreen(
        measurementState = measurementState,
        dtFormatter = dateFormatter,
        onDateBtnClick = {
            vm.setDatePickerVisibility()
        }, onConfirm = { newDate ->
            vm.setDate(newDate)
        }, modelProducer = modelProducer,

        labelState = labelState,
        onRetry = {
            vm.getMeasurements()
        }
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementScreen(
    modifier: Modifier = Modifier,
    measurementState: MeasurementState,
    labelState: ExtraStore.Key<List<String>>,
    dtFormatter: DateTimeFormatter,
    onDateBtnClick: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
    modelProducer: CartesianChartModelProducer,
    onRetry: () -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(title = {
            Text("Measurements")
        })
    }, bottomBar = {
        BottomAppBar {
            TextButton(onClick = {
                onDateBtnClick()
            }, modifier = Modifier.weight(1f)) {
                Text(
                    text = measurementState.date.format(dtFormatter),
                    textAlign = TextAlign.Center

                )
            }
        }
    }) { paddingValues ->
        when {
            measurementState.loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {
                measurementState.error?.let { err ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(err)
                            Button(onClick = {
                                onRetry()
                            }) {
                                Text("Retry")
                            }
                        }

                    }
                } ?: LineChart(
                    measurementState = measurementState,
                    labelState = labelState,
                    modelProducer = modelProducer
                )

                if (measurementState.showDatePicker) {

                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = null,

                        )
                    DatePickerDialog(
                        onDismissRequest = {
                            onDateBtnClick()
                        },
                        confirmButton = {
                            TextButton(
                                enabled = datePickerState.selectedDateMillis != null,
                                onClick = {

                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val newDate =
                                            Instant.ofEpochMilli(millis)
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDate()
                                        onConfirm(newDate)

                                    }


                                }) {
                                Text("Confirm")
                            }

                        }, dismissButton = {
                            TextButton(onClick = {
                                onDateBtnClick()
                            }) {
                                Text("Cancel")
                            }
                        }

                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

            }


        }
    }
}


@Composable
fun LineChart(
    modifier: Modifier = Modifier,
    measurementState: MeasurementState,
    labelState: ExtraStore.Key<List<String>>,

    modelProducer: CartesianChartModelProducer
) {



    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if(measurementState.data.isEmpty()) {
            Text("No data available")
        } else {
            JetpackComposeBasicLineChart(modelProducer, modifier, labelState)
        }

    }

}

@Composable
private fun JetpackComposeBasicLineChart(
    modelProducer: CartesianChartModelProducer,
    modifier: Modifier = Modifier,
    labelKeys: ExtraStore.Key<List<String>>
) {

    CartesianChartHost(
        chart =
            rememberCartesianChart(
                rememberLineCartesianLayer(),
                startAxis = VerticalAxis.rememberStart(),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = { context, x, _ ->
                        context.model.extraStore[labelKeys][x.toInt()]
                    }
                ),

                ),
        modelProducer = modelProducer,
        modifier = modifier,
    )
}

