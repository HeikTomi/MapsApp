package com.example.mapsapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.widget.Toast
import androidx.fragment.app.activityViewModels

private const val ARG_PARAM_LAT = "paramLat"
private const val ARG_PARAM_LONG = "paramLong"
private const val ARG_PARAM_TITLE = "paramTitle"

/**
 * A simple [Fragment] subclass.
 * Use the [BottomFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BottomFragment : Fragment() {

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val markerList = mutableListOf<com.google.android.gms.maps.model.Marker>()
    private var lastClickedMarker: com.google.android.gms.maps.model.Marker? = null
    private var lastClickTime: Long = 0

    private var paramLat: Double? = null
    private var paramLong: Double? = null
    private var paramTitle: String? = null
    private var defaultLatLng: LatLng = LatLng(60.9827, 25.6615) // fallback default
    private var defaultMarkerTitle: String = "Lahti Keskusta"

    private val callback = OnMapReadyCallback { googleMap ->
        val lat = arguments?.getDouble(ARG_PARAM_LAT)
        val lon = arguments?.getDouble(ARG_PARAM_LONG)

        googleMap.uiSettings.isMapToolbarEnabled = true
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true

        // Load markers from DB after map is ready
        val dbHelper = DatabaseHelper(requireContext())
        val dbManager = DatabaseManager(dbHelper)
        val places = dbManager.getData()
        places.forEach { place ->
            val location = LatLng(place.lat, place.lng)
            val marker = googleMap.addMarker(MarkerOptions().position(location).title(place.title))
            if (marker != null) {
                markerList.add(marker)
            }
        }
        if (places.isNotEmpty()) {
            val firstPlace = places.first()
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(firstPlace.lat, firstPlace.lng), 13f))
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 13f))
        }

        //googleMap.addMarker(MarkerOptions().position(location).title(defaultMarkerTitle))
        //googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13f))

        googleMap.setOnMapClickListener {
            /** Lisää uusi merkki kartalle karttaa klikkaamalla jos title ei ole tyhjä */
            val title = sharedViewModel.title.value

            if (markerList.any { it.title == title }) {
                Toast.makeText(requireContext(), "Samanniminen paikka on jo olemassa!", Toast.LENGTH_SHORT).show()
                return@setOnMapClickListener
            }
            val dbHelper = DatabaseHelper(requireContext())
            val dbManager = DatabaseManager(dbHelper)
            val places = dbManager.getData()
            if (places.any { it.title == title }) {
                Toast.makeText(requireContext(), "Samanniminen paikka on jo tietokannassa!", Toast.LENGTH_SHORT).show()
                return@setOnMapClickListener
            }

            if (!title.isNullOrEmpty()) {
                val marker = googleMap.addMarker(MarkerOptions().position(it).title(title))
                if (marker != null) {
                    markerList.add(marker)
                }
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(it))
                paramLat = it.latitude
                paramLong = it.longitude
                sharedViewModel.setLocation(paramLat, paramLong)

                // Insert the marker into the database
                dbManager.insertData(title, paramLat ?: 0.0, paramLong ?: 0.0)
            } else {
                Toast.makeText(requireContext(), "Anna paikka ensin!", Toast.LENGTH_SHORT).show()
            }
        }

        googleMap.setOnMarkerClickListener { marker ->
            val currentTime = System.currentTimeMillis()
            if (lastClickedMarker == marker && currentTime - lastClickTime < 1000) {
                // Poista myös tietokannasta
                val dbHelper = DatabaseHelper(requireContext())
                val dbManager = DatabaseManager(dbHelper)
                // Delete the marker from the database
                dbManager.deleteData(marker.title ?: "")
                // Remove the marker from the map
                marker.remove()
                // Remove the marker from the list
                markerList.remove(marker)
                lastClickedMarker = null
                lastClickTime = 0L

                Toast.makeText(requireContext(), "Kohde poistettu!", Toast.LENGTH_SHORT).show()
                true // consume event, do not show info window
            } else {
                lastClickedMarker = marker
                lastClickTime = currentTime
                val lat = marker.position.latitude
                val lon = marker.position.longitude
                sharedViewModel.setLocation(lat, lon)
                sharedViewModel.setTitle(marker.title ?: "")
                false // allow default behavior (show info window)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            paramTitle = it.getString(ARG_PARAM_TITLE)
            paramLat = it.getDouble(ARG_PARAM_LAT, 0.0)
            paramLong = it.getDouble(ARG_PARAM_LONG, 0.0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bottom, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_container) as? SupportMapFragment
        mapFragment?.getMapAsync(callback)
    }

    /** PINNIN LISÄYS ja KAMERAN SIIRTO */
    fun addPin(name: String, lat: Double, lon: Double) {
        if (name.isBlank()) {
            Toast.makeText(requireContext(), "Nimi ei voi olla tyhjä!", Toast.LENGTH_SHORT).show()
            return
        }
        if (lat == 0.0 || lon == 0.0) {
            Toast.makeText(requireContext(), "Anna sekä leveys- että pituusaste!", Toast.LENGTH_SHORT).show()
            return
        }
        // Check for duplicate name in markerList
        if (markerList.any { it.title == name }) {
            Toast.makeText(requireContext(), "Samanniminen paikka on jo olemassa!", Toast.LENGTH_SHORT).show()
            return
        }
        // Optionally, check in the database as well
        val dbHelper = DatabaseHelper(requireContext())
        val dbManager = DatabaseManager(dbHelper)
        val places = dbManager.getData()
        if (places.any { it.title == name }) {
            Toast.makeText(requireContext(), "Samanniminen paikka on jo tietokannassa!", Toast.LENGTH_SHORT).show()
            return
        }
        if(dbManager.insertData(name, lat, lon) > 0) {
            val mapFragment = childFragmentManager.findFragmentById(R.id.map_container) as? SupportMapFragment
            mapFragment?.getMapAsync { googleMap ->
                val location = LatLng(lat, lon)
                val marker = googleMap.addMarker(MarkerOptions().position(location).title(name))
                if (marker != null) {
                    markerList.add(marker)
                }
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            }
            Toast.makeText(requireContext(), "Paikka lisätty: $name", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Paikan lisäys epäonnistui!", Toast.LENGTH_SHORT).show()
        }
    }

    /** Etsi paikka nimellä */
    fun focusMarkerByTitle(title: String) {
        val marker = markerList.find { it.title == title }
        if (marker != null) {
            val mapFragment = childFragmentManager.findFragmentById(R.id.map_container) as? SupportMapFragment
            mapFragment?.getMapAsync { googleMap ->
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 15f))
                marker.showInfoWindow()
            }
        } else {
            Toast.makeText(requireContext(), "Kohdetta ei löydy!", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param paramTitle The marker title.
         * @param paramLat Latitude.
         * @param paramLong Longitude.
         * @return A new instance of fragment BottomFragment.
         */
        @JvmStatic
        fun newInstance(paramTitle: String, paramLat: Double, paramLong: Double) =
            BottomFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM_TITLE, paramTitle)
                    putDouble(ARG_PARAM_LAT, paramLat)
                    putDouble(ARG_PARAM_LONG, paramLong)
                }
            }
    }
}