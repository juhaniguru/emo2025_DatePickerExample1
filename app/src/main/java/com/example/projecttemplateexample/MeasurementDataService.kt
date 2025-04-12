package com.example.projecttemplateexample

import com.example.projecttemplateexample.models.MeasurementDto

interface MeasurementDataService {
    suspend fun getDataByDate(dateStr: String) : Map<String, Float>
}

class MeasurementDataServiceImpl(private val api: DataApi) : MeasurementDataService {
    override suspend fun getDataByDate(dateStr: String) : Map<String, Float> {
        val data =  api.getMeasurementsByDate(dateStr)
        return data.associate { point ->
            point.dateStr to point.value
        }
    }

}