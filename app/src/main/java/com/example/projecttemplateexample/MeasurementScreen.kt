package com.example.projecttemplateexample

import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.projecttemplateexample.models.MeasurementState
import com.example.projecttemplateexample.vm.MeasurementViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MeasurementScreenRoot(modifier: Modifier = Modifier, vm: MeasurementViewModel) {
    val measurementState by vm.measurementState.collectAsStateWithLifecycle()
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")


    MeasurementScreen(
        measurementState = measurementState,
        dtFormatter = dateFormatter,
        onDateBtnClick = {
            vm.setDatePickerVisibility()
        }, onConfirm = { newDate ->
            vm.setDate(newDate)
        })

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementScreen(
    modifier: Modifier = Modifier,
    measurementState: MeasurementState,
    dtFormatter: DateTimeFormatter,
    onDateBtnClick: () -> Unit,
    onConfirm: (LocalDate) -> Unit
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
                        Text(err)
                    }
                } ?: MeasurementChart(measurementState = measurementState)

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
                                        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementChart(modifier: Modifier = Modifier, measurementState: MeasurementState) {
    /*
    * This is the composable that should contain the datepicker
    * */
}

