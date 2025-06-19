package com.app.dosen.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.dosen.DosenDataManager
import com.app.dosen.R
import com.app.dosen.RuangKerjaActivity
import com.app.dosen.SearchActivity
import com.app.dosen.adapter.DosenAdapter
import com.app.dosen.adapter.MenuAdapter
import com.app.dosen.adapter.MenuPagerAdapter
import com.app.dosen.databinding.FragmentHomeBinding
import com.app.dosen.databinding.ItemProdiCircleBinding
import com.app.dosen.model.DosenModel
import com.app.dosen.model.MenuItem
import com.app.dosen.util.BaseFragment

class HomeFragment : BaseFragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var dosenAdapter: DosenAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)



        setupMenuPager()
        setupRecyclerView()
        return binding.root
    }

    private fun setupRecyclerView() {

        val adapter = DosenAdapter(DosenDataManager().generateDosenModels()) { dosen ->
            val bundle = Bundle()
            bundle.putParcelable("data", dosen)
            goToPage(RuangKerjaActivity::class.java, bundle)
        }
        dosenAdapter = adapter
        binding.recyclerDosen.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerDosen.adapter = dosenAdapter
    }
    private fun setupMenuPager() {
        val allMenus = listOf(
            MenuItem(R.drawable.ic_launcher_foreground, "Pendidikan Teknik Bangunan", "PTB"),
            MenuItem(R.drawable.ic_launcher_foreground, "Teknik Sipil", "Teksip"),
            MenuItem(R.drawable.ic_launcher_foreground, "Teknik Arsitektur", "Arsi"),
            MenuItem(R.drawable.ic_launcher_foreground, "Pendidikan Teknik Mesin", "PTM"),
            MenuItem(R.drawable.ic_launcher_foreground, "Pendidikan Teknik Otomotif", "PTO"),
            MenuItem(R.drawable.ic_launcher_foreground, "Teknik Mesin", "TM"),
            MenuItem(R.drawable.ic_launcher_foreground, "Pendidikan Teknik Elektro", "PTE"),
            MenuItem(R.drawable.ic_launcher_foreground, "Pendidikan Teknik Informatika dan Komputer", "PTIK"),
            MenuItem(R.drawable.ic_launcher_foreground, "Teknik Elektro", "TE"),
            MenuItem(R.drawable.ic_launcher_foreground, "Teknik Komputer", "Tekom"),
            MenuItem(R.drawable.ic_launcher_foreground, "Pendidikan Kesejahteraan Keluarga", "PKK"),
            MenuItem(R.drawable.ic_launcher_foreground, "Pendidikan Tata Busana", "Tabus"),
            MenuItem(R.drawable.ic_launcher_foreground, "Pendidikan Tata Boga", "Tabog"),
            MenuItem(R.drawable.ic_launcher_foreground, "Pendidikan Tata Kecantikan", "Takec"),
            MenuItem(R.drawable.ic_launcher_foreground, "Teknik Kimia", "Tekim")
        )

        // Split ke dalam page 8 item per halaman
        val pages = allMenus.chunked(8)

        val pagerAdapter = MenuPagerAdapter(requireContext(), pages) { selectedMenu ->
            val bundle = Bundle()
            bundle.putString("prodi", selectedMenu.label)
            goToPage(SearchActivity::class.java, bundle)
        }

        binding.viewPagerMenu.adapter = pagerAdapter
        binding.dotsIndicator.setViewPager2(binding.viewPagerMenu)
    }

//    private fun setupMenu() {
//        val menuList = listOf(
//            MenuItem(R.drawable.ic_launcher_foreground, "Pendidikan Teknik Bangunan", "PTB"),
//            MenuItem(R.drawable.ic_launcher_foreground, "Teknik Sipil", "Teksip"),
//            MenuItem(R.drawable.ic_launcher_foreground, "Teknik Arsitektur", "Arsi"),
//            MenuItem(R.drawable.ic_launcher_foreground, "Pendidikan Teknik Mesin", "PTM"),
//            MenuItem(R.drawable.ic_launcher_foreground, "Pendidikan Teknik Otomotif", "PTO"),
//            MenuItem(R.drawable.ic_launcher_foreground, "Teknik Mesin", "TM"),
//            MenuItem(R.drawable.ic_launcher_foreground, "Pendidikan Teknik Elektro", "PTE"),
//            MenuItem(
//                R.drawable.ic_launcher_foreground,
//                "Pendidikan Teknik Informatika dan Komputer",
//                "PTIK"
//            ),
//            MenuItem(R.drawable.ic_launcher_foreground, "Teknik Elektro", "TE"),
//            MenuItem(R.drawable.ic_launcher_foreground, "Teknik Komputer", "Tekom"),
//            MenuItem(R.drawable.ic_launcher_foreground, "Pendidikan Kesejahteraan Keluarga", "PKK"),
//            MenuItem(R.drawable.ic_launcher_foreground, "Pendidikan Tata Busana", "Tabus"),
//            MenuItem(R.drawable.ic_launcher_foreground, "Pendidikan Tata Boga", "Tabog"),
//            MenuItem(R.drawable.ic_launcher_foreground, "Pendidikan Tata Kecantika", "Takec"),
//            MenuItem(R.drawable.ic_launcher_foreground, "Teknik Kimia", "Tekim")
//        )
//
//        val adapter = MenuAdapter(menuList) { selectedMenu ->
//            val bundle = Bundle()
//            bundle.putString("prodi", selectedMenu.label)
//            goToPage(SearchActivity::class.java, bundle)
//            Toast.makeText(
//                requireContext(),
//                "Klik: ${selectedMenu.label} (${selectedMenu.inisial})",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//        binding.recyclerMenu.layoutManager = GridLayoutManager(requireContext(), 4)
//        binding.recyclerMenu.adapter = adapter
//
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
