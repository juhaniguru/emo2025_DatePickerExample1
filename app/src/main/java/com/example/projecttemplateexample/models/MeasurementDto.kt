package com.example.projecttemplateexample.models

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

data class MeasurementState(
    // jos tämä on true, näytetään latausikoni
    val loading: Boolean = false,
    // kun tämä on true, näytetään datepicker
    val showDatePicker: Boolean = false,
    // kun tämä != null, näytetään virhe käyttäjälle
    // sekä Retry-nappi
    val error: String? = null,
    // bottombarissa näkyvä päivä, joka
    // valitaan datepickerilla
    val date: LocalDate = LocalDate.now(),
    // backendista tuleva mittausdata
    val data: Map<String, Float> = emptyMap()

)


data class MeasurementDto(
    // @SerializedNamea voidaan käyttää muuttamaan backendista tulevien
    // avainten nimiä
    // Pythonissa käytetään nimeämisessä alaviivaa
    // Kotlin ei tykkää tästä käytännöstä
    // date_str -> dateStr
    @SerializedName("date_str")
    val dateStr: String,
    val value: Float)

