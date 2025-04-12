package com.example.projecttemplateexample

import com.example.projecttemplateexample.models.MeasurementDto
import com.example.projecttemplateexample.models.UserDto
import retrofit2.http.GET
import retrofit2.http.Path

interface DataApi {
    @GET("measurements/{dateStr}")
    suspend fun getMeasurementsByDate(@Path("dateStr") dateStr: String) : List<MeasurementDto>
}