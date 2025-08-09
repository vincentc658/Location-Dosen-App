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
import com.app.dosen.adapter.DosenPagerAdapter
import com.app.dosen.adapter.MenuAdapter
import com.app.dosen.adapter.MenuPagerAdapter
import com.app.dosen.databinding.FragmentHomeBinding
import com.app.dosen.databinding.ItemProdiCircleBinding
import com.app.dosen.model.DosenModel
import com.app.dosen.model.MenuItem
import com.app.dosen.util.BaseAppCompat
import com.app.dosen.util.BaseFragment
import com.google.firebase.firestore.FirebaseFirestore
class HomeFragment : BaseFragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var dosenAdapter: DosenAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Inisialisasi tampilan menu (grid prodi dengan paging)
        setupMenuPager()
        getDataDosen()

        return binding.root
    }


    private fun setupMenuPager() {
        // Daftar semua menu program studi
        val allMenus = listOf(
            MenuItem(R.drawable.ic_ptb, "Pendidikan Teknik Bangunan", "PTB"),
            MenuItem(R.drawable.ic_teksip, "Teknik Sipil", "Teksip"),
            MenuItem(R.drawable.ic_tekars, "Teknik Arsitektur", "Arsi"),
            MenuItem(R.drawable.ic_ptm, "Pendidikan Teknik Mesin", "PTM"),
            MenuItem(R.drawable.ic_pto, "Pendidikan Teknik Otomotif", "PTO"),
            MenuItem(R.drawable.ic_tm, "Teknik Mesin", "TM"),
            MenuItem(R.drawable.ic_pte, "Pendidikan Teknik Elektro", "PTE"),
            MenuItem(R.drawable.ic_ptik, "Pendidikan Teknik Informatika dan Komputer", "PTIK"),
            MenuItem(R.drawable.ic_te, "Teknik Elektro", "TE"),
            MenuItem(R.drawable.ic_tekom, "Teknik Komputer", "Tekom"),
            MenuItem(R.drawable.ic_pkk, "Pendidikan Kesejahteraan Keluarga", "PKK"),
            MenuItem(R.drawable.ic_tabus, "Pendidikan Tata Busana", "Tabus"),
            MenuItem(R.drawable.ic_tabog, "Pendidikan Tata Boga", "Tabog"),
            MenuItem(R.drawable.ic_takec, "Pendidikan Tata Kecantikan", "Takec"),
            MenuItem(R.drawable.ic_tekim, "Teknik Kimia", "Tekim")
        )

        // Bagi menu menjadi beberapa halaman, 8 item per halaman
        val pages = allMenus.chunked(8)

        // Buat adapter ViewPager (satu halaman = satu grid menu)
        val pagerAdapter = MenuPagerAdapter(requireContext(), pages) { selectedMenu ->
            // Saat menu dipilih, buka SearchActivity dan kirim label prodi
            val bundle = Bundle()
            bundle.putString("prodi", selectedMenu.label)
            goToPage(SearchActivity::class.java, bundle)
        }

        // Pasang adapter ke ViewPager dan hubungkan dengan dotsIndicator
        binding.viewPagerMenu.adapter = pagerAdapter
        binding.dotsIndicator.setViewPager2(binding.viewPagerMenu)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Hindari memory leak
    }
    private fun setupDosenPager(dosenList: List<DosenModel>) {
        // Bagi data menjadi 10 item per halaman
        val pages = dosenList.chunked(10)

        val pagerAdapter = DosenPagerAdapter(requireContext(), pages) { dosen ->
            val bundle = Bundle().apply { putParcelable("data", dosen) }
            goToPage(RuangKerjaActivity::class.java, bundle)
        }

        binding.viewPagerDosen.adapter = pagerAdapter
        binding.dotsIndicatorDosen.setViewPager2(binding.viewPagerDosen)
    }
    private fun getDataDosen(){
        FirebaseFirestore.getInstance()
            .collection("dosen")
            .get()
            .addOnSuccessListener { result ->
                val dosenList = result.mapNotNull { doc ->
                    try {
                        DosenModel(
                            nama = doc.getString("nama") ?: "",
                            prodi = doc.getString("prodi") ?: "",
                            namaGedung = doc.getString("namaGedung") ?: "",
                            kodeRuangan = doc.getString("kodeRuangan") ?: "",
                            lantaiRuangan = doc.getString("lantaiRuangan") ?: "",
                            lat = doc.getDouble("lat") ?: 0.0,
                            long = doc.getDouble("long") ?: 0.0,
                            fotoDosen = doc.getString("fotoDosen") ?: "",
                            deskripsiRuangan = doc.getString("deskripsiRuangan") ?: "",
                            fotoRuangan = doc.getString("fotoRuangan") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                setupDosenPager(dosenList)
                (requireActivity() as BaseAppCompat).hideLoading()
            }

    }

}

