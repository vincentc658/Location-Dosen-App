package com.app.dosen.fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.dosen.R
import com.app.dosen.adapter.DosenAdapter
import com.app.dosen.databinding.FragmentHomeBinding
import com.app.dosen.databinding.ItemProdiCircleBinding
import com.app.dosen.model.DosenModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var dosenAdapter: DosenAdapter
    private lateinit var menuBindings: List<ItemProdiCircleBinding>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Bind setiap menu
        menuBindings = listOf(
            ItemProdiCircleBinding.bind(binding.root.findViewById(R.id.menu1)),
            ItemProdiCircleBinding.bind(binding.root.findViewById(R.id.menu2)),
            ItemProdiCircleBinding.bind(binding.root.findViewById(R.id.menu3)),
            ItemProdiCircleBinding.bind(binding.root.findViewById(R.id.menu4)),
            ItemProdiCircleBinding.bind(binding.root.findViewById(R.id.menu5)),
            ItemProdiCircleBinding.bind(binding.root.findViewById(R.id.menu6)),
            ItemProdiCircleBinding.bind(binding.root.findViewById(R.id.menu7)),
            ItemProdiCircleBinding.bind(binding.root.findViewById(R.id.menu8)),
        )


        setupMenus()
        setupRecyclerView()
        return binding.root
    }

    private fun setupMenus() {
        val menuTitles = listOf(
            "Makanan", "Minuman", "Resep", "Favorit",
            "Belanja", "Tips", "Profil", "Tentang"
        )

        menuBindings.forEachIndexed { index, item ->
            item.tvLabel.text = menuTitles[index]
            item.imgIcon.setImageResource(getIconFor(index)) // optional
            item.root.setOnClickListener {
                Toast.makeText(requireContext(), "Klik: ${menuTitles[index]}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getIconFor(index: Int): Int {
        // Ganti dengan icon kamu
        return when (index) {
            0 -> R.drawable.ic_launcher_foreground
            1 -> R.drawable.ic_launcher_foreground
            2 -> R.drawable.ic_launcher_foreground
            3 -> R.drawable.ic_launcher_foreground
            4 -> R.drawable.ic_launcher_foreground
            5 -> R.drawable.ic_launcher_foreground
            6 -> R.drawable.ic_launcher_foreground
            7 -> R.drawable.ic_launcher_foreground
            else -> R.drawable.ic_launcher_foreground
        }
    }

    private fun setupRecyclerView() {
        val dataDummy = listOf(
            DosenModel("Dr. Andi", "Sistem Informasi"),
            DosenModel("Prof. Budi", "Teknik Informatika"),
            DosenModel("Mbak Chika", "Ilmu Komputer"),
            DosenModel("Pak Darto", "Teknik Elektro")
        )

        dosenAdapter = DosenAdapter(dataDummy)
        binding.recyclerDosen.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerDosen.adapter = dosenAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
