package com.app.dosen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    private var myMarker: Marker? = null
    private var travelMode: String = "driving"
    private var lokasiDosen = LatLng(-6.9932, 110.4230)
    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var textToSpeech: TextToSpeech
    private var instructions = listOf<String>()
    private var stepTargets = listOf<LatLng>()
    private var maneuvers = listOf<String>()
    private var polylinePoints = listOf<LatLng>()
    private var currentStepIndex = 0
    private var hasSpokenAdvance = false
    private var currentPolyline: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dosen = intent.extras?.getParcelable<DosenModel>("data")
        dosen?.let { lokasiDosen = LatLng(it.lat, it.long) }
        binding.tvDestination.text="${dosen?.namaGedung}, ${dosen?.lantaiRuangan}"
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)
        textToSpeech = TextToSpeech(this) {
            if (it == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale("id", "ID")
            }
        }

        locationRequest = LocationRequest.create().apply {
            interval = 3000
            fastestInterval = 2000
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
            myLatLng?.let {
                showLoading("Mengambil Rute...")
                getDirections(it, lokasiDosen)
            }
        }
    }
    private fun startFakeGPS(){
        val fakeLocations = listOf(
            LatLng(3.521098, 98.795321),
            LatLng(3.521115, 98.795293),
            LatLng(3.521125, 98.795266),
            LatLng(3.521139, 98.795243),
            LatLng(3.521119, 98.795229),
            LatLng(3.521095, 98.795216),
            LatLng(3.521081, 98.795212),
            LatLng(3.521064, 98.795199),
            LatLng(3.521022, 98.795175),
            LatLng(3.520975, 98.795151),
            LatLng(3.520936, 98.795120),
            LatLng(3.520903, 98.795106),
            LatLng(3.520869, 98.795079),
            LatLng(3.520826, 98.795062),
            LatLng(3.520771, 98.795033),
            LatLng(3.519984, 98.794546),
            LatLng(3.519445, 98.794224),
            LatLng(3.519141, 98.794048),
            LatLng(3.518988, 98.793932),
            LatLng(3.518882, 98.793797),
            LatLng(3.518858, 98.793753),
            LatLng(3.518881, 98.793718),
        )

        var fakeIndex = 0
        Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
            override fun run() {
                if (fakeIndex < fakeLocations.size) {
                    val fakeLoc = fakeLocations[fakeIndex++]
                    onFakeLocationUpdate(fakeLoc)
                    Handler(Looper.getMainLooper()).postDelayed(this, 2000)
                }
            }
        }, 2000)
    }
    fun onFakeLocationUpdate(latLng: LatLng) {
        myLatLng = latLng
        if (myMarker == null) {
            val icon = getBitmapDescriptor(this@MapsActivity, R.drawable.ic_circle_24)
            myMarker = mMap.addMarker(
                MarkerOptions().position(myLatLng!!).title("Lokasi Saya").icon(icon)
            )
        } else {
            myMarker?.position = myLatLng!!
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLng(myLatLng!!))

        if (currentStepIndex < stepTargets.size) {
            val distanceToStep = distanceBetween(latLng, stepTargets[currentStepIndex])

            // Advance warning 100m sebelum belok
            if (!hasSpokenAdvance && distanceToStep < 100) {
                speak("Dalam 100 meter, ${instructions[currentStepIndex]}")
                hasSpokenAdvance = true
            }

            // Cek apakah sudah melewati beberapa step sekaligus
            while (currentStepIndex < stepTargets.size &&
                distanceBetween(latLng, stepTargets[currentStepIndex]) < 30) {
                if (currentStepIndex + 1 < instructions.size) {
                    speak(instructions[currentStepIndex+1])
                    updateStepUI(instructions[currentStepIndex+1], maneuvers[currentStepIndex+1])
                }
                currentStepIndex++
                hasSpokenAdvance = false
            }

        } else {
            // Jika semua step sudah dilewati → tujuan tercapai
            speak("Anda telah sampai di tujuan.")
            binding.tvRealtimeInstruction.text = "Tujuan telah dicapai"
            binding.imgRealtimeIcon.setImageResource(R.drawable.ic_check_circle_24)
            binding.realtimeInstructionLayout.showView()
            fusedLocationProvider.removeLocationUpdates(locationCallback)
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
        getMyLocation { mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 16f)) }
    }

    private fun getMyLocation(callback: (LatLng) -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }
        fusedLocationProvider.lastLocation.addOnSuccessListener {
            it?.let {
                myLatLng = LatLng(it.latitude, it.longitude)
                val icon = getBitmapDescriptor(this, R.drawable.ic_circle_24)
                myMarker = mMap.addMarker(MarkerOptions().position(myLatLng!!).icon(icon))
                callback(myLatLng!!)
            }
        }
    }

    private fun getDirections(origin: LatLng, destination: LatLng) {
        val apiKey = "AIzaSyBB7uJxN6nrrQk8Bh8OvaNaexjfyBpZGow"
        val url = "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${destination.latitude},${destination.longitude}&mode=driving&language=id&key=$apiKey"
        OkHttpClient().newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { hideLoading(); showToast(e.message) }
            }
            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body?.string() ?: "")
                val steps = json.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps")
                val instrList = mutableListOf<String>()
                val stepList = mutableListOf<LatLng>()
                val maneuverList = mutableListOf<String>()
                val polyline = mutableListOf<LatLng>()

                for (i in 0 until steps.length()) {
                    val step = steps.getJSONObject(i)
                    instrList.add(step.getString("html_instructions").replace(Regex("<.*?>"), ""))
                    val end = step.getJSONObject("end_location")
                    stepList.add(LatLng(end.getDouble("lat"), end.getDouble("lng")))
                    maneuverList.add(step.optString("maneuver", "straight"))
                    polyline.addAll(PolyUtil.decode(step.getJSONObject("polyline").getString("points")))
                }

                instructions = instrList
                stepTargets = stepList
                maneuvers = maneuverList
                polylinePoints = polyline
                currentStepIndex = 0
                hasSpokenAdvance = false

                runOnUiThread {
                    currentPolyline?.remove()
                    currentPolyline = mMap.addPolyline(PolylineOptions().addAll(polyline).color(Color.BLUE).width(10f))
                    hideLoading()
//                    startFakeGPS()
                    startTracking()
                }
            }
        })
    }

    private fun startTracking() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) return

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                val newLatLng = LatLng(loc.latitude, loc.longitude)
                myLatLng = newLatLng
                if (myMarker == null) {
                    val icon = getBitmapDescriptor(this@MapsActivity, R.drawable.ic_circle_24)
                    myMarker = mMap.addMarker(
                        MarkerOptions().position(myLatLng!!).title("Lokasi Saya").icon(icon)
                    )
                } else {
                    myMarker?.position = myLatLng!!
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLng(myLatLng!!))

                if (currentStepIndex < stepTargets.size) {
                    val distanceToStep = distanceBetween(newLatLng, stepTargets[currentStepIndex])

                    // Advance warning 100m sebelum belok
                    if (!hasSpokenAdvance && distanceToStep < 100) {
                        speak("Dalam 100 meter, ${instructions[currentStepIndex]}")
                        hasSpokenAdvance = true
                    }

                    // Cek apakah sudah melewati beberapa step sekaligus
                    while (currentStepIndex < stepTargets.size &&
                        distanceBetween(newLatLng, stepTargets[currentStepIndex]) < 30) {

                        speak(instructions[currentStepIndex])
                        updateStepUI(instructions[currentStepIndex], maneuvers[currentStepIndex])
                        currentStepIndex++
                        hasSpokenAdvance = false
                    }

                } else {
                    // Jika semua step sudah dilewati → tujuan tercapai
                    speak("Anda telah sampai di tujuan.")
                    binding.tvRealtimeInstruction.text = "Tujuan telah dicapai"
                    binding.imgRealtimeIcon.setImageResource(R.drawable.ic_check_circle_24)
                    binding.realtimeInstructionLayout.showView()
                    fusedLocationProvider.removeLocationUpdates(locationCallback)
                }

            }
        }
        fusedLocationProvider.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun updateStepUI(text: String, maneuver: String) {
        binding.tvRealtimeInstruction.text = text
        binding.realtimeInstructionLayout.showView()
        val icon = when (maneuver) {
            "turn-left" -> R.drawable.ic_turn_left
            "turn-right" -> R.drawable.ic_turn_right
            "uturn-left", "uturn-right" -> R.drawable.ic_uturn
            else -> R.drawable.ic_direction_straight
        }
        binding.imgRealtimeIcon.setImageResource(icon)
    }

    private fun speak(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun distanceBetween(a: LatLng, b: LatLng): Float {
        val result = FloatArray(1)
        android.location.Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, result)
        return result[0]
    }

    fun getBitmapDescriptor(context: Context, @DrawableRes vectorResId: Int): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(context, vectorResId)!!
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::textToSpeech.isInitialized) textToSpeech.shutdown()
        if (::locationCallback.isInitialized) fusedLocationProvider.removeLocationUpdates(locationCallback)
    }
}
