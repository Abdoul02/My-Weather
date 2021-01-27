package com.abdoul.myweather.other

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.abdoul.myweather.R
import com.abdoul.myweather.model.enums.WeatherTypes
import com.abdoul.myweather.model.forecast.ForecastDetail
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ForecastAdapter (private val context: Context) :
    RecyclerView.Adapter<ForecastAdapter.ViewHolder>() {
    private var data: ArrayList<ForecastDetail> = ArrayList()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvDay: TextView = itemView.findViewById(R.id.tvDay)
        var imvWeatherIcon: ImageView = itemView.findViewById(R.id.imvWeatherIcon)
        var tvTemperature: TextView = itemView.findViewById(R.id.tvTemperature)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.weather_entry,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return if (!data.isNullOrEmpty()) data.size else 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val forecastModel = data[position]
        holder.tvDay.text = forecastModel.dt_txt
        holder.imvWeatherIcon.setImageResource(getDrawable(forecastModel.weather[0].main))
        holder.tvTemperature.text = context.getString(
            R.string.current_temp,
            forecastModel.main.temp.toInt().toString(),
            "\u2103"
        )
    }

    fun setData(forecastData: List<ForecastDetail>) {
        if(data.isNotEmpty()) data.clear()
        this.data.addAll(filteredForecastDetail(forecastData))
        notifyDataSetChanged()
    }

    private fun getDrawable(tempDetail: String): Int {
        return when (tempDetail) {
            WeatherTypes.CLEAR.types -> {
                context.resources.getIdentifier("clear", "drawable", context.packageName)
            }
            WeatherTypes.CLOUD.types -> {
                context.resources.getIdentifier("ic_cloudy", "drawable", context.packageName)
            }
            WeatherTypes.RAIN.types -> {
                context.resources.getIdentifier("rain", "drawable", context.packageName)
            }
            else -> context.resources.getIdentifier("partlysunny", "drawable", context.packageName)
        }
    }

    private fun getDayOfWeekFromDate(dateTime: String): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date: Date = dateFormat.parse(dateTime)
        val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale.getDefault())

        return dayOfWeekFormat.format(date)
    }

    private fun filteredForecastDetail(forecastData: List<ForecastDetail>): List<ForecastDetail> {
        val modifiedList = ArrayList<ForecastDetail>()
        for (data in forecastData) {
            val newForecastDetail = ForecastDetail(
                clouds = data.clouds,
                dt = data.dt,
                dt_txt = getDayOfWeekFromDate(data.dt_txt),
                main = data.main,
                rain = data.rain,
                sys = data.sys,
                weather = data.weather,
                wind = data.wind
            )
            modifiedList.add(newForecastDetail)
        }
        return modifiedList.distinctBy { it.dt_txt }
            .filter { it.dt_txt != getDayOfWeekFromDate(getCurrentDateTime()) }
    }

    private fun getCurrentDateTime(): String {
        val date = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK)
        return dateFormat.format(date)
    }
}