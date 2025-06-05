package com.example.mapsapp

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import kotlin.getValue

private const val ARG_PARAM_LAT = "paramLat"
private const val ARG_PARAM_LONG = "paramLong"
private const val ARG_PARAM_TITLE = "paramTitle"

/**
 * A simple [Fragment] subclass.
 * Use the [TopFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TopFragment : Fragment() {

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var paramLat: Double? = null
    private var paramLong: Double? = null
    private var paramTitle: String? = null

    private var listener: OnAddLocationListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnAddLocationListener
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_top, container, false)
        val addButton = view.findViewById<Button>(R.id.buttonSave)
        val searchButton = view.findViewById<Button>(R.id.buttonSearch)
        val searchView = view.findViewById<androidx.appcompat.widget.SearchView>(R.id.search_view_top)
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)

        val nameEdit = view.findViewById<EditText>(R.id.editTextPlaceName)
        val latEdit = view.findViewById<EditText>(R.id.editTextLatitude)
        val lonEdit = view.findViewById<EditText>(R.id.editTextLongitude)

        addButton.setOnClickListener {
            val name = nameEdit.text.toString()
            val lat = latEdit.text.toString().toDoubleOrNull() ?: 0.0
            val lon = lonEdit.text.toString().toDoubleOrNull() ?: 0.0
            listener?.onAddLocation(name, lat, lon)
        }

        searchButton.setOnClickListener {
            val searchQuery = searchEditText.text.toString()
            if (searchQuery.isNotEmpty()) {
                (activity as? MainActivity)?.focusMarkerByTitle(searchQuery)
            } else {
                Toast.makeText(context, "Haku ei voi olla tyhjä.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val nameEdit = view.findViewById<EditText>(R.id.editTextPlaceName)
        nameEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                sharedViewModel.setTitle(s?.toString() ?: "")
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        val latEdit = view.findViewById<EditText>(R.id.editTextLatitude)
        val lonEdit = view.findViewById<EditText>(R.id.editTextLongitude)

        sharedViewModel.title.observe(viewLifecycleOwner) { title ->
            if (nameEdit.text.toString() != title) {
                nameEdit.setText(title)
            }
        }

        sharedViewModel.location.observe(viewLifecycleOwner) { (lat, lon) ->
            latEdit.setText(lat.toString())
            lonEdit.setText(lon.toString())
        }
    }

    interface OnAddLocationListener {
        fun onAddLocation(name: String, lat: Double, lon: Double)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param paramTitle The marker title.
         * @param paramLat Latitude.
         * @param paramLong Longitude.
         * @return A new instance of fragment TopFragment.
         */
        @JvmStatic
        fun newInstance(paramTitle: String, paramLat: Double, paramLong: Double) =
            TopFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM_TITLE, paramTitle)
                    putDouble(ARG_PARAM_LAT, paramLat)
                    putDouble(ARG_PARAM_LONG, paramLong)
                }
            }
    }
}