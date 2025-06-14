package com.app.dosen

import android.os.Bundle
import android.os.Handler
import com.app.dosen.databinding.ActivityIntroductionBinding
import com.app.dosen.util.BaseView

class IntroductionActivity : BaseView() {
    private lateinit var binding: ActivityIntroductionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroductionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Handler().postDelayed({
            goToPage(HomeActivity::class.java)
        }, 1500)
    }
}