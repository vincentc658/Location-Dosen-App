package com.app.dosen

import android.os.Bundle
import android.os.Handler
import com.app.dosen.databinding.ActivityIntroductionBinding
import com.app.dosen.util.BaseAppCompat

class IntroductionActivity : BaseAppCompat() {

    // View binding untuk layout activity_introduction.xml
    private lateinit var binding: ActivityIntroductionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi view binding dan set tampilan activity
        binding = ActivityIntroductionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Handler dengan delay 3 detik (3000ms)
        // Setelah itu pindah ke HomeActivity dan tutup IntroductionActivity
        Handler().postDelayed({
            goToPage(HomeActivity::class.java) // Fungsi dari BaseAppCompat
            finish() // Selesai dan hapus activity dari stack
        }, 3000)
    }
}
