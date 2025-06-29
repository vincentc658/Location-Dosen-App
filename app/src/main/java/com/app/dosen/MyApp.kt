package com.app.dosen

import android.app.Application
import com.google.firebase.FirebaseApp

// Kelas custom Application untuk inisialisasi global aplikasi
class MyApp : Application() {

    // Fungsi ini otomatis dipanggil saat aplikasi pertama kali diluncurkan
    override fun onCreate() {
        super.onCreate()

        // 🔽 Inisialisasi FirebaseApp
        // Fungsi ini wajib dipanggil agar semua layanan Firebase seperti Firestore, Auth, dan lainnya dapat digunakan di seluruh aplikasi
        FirebaseApp.initializeApp(this)

        // Setelah baris ini dijalankan, kamu bisa menggunakan FirebaseFirestore.getInstance() di mana pun dalam aplikasi
        /*
        * Untuk menggunakan Firestore, pastikan kamu sudah melakukan hal berikut:

        1. Tambahkan Dependency di build.gradle (Module)
        implementation platform('com.google.firebase:firebase-bom:32.8.1') // Versi bisa berubah
        implementation 'com.google.firebase:firebase-firestore-ktx'

        2. Tambahkan di build.gradle (Project-level)
        classpath 'com.google.gms:google-services:4.4.1' // Plugin Google Services

        3. Apply Plugin di build.gradle (Module)
        plugins {
            id 'com.android.application'
            id 'com.google.gms.google-services' // Wajib untuk Firebase
        }

        4. Tambahkan google-services.json
        Dapatkan dari Firebase Console → Project Settings → Tambah aplikasi Android.
        Letakkan file tersebut di: app/google-services.json
*/
    }
}
