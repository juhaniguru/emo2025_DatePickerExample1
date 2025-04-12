package com.example.projecttemplateexample


// MeasurementDataInterfacea käytetään AppModulessa
interface MeasurementDataService {
    suspend fun getDataByDate(dateStr: String) : Map<String, Float>
}

// konkreettinen implementaatio yo. interfacen metodista
// käyttää dependencyna DataApi interfacea (retrofit)
class MeasurementDataServiceImpl(private val api: DataApi) : MeasurementDataService {
    override suspend fun getDataByDate(dateStr: String) : Map<String, Float> {
        // restapi palauttaa listan json-objekteja
        /*
        * {"date_str": "00:00", "value": 1}
        * */
        val data =  api.getMeasurementsByDate(dateStr)
        // koska vico chart tarvii datan Map<String, Float>-muodossa
        // associate muuttaa retofitiltä saadun listan sopivaan muotoon
        return data.associate { point ->
            point.dateStr to point.value
        }
    }

}