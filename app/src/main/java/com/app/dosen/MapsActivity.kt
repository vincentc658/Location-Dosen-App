package com.app.dosen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.app.dosen.databinding.ActivityMapsBinding
import com.app.dosen.model.DosenModel
import com.app.dosen.util.BaseAppCompat
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

class MapsActivity : BaseAppCompat(), OnMapReadyCallback {
    companion object{
        const val MODE_NONE="0"
        const val MODE_DIRECTION="1"
        const val MODE_NAVIGATION="2"
    }


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
    private var borderPolyline: Polyline? = null
    private lateinit var sensorManager: SensorManager
    private var currentAzimuth: Float = 0f

    // Variabel untuk mengecek apakah sudah sampai tujuan
    private var hasReachedDestination = false
    private var isAlreadyGetDirection = false
    private var mode= MODE_NONE
    private val destinationRadius = 5.0f // 5 meter radius untuk tujuan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi sensor arah (kompas)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Ambil data dosen dari intent, lalu tampilkan tujuan
        val dosen = intent.extras?.getParcelable<DosenModel>("data")
        dosen?.let { lokasiDosen = LatLng(it.lat, it.long) }
        binding.tvDestination.text = "${dosen?.namaGedung}, ${dosen?.lantaiRuangan}"

        // Setup fragment peta Google Maps
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Setup layanan lokasi
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)

        // Inisialisasi Text to Speech dalam bahasa Indonesia
        textToSpeech = TextToSpeech(this) {
            if (it == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale("id", "ID")
            }
        }

        // Konfigurasi permintaan lokasi
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L // Update tiap 2 detik
        ).apply {
            setMinUpdateIntervalMillis(2000L)
        }.build()

        // Klik mode Driving
        binding.llDriving.setOnClickListener {
            if(mode== MODE_NAVIGATION)
            {
                return@setOnClickListener
            }
            if (myLatLng == null) return@setOnClickListener
            travelMode = "driving"
            binding.tvMode.text = travelMode.replaceFirstChar { it.uppercaseChar() }
            highlightSelectedMode(true)
            getDirections(myLatLng!!, lokasiDosen, isAlreadyGetDirection)
        }

        // Klik mode Walking
        binding.llWalking.setOnClickListener {
            if(mode== MODE_NAVIGATION)
            {
                return@setOnClickListener
            }
            if (myLatLng == null) return@setOnClickListener
            travelMode = "walking"
            binding.tvMode.text = travelMode.replaceFirstChar { it.uppercaseChar() }
            highlightSelectedMode(false)
            getDirections(myLatLng!!, lokasiDosen, isAlreadyGetDirection)
        }

        // Klik tombol batal
        binding.cvCancel.setOnClickListener { finish() }

        // Klik tombol mulai navigasi
        binding.cvNavigation.setOnClickListener {
            if (mode == MODE_NONE) {
                myLatLng?.let {
                    showLoading("Mengambil Rute...")
                    getDirections(it, lokasiDosen, true)
                }
                isAlreadyGetDirection= true
                mode= MODE_DIRECTION
            }
            else if (mode == MODE_DIRECTION) {
                startTracking()
                mode= MODE_NAVIGATION
            }else if (mode == MODE_NAVIGATION) {
                stopTracking()
                resetCameraToDefault()
                currentPolyline?.remove()
                borderPolyline?.remove()
                mode= MODE_NONE
            }
            setActionView()

        }
        setActionView()

    }

    // Listener untuk mendapatkan arah kompas dari sensor
    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                currentAzimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun onResume() {
        super.onResume()
        // Aktifkan listener sensor kompas
        sensorManager.registerListener(
            sensorListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SensorManager.SENSOR_DELAY_UI
        )
    }

    override fun onPause() {
        super.onPause()
        // Nonaktifkan listener sensor saat aplikasi dijeda
        sensorManager.unregisterListener(sensorListener)
    }

    // Fungsi untuk mengganti tampilan mode yang dipilih
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

    // Dipanggil saat peta siap digunakan
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.addMarker(MarkerOptions().position(lokasiDosen).title("Lokasi Dosen"))
        showLoading("Mengambil Lokasi Anda")
        getMyLocation {
            hideLoading()
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 16f))
        }
    }

    // Mendapatkan lokasi terkini dari perangkat
    private fun getMyLocation(callback: (LatLng) -> Unit) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        fusedLocationProvider.lastLocation.addOnSuccessListener {
            it?.let {
                myLatLng = LatLng(it.latitude, it.longitude)
                val icon = getBitmapDescriptor(this, R.drawable.ic_circle_24)
                myMarker = mMap.addMarker(MarkerOptions().position(myLatLng!!).icon(icon))
                callback(myLatLng!!)
                highlightSelectedMode(true)
                getDirections(myLatLng!!, lokasiDosen, false)
            }
        }
    }

    // Mengambil rute dan instruksi dari Google Directions API
    private fun getDirections(origin: LatLng, destination: LatLng, isShowRoute: Boolean) {
        val apiKey = "AIzaSyBB7uJxN6nrrQk8Bh8OvaNaexjfyBpZGow"
        val url =
            "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${destination.latitude},${destination.longitude}&mode=${travelMode}&language=id&key=$apiKey"

        OkHttpClient().newCall(Request.Builder().url(url).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    hideLoading()
                    showToast(e.message)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = JSONObject(response.body?.string() ?: "")
                val steps = json.getJSONArray("routes").getJSONObject(0).getJSONArray("legs")
                    .getJSONObject(0).getJSONArray("steps")
                val instrList = mutableListOf<String>()
                val stepList = mutableListOf<LatLng>()
                val maneuverList = mutableListOf<String>()
                val polyline = mutableListOf<LatLng>()
                val routes = json.getJSONArray("routes")

                var durationText = ""
                var distanceText = ""
                if (routes.length() > 0) {
                    val leg = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0)
                    durationText = leg.getJSONObject("duration").getString("text")
                    distanceText = leg.getJSONObject("distance").getString("text")
                }

                // Ambil semua instruksi dan data koordinat
                for (i in 0 until steps.length()) {
                    val step = steps.getJSONObject(i)
                    instrList.add(step.getString("html_instructions").replace(Regex("<.*?>"), ""))
                    val end = step.getJSONObject("end_location")
                    stepList.add(LatLng(end.getDouble("lat"), end.getDouble("lng")))
                    maneuverList.add(step.optString("maneuver", "straight"))
                    polyline.addAll(
                        PolyUtil.decode(
                            step.getJSONObject("polyline").getString("points")
                        )
                    )
                }

                instructions = instrList
                stepTargets = stepList
                maneuvers = maneuverList
                polylinePoints = polyline
                currentStepIndex = 0
                hasSpokenAdvance = false
                hasReachedDestination = false // Reset status tujuan

                runOnUiThread {
                    binding.tvEstTime.text = durationText
                    binding.tvDistance.text = distanceText

                    // Hapus jalur lama
                    currentPolyline?.remove()
                    borderPolyline?.remove()
                    if (isShowRoute) {
                        createModernPolyline(polyline)
                    }
                    hideLoading()
                    setActionView()
                }
            }
        })
    }
    private fun setActionView(){
        if(mode == MODE_NONE){
            binding.ivAction.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_directions_24))
            binding.tvAction.text="Direction"
        }else if(mode == MODE_DIRECTION){
            binding.ivAction.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_navigation))
            binding.tvAction.text="Navigation"
        }else if(mode == MODE_NAVIGATION) {
            binding.ivAction.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_close_24))
            binding.tvAction.text="End Navigation"
        }
    }
    // Tambahkan fungsi untuk menghitung bearing antara dua titik
    private fun calculateBearing(start: LatLng, end: LatLng): Float {
        val lat1 = Math.toRadians(start.latitude)
        val lat2 = Math.toRadians(end.latitude)
        val deltaLon = Math.toRadians(end.longitude - start.longitude)

        val y = Math.sin(deltaLon) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) -
                Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLon)

        var bearing = Math.toDegrees(Math.atan2(y, x))
        // Normalisasi bearing ke 0-360 derajat
        bearing = (bearing + 360) % 360
        return bearing.toFloat()
    }

    // Fungsi untuk mendapatkan titik yang lebih dekat di polyline
    private fun getClosestPointAhead(currentLocation: LatLng): LatLng? {
        if (polylinePoints.isEmpty()) return lokasiDosen

        var closestDistance = Float.MAX_VALUE
        var closestIndex = -1

        // Cari titik terdekat di polyline
        for (i in polylinePoints.indices) {
            val distance = distanceBetween(currentLocation, polylinePoints[i])
            if (distance < closestDistance) {
                closestDistance = distance
                closestIndex = i
            }
        }

        // Ambil beberapa titik ke depan untuk arah yang lebih stabil
        val lookAheadDistance = 20 // meter
        for (i in (closestIndex + 1) until polylinePoints.size) {
            val distance = distanceBetween(currentLocation, polylinePoints[i])
            if (distance >= lookAheadDistance) {
                return polylinePoints[i]
            }
        }

        // Jika tidak ada titik yang cukup jauh, gunakan tujuan akhir
        return lokasiDosen
    }

    // Fungsi untuk mendapatkan titik target berikutnya di jalur
    private fun getNextTargetPoint(currentLocation: LatLng): LatLng? {
        // Jika masih ada step target, gunakan step target berikutnya
        if (currentStepIndex < stepTargets.size) {
            return stepTargets[currentStepIndex]
        }

        // Jika step sudah habis tapi belum sampai tujuan,
        // cari titik terdekat di polyline untuk navigasi yang lebih akurat
        if (!hasReachedDestination && polylinePoints.isNotEmpty()) {
            var closestPoint = lokasiDosen
            var minDistance = distanceBetween(currentLocation, lokasiDosen)

            // Cari titik di polyline yang paling dekat dengan posisi saat ini
            for (point in polylinePoints) {
                val distance = distanceBetween(currentLocation, point)
                if (distance < minDistance) {
                    minDistance = distance
                    closestPoint = point
                }
            }

            // Jika ada titik yang lebih dekat di polyline, gunakan titik setelahnya
            val currentIndex = polylinePoints.indexOf(closestPoint)
            if (currentIndex != -1 && currentIndex < polylinePoints.size - 1) {
                return polylinePoints[currentIndex + 1]
            }

            return lokasiDosen
        }

        return null
    }

    // Update locationCallback untuk panah mengikuti arah hadap user
    private fun startTracking() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) return

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                val newLatLng = LatLng(loc.latitude, loc.longitude)

                // KAMERA: Gunakan kompas untuk orientasi peta (agar berputar sesuai arah hadap user)
                val cameraPosition = CameraPosition.Builder()
                    .target(newLatLng)
                    .zoom(18f)
                    .tilt(60f)
                    .bearing(currentAzimuth) // Kamera ikut kompas (arah hadap user)
                    .build()
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

                // ICON PANAH: Menggunakan currentAzimuth langsung (arah hadap user)
                // Karena kamera juga menggunakan currentAzimuth, panah akan selalu menunjuk ke atas peta
                // yang berarti mengikuti arah hadap user

                // Update marker dengan currentAzimuth
                myLatLng = newLatLng
                if (myMarker == null) {
                    val icon = getBitmapDescriptorWithShadowBackground(
                        this@MapsActivity,
                        R.drawable.ic_arrow,
                    )
                    myMarker = mMap.addMarker(
                        MarkerOptions().position(newLatLng).icon(icon)
                            .anchor(0.5f, 0.5f).flat(true)
                            .rotation(0f) // Set ke 0 karena kamera sudah berputar sesuai currentAzimuth
                    )
                } else {
                    val icon = getBitmapDescriptorWithShadowBackground(
                        this@MapsActivity,
                        R.drawable.ic_arrow,
                    )
                    myMarker?.setIcon(icon)
                    myMarker?.position = newLatLng
                    myMarker?.rotation = 0f // Panah selalu menunjuk ke "atas" peta
                }

                // Sisa kode untuk navigation instructions tetap sama...
                // Cek jarak ke tujuan sebenarnya (koordinat lokasiDosen)
                val distanceToDestination = distanceBetween(newLatLng, lokasiDosen)

                // Jika sudah dalam radius 5 meter dari tujuan
                if (!hasReachedDestination && distanceToDestination <= destinationRadius) {
                    hasReachedDestination = true
                    binding.tvRealtimeInstruction.text = "Tujuan telah dicapai"
                    binding.imgRealtimeIcon.setImageResource(R.drawable.ic_check_circle_24)
                    binding.realtimeInstructionLayout.showView()
                    fusedLocationProvider.removeLocationUpdates(locationCallback)
                    return
                }

                // Jika belum sampai tujuan, lanjutkan dengan instruksi navigasi normal
                if (!hasReachedDestination && currentStepIndex < stepTargets.size) {
                    val distanceToStep = distanceBetween(newLatLng, stepTargets[currentStepIndex])
                    val maneuver = maneuvers[currentStepIndex]
                    val isTurn = maneuver.contains("turn") || maneuver.contains("uturn")

                    // Advance warning: jika 50-100m sebelum belok
                    if (!hasSpokenAdvance && distanceToStep in 50.0..100.0 && isTurn) {
                        updateStepUI(
                            "Dalam ${distanceToStep.toInt()} meter, ${instructions[currentStepIndex]}",
                            maneuver
                        )
                        hasSpokenAdvance = true
                    }

                    // Jalankan langkah saat cukup dekat
                    if (distanceToStep < 30) {
                        updateStepUI(instructions[currentStepIndex], maneuver)
                        currentStepIndex++
                        hasSpokenAdvance = false
                    }
                } else if (!hasReachedDestination && currentStepIndex >= stepTargets.size) {
                    // Jika semua instruksi Google selesai tapi belum sampai tujuan sebenarnya
                    val distanceRemaining = distanceToDestination.toInt()
                    binding.tvRealtimeInstruction.text =
                        "Lanjutkan ke tujuan, ${distanceRemaining}m lagi"
                    binding.imgRealtimeIcon.setImageResource(R.drawable.ic_direction_straight)
                    binding.realtimeInstructionLayout.showView()
                }
            }
        }

        fusedLocationProvider.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }
    // Menampilkan instruksi navigasi saat ini di UI
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

    // Menghitung jarak antara dua titik
    private fun distanceBetween(a: LatLng, b: LatLng): Float {
        val result = FloatArray(1)
        android.location.Location.distanceBetween(
            a.latitude,
            a.longitude,
            b.latitude,
            b.longitude,
            result
        )
        return result[0]
    }

    // Mengubah vector drawable menjadi BitmapDescriptor untuk marker
    fun getBitmapDescriptor(context: Context, @DrawableRes vectorResId: Int): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(context, vectorResId)!!
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    // Bersihkan sumber daya saat activity dihancurkan
    override fun onDestroy() {
        super.onDestroy()
        if (::textToSpeech.isInitialized) textToSpeech.shutdown()
        if (::locationCallback.isInitialized) fusedLocationProvider.removeLocationUpdates(
            locationCallback
        )
    }
    private fun stopTracking() {
        if (::locationCallback.isInitialized) {
            fusedLocationProvider.removeLocationUpdates(locationCallback)
        }

        // Ubah icon marker kembali ke ic_circle_24
        myMarker?.setIcon(getBitmapDescriptor(this, R.drawable.ic_circle_24))

        mode = MODE_NONE
        setActionView()
    }


    private fun resetCameraToDefault() {
        val cameraPosition = CameraPosition.Builder()
            .target(myLatLng!!)
            .zoom(16f)
            .tilt(0f)
            .bearing(currentAzimuth)
            .build()
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        binding.realtimeInstructionLayout.hideView()
    }

    // Fungsi baru untuk membuat icon dengan background circle
    fun getBitmapDescriptorWithShadowBackground(
        context: Context,
        @DrawableRes vectorResId: Int
    ): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(context, vectorResId)!!

        val backgroundSize = (drawable.intrinsicWidth * 1.25).toInt() // Diperkecil dari 1.5 ke 1.25
        val iconSize = (drawable.intrinsicWidth * 0.8).toInt()        // Diperbesar dari 0.7 ke 0.8

        val bitmap = Bitmap.createBitmap(
            backgroundSize,
            backgroundSize,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        val centerX = backgroundSize / 2f
        val centerY = backgroundSize / 2f
        val radius = (backgroundSize / 2f) - 3f

        // Shadow circle (sedikit offset, lebih transparent)
        val shadowPaint = Paint().apply {
            color = Color.parseColor("#20000000") // Hitam transparan (lebih ringan)
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        canvas.drawCircle(centerX + 1.5f, centerY + 1.5f, radius, shadowPaint)

        // Background circle putih semi-transparent
        val backgroundPaint = Paint().apply {
            color = Color.parseColor("#E6FFFFFF") // Putih semi-transparent (90% opacity)
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint)

        // Border circle semi-transparent
        val borderPaint = Paint().apply {
            color = Color.parseColor("#80CCCCCC") // Abu-abu semi-transparent (50% opacity)
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 1.5f // Diperkecil dari 2f ke 1.5f
        }
        canvas.drawCircle(centerX, centerY, radius, borderPaint)

        // Icon di tengah
        val iconLeft = (backgroundSize - iconSize) / 2
        val iconTop = (backgroundSize - iconSize) / 2

        drawable.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
        drawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
    private fun createModernPolyline(polyline: List<LatLng>) {
        // Hapus polyline lama
        currentPolyline?.remove()
        borderPolyline?.remove()

        // Buat polyline border (lebih lebar, warna gelap)
        borderPolyline = mMap.addPolyline(
            PolylineOptions()
                .addAll(polyline)
                .color(Color.parseColor("#CCffffff")) // Hitam semi-transparent
                .width(20f) // Lebih lebar untuk border
                .jointType(JointType.ROUND) // Ujung melengkung
                .endCap(RoundCap()) // Cap bulat
                .startCap(RoundCap())
        )

        // Buat polyline utama (di atas border)
        currentPolyline = mMap.addPolyline(
            PolylineOptions()
                .addAll(polyline)
                .color(ContextCompat.getColor(this@MapsActivity, R.color.blue))
                .width(12f)
                .jointType(JointType.ROUND)
                .endCap(RoundCap())
                .startCap(RoundCap())
        )
    }
}