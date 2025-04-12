package com.example.projecttemplateexample.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projecttemplateexample.MeasurementDataService
import com.example.projecttemplateexample.NetworkChecker

import com.example.projecttemplateexample.models.MeasurementState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import kotlinx.coroutines.launch
import android.util.Log

@HiltViewModel
class MeasurementViewModel @Inject constructor(
    private val _dataService: MeasurementDataService,
    private val networkChecker: NetworkChecker
) : ViewModel() {
    private val _measurementState = MutableStateFlow(MeasurementState())
    val measurementState = _measurementState.asStateFlow()

    private val _labelState = MutableStateFlow(ExtraStore.Key<List<String>>())
    val labelState = _labelState.asStateFlow()

    // kun viewmodelista tehdään instanssi
    // haetaan valitun päivän datat
    // oletuksena kuluva päivä
    init {
        getMeasurements()
    }

    fun getMeasurements() {
        viewModelScope.launch {
            // jos laittessa on internetyhteys päällä
            if (networkChecker.isNetworkAvailable()) {
                try {
                    // näytetään latausikoni
                    _measurementState.update { currentState ->
                        currentState.copy(loading = true, error = null)
                    }
                    // haetaan datepickerilla valitun päivän datat
                    val measurements =
                        _dataService.getDataByDate(_measurementState.value.date.toString())
                    _measurementState.update { currentState ->
                        currentState.copy(data = measurements)
                    }
                    // jos tulee virhe, näytetään se käyttöliittymässä
                } catch (e: Exception) {
                    _measurementState.update { currentState ->
                        currentState.copy(error = e.toString())
                    }
                    // ei väliä onnistutaanko / epäonnistutaanko, laitetaan lopuksi loading falseksi
                } finally {
                    _measurementState.update { currentState ->
                        currentState.copy(loading = false)
                    }
                }
                // jos internet-yhteyttä ei ole, näytetään virhe käyttäjälle
            } else {
                _measurementState.update { currentState ->
                    currentState.copy(error = "Network unavailable")
                }
            }
        }
    }

    // tätä kutsutaan, kun painetaan confirm-nappia
    // datepickerissa
    fun setDate(newDate: LocalDate) {
        _measurementState.update { currentState ->
            currentState.copy(date = newDate, showDatePicker = false)
        }
        // kun uusi päivä on asetettu ja datepicker piilotettu, haetaan uudet datat
        getMeasurements()
    }

    // tällä näytetään / piilotetaan datepicker
    fun setDatePickerVisibility() {
        _measurementState.update { currentState ->
            currentState.copy(showDatePicker = !currentState.showDatePicker)
        }
    }
}