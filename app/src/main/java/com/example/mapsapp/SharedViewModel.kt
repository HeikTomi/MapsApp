package com.example.mapsapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _location = MutableLiveData<Pair<Double, Double>>()
    private val _title = MutableLiveData<String>()

    val location: LiveData<Pair<Double, Double>> = _location
    val title: LiveData<String> = _title

    fun setLocation(lat: Double?, lon: Double?) {
        _location.value = Pair(lat, lon) as Pair<Double, Double>?
    }

    fun setTitle(title: String) {
        _title.value = title
    }
}