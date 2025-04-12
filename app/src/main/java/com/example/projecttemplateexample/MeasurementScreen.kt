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
    // measurementState löytyy MeasurementDto-tiedostosta

    val measurementState by vm.measurementState.collectAsStateWithLifecycle()
    // labelState on viewmodelissa tyhjä aluksi, mutta kannattaa pitää viewmodelissa
    // jos datapisteiden määrä vaihtuu, eikä labelstate päivity datan mukana, ohjelma kaatuu
    // ongelmaa ei ole, kun labelState on viewmodelissa
    val labelState by vm.labelState.collectAsStateWithLifecycle()
    // dataformatteria käytetään bottombarissa olevan  napin päivämäärän muotoiluun
    // se on mallia 12 Apr 2025
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    // chartin state. Ei kannata laittaa viewmodeliin
    // kannattaa pitää composablessa, jotta modelProducer kuolee composablen mukana
    // eikä jää vuotamaan muistia
    val modelProducer = remember { CartesianChartModelProducer() }

    // kun measurementStaten data-attribuutti muuttuu (eli mittausdata haetaan backendista
    // piirretään chart uudestaan)
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
        // onDateBtnClick-callback avaa / sulkee DatePickerin
        onDateBtnClick = {
            vm.setDatePickerVisibility()
            // onConfirm asettaa valitun päivän ja hakee sille datan
        }, onConfirm = { newDate ->
            vm.setDate(newDate)
        }, modelProducer = modelProducer,

        labelState = labelState,
        // jos tulee virhe datan haussa,
        // näytetään käyttöliittymäsä virheviesti ja Retry-nappi
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
        // bottomappbarissa on nappi, jolla saa datepickerin auki

        BottomAppBar {
            TextButton(onClick = {
                onDateBtnClick()
                // kun weight(1f) on ainoalla elementillä
                // bottombarissa, se vie koko leveyden
            }, modifier = Modifier.weight(1f)) {
                Text(
                    // näytetään valittu päivämäärä napissa ja keskitetään teksti
                    text = measurementState.date.format(dtFormatter),
                    textAlign = TextAlign.Center

                )
            }
        }
    }) { paddingValues ->
        when {
            measurementState.loading -> {
                // kun loading on true, näytetään latausikoni
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
                        // jos tapahtuu virhe, näytetään virhe ja nappi,
                        // jolla voi hakea datat uudelleen
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(err)
                            Button(onClick = {
                                onRetry()
                            }) {
                                Text("Retry")
                            }
                        }

                    }
                    // jos error on null (eli ei virhettä), piirretään chart
                } ?: LineChart(
                    measurementState = measurementState,
                    labelState = labelState,
                    modelProducer = modelProducer
                )
                // kun showDatePicker on true, näytetään DatePicker modaali-ikkunassa
                if (measurementState.showDatePicker) {

                    // datepickeria käytetään vain täällä, kannatta pitää poissa viewmodelista
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = null,

                        )
                    DatePickerDialog(
                        // koska datepicker on modaali-ikkunassa
                        // kun klikkaat ikkunan ulkopuolelle, se sulkeutuu
                        onDismissRequest = {
                            onDateBtnClick()
                        },
                        // tämä on Confirm-nappi modaali-ikkunassa
                        confirmButton = {
                            TextButton(
                                // nappi on aktiivinen vain, jos uusi päivä on valittu
                                enabled = datePickerState.selectedDateMillis != null,
                                onClick = {
                                    // valittu päivä on millisekunneissa
                                    // muutetaan se päivämääräksi
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val newDate =
                                            Instant.ofEpochMilli(millis)
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDate()
                                        // kun päivämäärä on valittu
                                        // ja muutettu oikeaan muotoon
                                        // kutsutaan funktiota, joka sulkee datepickerin
                                        // ja hakee datat valitulta päivältä
                                        onConfirm(newDate)

                                    }


                                }) {
                                Text("Confirm")
                            }
                        // jos painetaan cance-nappia, suljetaan modaali
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
        // voi olla, että päivältä ei ole dataa saatavilla
        // näytetään käyttäjälle ilmoitus
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

