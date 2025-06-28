package com.app.dosen

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.dosen.adapter.DosenAdapter
import com.app.dosen.databinding.ActivitySearchBinding
import com.app.dosen.model.DosenModel
import com.app.dosen.util.BaseView
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast

class SearchActivity : BaseView() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: DosenAdapter
    private var fullList: List<DosenModel> = emptyList()
    private var prodi: String = "Semua"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        prodi = bundle?.getString("prodi") ?: "Semua"
        binding.tvProdi.text = "Daftar Dosen"
        binding.toolbar.title = prodi

        setupRecyclerView()
        setupSearchFilter()
        loadDataFromFirestore()
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupRecyclerView() {
        adapter = DosenAdapter(emptyList()) { dosen ->
            val bundle = Bundle()
            bundle.putParcelable("data", dosen)
            goToPage(RuangKerjaActivity::class.java, bundle)
        }

        binding.recyclerDosen.layoutManager = LinearLayoutManager(this)
        binding.recyclerDosen.adapter = adapter
    }

    private fun setupSearchFilter() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterData()
            }
        })
    }

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

                filterData()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat data dosen", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterData() {
        val query = binding.etSearch.text.toString().trim().lowercase()

        val filtered = fullList.filter { dosen ->
            val matchName = dosen.nama.lowercase().contains(query)
            val matchProdi = prodi == "Semua" || dosen.prodi == prodi
            matchName && matchProdi
        }

        adapter.updateData(filtered)
    }
}
