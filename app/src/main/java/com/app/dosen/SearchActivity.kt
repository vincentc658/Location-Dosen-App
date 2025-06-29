package com.app.dosen

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.dosen.adapter.DosenAdapter
import com.app.dosen.databinding.ActivitySearchBinding
import com.app.dosen.model.DosenModel
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast
import com.app.dosen.util.BaseAppCompat

class SearchActivity : BaseAppCompat() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: DosenAdapter
    private var fullList: List<DosenModel> = emptyList()
    private var prodi: String = "Semua" // Nilai default filter prodi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔽 Ambil parameter "prodi" dari halaman sebelumnya
        val bundle = intent.extras
        prodi = bundle?.getString("prodi") ?: "Semua"

        // 🔽 Tampilkan nama prodi pada UI
        binding.tvProdi.text = "Daftar Dosen"
        binding.toolbar.title = prodi

        setupRecyclerView()
        setupSearchFilter()
        loadDataFromFirestore()

        // 🔽 Setup tombol back
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupRecyclerView() {
        // 🔽 Siapkan adapter dan aksi ketika item dosen diklik
        adapter = DosenAdapter(emptyList()) { dosen ->
            val bundle = Bundle()
            bundle.putParcelable("data", dosen)
            goToPage(RuangKerjaActivity::class.java, bundle)
        }

        binding.recyclerDosen.layoutManager = LinearLayoutManager(this)
        binding.recyclerDosen.adapter = adapter
    }

    private fun setupSearchFilter() {
        // 🔽 Tambahkan listener ketika user mengetik di kolom pencarian
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterData() // jalankan filter setiap kali teks berubah
            }
        })
    }

    private fun loadDataFromFirestore() {
        // 🔽 Ambil semua data dosen dari koleksi "dosen" di Firestore
        FirebaseFirestore.getInstance().collection("dosen")
            .get()
            .addOnSuccessListener { result ->
                fullList = result.mapNotNull { doc ->
                    try {
                        // 🔽 Mapping setiap dokumen menjadi DosenModel
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
                        null // jika ada error parsing data, lewati
                    }
                }

                // 🔽 Setelah data diambil, langsung filter sesuai keyword/prodi
                filterData()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat data dosen", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterData() {
        // 🔽 Ambil kata kunci pencarian
        val query = binding.etSearch.text.toString().trim().lowercase()

        // 🔽 Filter berdasarkan nama dosen dan prodi yang cocok
        val filtered = fullList.filter { dosen ->
            val matchName = dosen.nama.lowercase().contains(query)
            val matchProdi = prodi == "Semua" || dosen.prodi == prodi
            matchName && matchProdi
        }

        // 🔽 Update data yang ditampilkan
        adapter.updateData(filtered)
    }
}
