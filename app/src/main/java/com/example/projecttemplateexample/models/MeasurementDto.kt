package com.example.projecttemplateexample.models

import java.time.LocalDate

data class MeasurementState(
    val loading: Boolean = false,
    val showDatePicker: Boolean = false,
    val error: String? = null,
    val date: LocalDate = LocalDate.now()
)
