package com.example.mindguide

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.mindguide.databinding.ActivityMapsBinding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private var info = false
    private lateinit var sharedPreferences: SharedPreferences
    private var selectedLatitude = 0.0
    private var selectedLongitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerLauncher()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        sharedPreferences = getSharedPreferences("com.example.mindguide", MODE_PRIVATE)
        info = false

        binding.saveButton.setOnClickListener {
            val intent = Intent(this@MapsActivity, AddPlaceActivity::class.java)
            intent.putExtra("latitude", selectedLatitude.toString())
            intent.putExtra("longitude", selectedLongitude.toString())

            finish()

            startActivity(intent)
        }

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)
        binding.saveButton.isEnabled = false
        val intent = intent
        when (intent.getStringExtra("oldornew")) {
            "new" -> {
                selectedLatitude = intent.getStringExtra("latitude")?.toDouble() ?: 0.0
                selectedLongitude = intent.getStringExtra("longitude")?.toDouble() ?: 0.0
                binding.saveButton.visibility = View.GONE
                mMap.clear()

                val latLng = LatLng(selectedLatitude, selectedLongitude)
                mMap.addMarker(MarkerOptions().position(latLng))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            }

            else -> {
                Snackbar.make(binding.root, R.string.long_tap_on_the, 4000)
                    .show()
                binding.saveButton.visibility = View.VISIBLE

                locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

                locationListener = LocationListener { location ->

                    info = sharedPreferences.getBoolean("info", false)

                    if (!info) {
                        val userLocation = LatLng(location.latitude, location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                        sharedPreferences.edit().putBoolean("info", true).apply()
                    }
                }

                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) {
                        Snackbar.make(
                            binding.root,
                            R.string.permission_is_required_for_maps,
                            Snackbar.LENGTH_INDEFINITE
                        )
                            .setAction(R.string.grant_permission) {
                                permissionLauncher.launch(
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                )
                            }
                            .show()
                    } else {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                } else {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        0,
                        0f,
                        locationListener
                    )

                    val lastLocation =
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (lastLocation != null) {
                        val lastUserLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15f))
                    }

                    mMap.isMyLocationEnabled = true
                }
            }
        }
    }

    override fun onMapLongClick(latLng: LatLng) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(latLng))

        selectedLatitude = latLng.latitude
        selectedLongitude = latLng.longitude

        binding.saveButton.isEnabled = true
    }

    private fun registerLauncher() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->

                if (result) {

                    if (ContextCompat.checkSelfPermission(
                            this@MapsActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0,
                            0f,
                            locationListener
                        )

                        val lastLocation =
                            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        if (lastLocation != null) {
                            val lastUserLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15f))
                        }
                    }

                } else {
                    Toast.makeText(this@MapsActivity, R.string.permission_is_required, Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onBackPressed() {

        when (intent.getStringExtra("oldornew")) {
            "new" -> {
                super.onBackPressed()

            }

            else -> {

                Snackbar.make(
                    binding.root,
                    R.string.are_you_sure_you_want_to_leave,
                    Snackbar.LENGTH_LONG
                )
                    .setAction(R.string.yes) {
                        super.onBackPressed()
                        val intent = Intent(this, AddPlaceActivity::class.java)
                        finish()
                        startActivity(intent)
                    }
                    .show()
            }


        }

    }

}
