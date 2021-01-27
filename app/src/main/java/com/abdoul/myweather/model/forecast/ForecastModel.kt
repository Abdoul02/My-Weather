package com.abdoul.myweather.model.forecast

import com.abdoul.myweather.model.forecast.ForecastDetail

data class ForecastModel(
    //val city: City,  //No use at the moment
    val cnt: Int,
    val cod: String,
    val list: List<ForecastDetail>,
    val message: Int
)