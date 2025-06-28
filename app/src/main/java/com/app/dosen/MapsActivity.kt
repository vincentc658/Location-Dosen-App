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
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import android.graphics.Color
import android.util.Log
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.app.dosen.databinding.ActivityInformasiRuangKerjaBinding
import com.app.dosen.databinding.ActivityMapsBinding
import com.app.dosen.model.DosenModel
import com.app.dosen.util.BaseView
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Polyline
import okhttp3.Response
import com.google.maps.android.PolyUtil
import com.google.android.gms.maps.model.PolylineOptions

import org.json.JSONObject

import java.io.IOException

class MapsActivity : BaseView(), OnMapReadyCallback {
    private lateinit var binding: ActivityMapsBinding

    private lateinit var mMap: GoogleMap
    private var myLatLng: LatLng? = null
    private var lokasiDosen = LatLng(-6.9932, 110.4230) // contoh koordinat ruangan dosen
    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private var travelMode: String = "driving" // default
    private var currentPolyline: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bundle = intent.extras
        val dosen: DosenModel? = bundle?.getParcelable("data")
        dosen?.let {
            lokasiDosen = LatLng(it.lat, it.long)
        }
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.tvDestination.text = dosen?.namaGedung
        binding.llDriving.setOnClickListener {
            if (myLatLng == null) {
                return@setOnClickListener
            }
            travelMode = "driving"
            binding.tvMode.text = travelMode.replaceFirstChar { it.uppercaseChar() }
            highlightSelectedMode(true)
            getDirections(myLatLng!!, lokasiDosen)
        }

        binding.llWalking.setOnClickListener {
            if (myLatLng == null) {
                return@setOnClickListener
            }
            travelMode = "walking"
            binding.tvMode.text = travelMode.replaceFirstChar { it.uppercaseChar() }
            highlightSelectedMode(false)
            getDirections(myLatLng!!, lokasiDosen)
        }
        binding.cvCancel.setOnClickListener {
            finish()
        }
        binding.cvNavigation.setOnClickListener {
            showLoading("Mengambil Rute...")
            getDirections(myLatLng!!, lokasiDosen)

        }

    }

    private fun highlightSelectedMode(isDriving: Boolean) {
        val selectedBg = R.drawable.bg_white_border_navy
        val unselectedBg = R.drawable.bg_white_border_grey
        val selectedTextColor = getColor(R.color.navy)
        val unselectedTextColor = getColor(R.color.black)

        binding.llDriving.setBackgroundResource(if (isDriving) selectedBg else unselectedBg)
        binding.llWalking.setBackgroundResource(if (!isDriving) selectedBg else unselectedBg)

        // Optional: ubah warna text kalau kamu punya akses TextView langsung
        val drivingText = binding.llDriving.findViewById<TextView>(R.id.drivingText)
        val walkingText = binding.llWalking.findViewById<TextView>(R.id.walkingText)
        drivingText?.setTextColor(if (isDriving) selectedTextColor else unselectedTextColor)
        walkingText?.setTextColor(if (!isDriving) selectedTextColor else unselectedTextColor)
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.addMarker(MarkerOptions().position(lokasiDosen).title("Lokasi Dosen"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokasiDosen, 16f))

        showLoading("Mengambil Lokasi anda")
        getMyLocation { myLocation ->
            hideLoading()
//            getDirections(myLocation, lokasiDosen)
        }
    }

    private fun getMyLocation(callback: (LatLng) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        fusedLocationProvider.lastLocation.addOnSuccessListener {
            if (it != null) {
                myLatLng = LatLng(it.latitude, it.longitude)
                val icon = getBitmapDescriptor(this, R.drawable.ic_circle_24)
                mMap.addMarker(
                    MarkerOptions()
                        .position(myLatLng!!)
                        .title("Lokasi Saya")
                        .icon(icon)
                )
                callback(myLatLng!!)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng!!, 16f))

            }
        }
    }

    private fun getDirections(origin: LatLng, destination: LatLng) {
        val apiKey = "AIzaSyBB7uJxN6nrrQk8Bh8OvaNaexjfyBpZGow"
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.latitude},${origin.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}" +
                "&mode=$travelMode" +
                "&key=$apiKey"

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                hideLoading()
                showToast(e.message)
                Log.d("getDirections ", "--> ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body?.string() ?: "")
                val routes = json.getJSONArray("routes")

                if (routes.length() > 0) {
                    val leg = routes.getJSONObject(0)
                        .getJSONArray("legs")
                        .getJSONObject(0)

                    val durationText = leg.getJSONObject("duration").getString("text")
                    val distanceText = leg.getJSONObject("distance").getString("text")


                    val steps = leg.getJSONArray("steps")
                    val path = mutableListOf<LatLng>()

                    for (i in 0 until steps.length()) {
                        val step = steps.getJSONObject(i)
                        val polyline = step.getJSONObject("polyline").getString("points")
                        path.addAll(PolyUtil.decode(polyline))
                    }

                    runOnUiThread {
                        // Hapus polyline sebelumnya jika ada
                        currentPolyline?.remove()

                        // Tambahkan polyline baru dan simpan referensinya
                        currentPolyline = mMap.addPolyline(
                            PolylineOptions()
                                .addAll(path)
                                .color(Color.BLUE)
                                .width(10f)
                        )

                        // Tampilkan estimasi waktu dan jarak
                        binding.tvEstTime.text = durationText
                        binding.tvDistance.text = distanceText

                        hideLoading()
                    }

                }
            }
        })
    }
    fun getBitmapDescriptor(context: Context, @DrawableRes vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)!!
        vectorDrawable.setBounds(
            0, 0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

}
