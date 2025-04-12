package com.example.projecttemplateexample.vm

import androidx.lifecycle.ViewModel

import com.example.projecttemplateexample.models.MeasurementState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MeasurementViewModel @Inject constructor() : ViewModel() {
    private val _measurementState = MutableStateFlow(MeasurementState())
    val measurementState = _measurementState.asStateFlow()

    fun setDate(newDate: LocalDate) {
        _measurementState.update { currentState ->
            currentState.copy(date = newDate, showDatePicker = false)
        }
    }

    fun setDatePickerVisibility() {
        _measurementState.update { currentState ->
            currentState.copy(showDatePicker = !currentState.showDatePicker)
        }
    }
}