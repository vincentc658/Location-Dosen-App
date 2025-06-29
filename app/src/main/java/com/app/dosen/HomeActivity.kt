package com.app.dosen

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.app.dosen.databinding.ActivityHomeBinding
import com.app.dosen.fragment.AboutUsFragment
import com.app.dosen.fragment.HomeFragment
import com.app.dosen.fragment.SearchFragment
import com.app.dosen.util.BaseAppCompat

class HomeActivity : BaseAppCompat() {
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
                R.id.nav_search -> loadFragment(SearchFragment())
                R.id.nav_about_us -> loadFragment(AboutUsFragment())
            }

            true
        }
//        DosenDataManager().uploadToFirestore()
    }
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}