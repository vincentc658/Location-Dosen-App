package com.app.dosen

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.app.dosen.databinding.ActivityHomeBinding
import com.app.dosen.fragment.HomeFragment
import com.app.dosen.util.BaseView

class HomeActivity : BaseView() {
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadFragment(HomeFragment())
        // Handle navigation selection
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
//                R.id.nav_about_us -> loadFragment(ListFoodFragment())
//                R.id.nav_search -> {
//                    binding.ivRedDot.hideView()
//                }
            }

            true
        }
    }
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}