package com.app.dosen

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.Manifest
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import android.graphics.Color
import android.util.Log
import okhttp3.Response
import com.google.maps.android.PolyUtil
import com.google.android.gms.maps.model.PolylineOptions

import org.json.JSONObject

import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val lokasiDosen = LatLng(-6.9932, 110.4230) // contoh koordinat ruangan dosen
    private lateinit var fusedLocationProvider: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.addMarker(MarkerOptions().position(lokasiDosen).title("Lokasi Dosen"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokasiDosen, 16f))

        // Arahkan dari lokasi pengguna ke lokasi dosen
        getMyLocation { myLocation ->
            getDirections(myLocation, lokasiDosen)
        }
    }

    private fun getMyLocation(callback: (LatLng) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        fusedLocationProvider.lastLocation.addOnSuccessListener {
            if (it != null) {
                val myLatLng = LatLng(it.latitude, it.longitude)
                mMap.addMarker(MarkerOptions().position(myLatLng).title("Lokasi Saya"))
                callback(myLatLng)
            }
        }
    }

    private fun getDirections(origin: LatLng, destination: LatLng) {
        val apiKey = "AIzaSyBB7uJxN6nrrQk8Bh8OvaNaexjfyBpZGow"
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.latitude},${origin.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}" +
                "&key=$apiKey"

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("getDirections ","--> ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body?.string() ?: "")
                val routes = json.getJSONArray("routes")

                if (routes.length() > 0) {
                    val steps = routes.getJSONObject(0)
                        .getJSONArray("legs")
                        .getJSONObject(0)
                        .getJSONArray("steps")

                    val path = mutableListOf<LatLng>()

                    for (i in 0 until steps.length()) {
                        val step = steps.getJSONObject(i)
                        val polyline = step.getJSONObject("polyline").getString("points")
                        path.addAll(PolyUtil.decode(polyline))
                    }

                    runOnUiThread {
                        mMap.addPolyline(
                            PolylineOptions()
                                .addAll(path)
                                .color(Color.BLUE)
                                .width(10f)
                        )
                    }
                }
            }
        })
    }
}
