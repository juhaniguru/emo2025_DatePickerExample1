package com.example.projecttemplateexample.models

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

data class MeasurementState(
    val loading: Boolean = false,
    val showDatePicker: Boolean = false,
    val error: String? = null,
    val date: LocalDate = LocalDate.now(),
    val data: Map<String, Float> = emptyMap()

)


data class MeasurementDto(
    @SerializedName("date_str")
    val dateStr: String,
    val value: Float)

