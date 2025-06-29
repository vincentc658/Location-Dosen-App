package com.app.dosen.fragment

import android.R
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.dosen.RuangKerjaActivity
import com.app.dosen.adapter.DosenAdapter
import com.app.dosen.databinding.FragmentSearchBinding
import com.app.dosen.model.DosenModel
import com.app.dosen.util.BaseFragment
import com.google.firebase.firestore.FirebaseFirestore
class SearchFragment : BaseFragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    // Adapter untuk RecyclerView dosen
    private lateinit var adapter: DosenAdapter

    // Data penuh dari Firestore sebelum difilter
    private var fullList: List<DosenModel> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinner()        // Atur isi spinner Prodi
        setupRecyclerView()   // Atur daftar RecyclerView dosen
        setupSearchFilter()   // Atur listener pencarian & spinner
        loadDataFromFirestore() // Ambil data dari Firestore
    }

    // Isi spinner program studi
    private fun setupSpinner() {
        val prodiList = listOf(
            "Semua",
            "Pendidikan Teknik Bangunan",
            "Teknik Sipil",
            "Teknik Arsitektur",
            "Pendidikan Teknik Mesin",
            "Pendidikan Teknik Otomotif",
            "Teknik Mesin",
            "Pendidikan Teknik Elektro",
            "Pendidikan Teknik Informatika dan Komputer",
            "Teknik Elektro",
            "Teknik Komputer",
            "Pendidikan Kesejahteraan Keluarga",
            "Pendidikan Tata Busana",
            "Pendidikan Tata Boga",
            "Pendidikan Tata Kecantikan",
            "Teknik Kimia"
        )
        // Gunakan adapter standar bawaan Android
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, prodiList)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerProdi.adapter = spinnerAdapter
    }

    // Inisialisasi RecyclerView untuk daftar dosen
    private fun setupRecyclerView() {
        adapter = DosenAdapter(emptyList()) { dosen ->
            // Jika item diklik, kirim data ke RuangKerjaActivity
            val bundle = Bundle()
            bundle.putParcelable("data", dosen)
            goToPage(RuangKerjaActivity::class.java, bundle)
        }
        binding.recyclerDosen.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerDosen.adapter = adapter
    }

    // Ambil semua data dosen dari Firestore dan simpan di fullList
    private fun loadDataFromFirestore() {
        FirebaseFirestore.getInstance().collection("dosen")
            .get()
            .addOnSuccessListener { result ->
                fullList = result.mapNotNull { doc ->
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

                // Pastikan fragment masih hidup sebelum update UI
                if (isAdded && _binding != null) {
                    filterData()
                }
            }
    }

    // Atur filter berdasarkan pencarian nama dan spinner prodi
    private fun setupSearchFilter() {
        // Listener untuk input teks (nama dosen)
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterData()
            }
        })

        // Listener untuk perubahan pilihan spinner
        binding.spinnerProdi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View, position: Int, id: Long
            ) {
                filterData()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // Lakukan filter data berdasarkan nama dan prodi yang dipilih
    private fun filterData() {
        val query = binding.etSearch.text.toString().trim().lowercase()
        val selectedProdi = binding.spinnerProdi.selectedItem.toString()

        val filtered = fullList.filter { dosen ->
            val matchName = dosen.nama.lowercase().contains(query)
            val matchProdi = selectedProdi == "Semua" || dosen.prodi == selectedProdi
            matchName && matchProdi
        }

        adapter.updateData(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

