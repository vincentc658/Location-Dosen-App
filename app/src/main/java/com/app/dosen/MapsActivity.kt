package com.app.dosen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.app.dosen.databinding.ActivityMapsBinding
import com.app.dosen.model.DosenModel
import com.app.dosen.util.BaseView
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*

class MapsActivity : BaseView(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var mMap: GoogleMap
    private var myLatLng: LatLng? = null
    private var lokasiDosen = LatLng(-6.9932, 110.4230)
    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private var travelMode: String = "driving"
    private var currentPolyline: Polyline? = null

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var textToSpeech: TextToSpeech
    private var lastInstructionIndex = 0
    private var instructions: List<String> = listOf()
    private var stepTargets: List<LatLng> = listOf()
    private var maneuvers: List<String> = listOf()

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
        binding.tvDestination.text = "${dosen?.namaGedung}, ${dosen?.lantaiRuangan}"

        textToSpeech = TextToSpeech(this) {
            if (it == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale("id", "ID")
            }
        }

        locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 3000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        binding.llDriving.setOnClickListener {
            if (myLatLng == null) return@setOnClickListener
            travelMode = "driving"
            binding.tvMode.text = travelMode.replaceFirstChar { it.uppercaseChar() }
            highlightSelectedMode(true)
//            getDirections(myLatLng!!, lokasiDosen)
        }

        binding.llWalking.setOnClickListener {
            if (myLatLng == null) return@setOnClickListener
            travelMode = "walking"
            binding.tvMode.text = travelMode.replaceFirstChar { it.uppercaseChar() }
            highlightSelectedMode(false)
//            getDirections(myLatLng!!, lokasiDosen)
        }

        binding.cvCancel.setOnClickListener { finish() }

        binding.cvNavigation.setOnClickListener {
            if (myLatLng != null) {
                showLoading("Mengambil Rute...")
                getDirections(myLatLng!!, lokasiDosen)
            }
        }
    }

    private fun highlightSelectedMode(isDriving: Boolean) {
        val selectedBg = R.drawable.bg_white_border_navy
        val unselectedBg = R.drawable.bg_white_border_grey
        val selectedTextColor = getColor(R.color.navy)
        val unselectedTextColor = getColor(R.color.black)

        binding.llDriving.setBackgroundResource(if (isDriving) selectedBg else unselectedBg)
        binding.llWalking.setBackgroundResource(if (!isDriving) selectedBg else unselectedBg)

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
        showLoading("Mengambil Lokasi anda")
        getMyLocation { hideLoading() }
    }

    private fun getMyLocation(callback: (LatLng) -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        fusedLocationProvider.lastLocation.addOnSuccessListener {
            if (it != null) {
                myLatLng = LatLng(it.latitude, it.longitude)
                val icon = getBitmapDescriptor(this, R.drawable.ic_circle_24)
                mMap.addMarker(MarkerOptions().position(myLatLng!!).title("Lokasi Saya").icon(icon))
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
                "&language=id" +
                "&key=$apiKey"

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                hideLoading()
                showToast(e.message)
                Log.d("getDirections", "--> ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body?.string() ?: "")
                val routes = json.getJSONArray("routes")

                if (routes.length() > 0) {
                    val leg = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0)
                    val durationText = leg.getJSONObject("duration").getString("text")
                    val distanceText = leg.getJSONObject("distance").getString("text")
                    val steps = leg.getJSONArray("steps")

                    val path = mutableListOf<LatLng>()
                    val instrList = mutableListOf<String>()
                    val stepList = mutableListOf<LatLng>()
                    val maneuverList = mutableListOf<String>()

                    for (i in 0 until steps.length()) {
                        val step = steps.getJSONObject(i)
                        val polyline = step.getJSONObject("polyline").getString("points")
                        path.addAll(PolyUtil.decode(polyline))

                        val instruction = step.getString("html_instructions").replace(Regex("<.*?>"), "")
                        instrList.add(instruction)

                        val endLoc = step.getJSONObject("end_location")
                        stepList.add(LatLng(endLoc.getDouble("lat"), endLoc.getDouble("lng")))

                        maneuverList.add(step.optString("maneuver", "straight"))
                    }

                    instructions = instrList
                    stepTargets = stepList
                    maneuvers = maneuverList
                    lastInstructionIndex = 0

                    runOnUiThread {
                        currentPolyline?.remove()
                        currentPolyline = mMap.addPolyline(
                            PolylineOptions().addAll(path).color(Color.BLUE).width(10f)
                        )
                        binding.tvEstTime.text = durationText
                        binding.tvDistance.text = distanceText

                        startRealTimeTracking()
                        hideLoading()
                    }
                }
            }
        })
    }

    private fun startRealTimeTracking() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                myLatLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(myLatLng!!))

                if (lastInstructionIndex < instructions.size && myLatLng != null) {
                    val target = stepTargets[lastInstructionIndex]
                    val distance = distanceBetween(myLatLng!!, target)

                    if (distance < 30) {
                        val text = instructions[lastInstructionIndex]
                        val maneuver = maneuvers[lastInstructionIndex]
                        updateStepInstructionUI(text, maneuver)
                        speakInstruction(text)
                        lastInstructionIndex++
                    }
                }
            }
        }

        fusedLocationProvider.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    private fun updateStepInstructionUI(text: String, maneuver: String) {
        binding.realtimeInstructionLayout.showView()
        binding.tvRealtimeInstruction.text = text

        val iconRes = when (maneuver) {
            "turn-left" -> R.drawable.ic_turn_left
            "turn-right" -> R.drawable.ic_turn_right
            "uturn-left", "uturn-right" -> R.drawable.ic_uturn
            "fork-left" -> R.drawable.ic_turn_left
            "fork-right" -> R.drawable.ic_turn_left
            "straight" -> R.drawable.ic_direction_straight
            else -> R.drawable.ic_direction_straight
        }
        binding.imgRealtimeIcon.setImageResource(iconRes)
    }

    private fun speakInstruction(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun distanceBetween(a: LatLng, b: LatLng): Float {
        val result = FloatArray(1)
        android.location.Location.distanceBetween(
            a.latitude, a.longitude, b.latitude, b.longitude, result
        )
        return result[0]
    }

    fun getBitmapDescriptor(context: Context, @DrawableRes vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)!!
        vectorDrawable.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        if (::locationCallback.isInitialized) {
            fusedLocationProvider.removeLocationUpdates(locationCallback)
        }
    }
}