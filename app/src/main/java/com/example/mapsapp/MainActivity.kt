package com.example.mapsapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity(), TopFragment.OnAddLocationListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onAddLocation(name: String, lat: Double, lon: Double) {
        val bottomFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView2) as? BottomFragment
        bottomFragment?.addPin(name, lat, lon)
    }

    fun focusMarkerByTitle(title: String) {
        val bottomFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView2) as? BottomFragment
        bottomFragment?.focusMarkerByTitle(title)
    }
}