package com.app.dosen

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.dosen.adapter.DosenAdapter
import com.app.dosen.databinding.ActivitySearchBinding
import com.app.dosen.model.DosenModel
import com.app.dosen.util.BaseView

class SearchActivity : BaseView() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: DosenAdapter
    private lateinit var fullList: List<DosenModel>
    private var prodi="Semua"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bundle = intent.extras
        prodi= bundle?.getString("prodi")?:"Semua"
        binding.tvProdi.text="Prodi : $prodi"
        setupRecyclerView()
        setupSearchFilter()
    }


    private fun setupRecyclerView() {
        fullList = DosenDataManager().generateDosenModels()

        adapter = DosenAdapter(fullList) { dosen ->
            val bundle = Bundle()
            bundle.putParcelable("data", dosen)
            goToPage(RuangKerjaActivity::class.java, bundle)
        }

        binding.recyclerDosen.layoutManager = LinearLayoutManager(this)
        binding.recyclerDosen.adapter = adapter
        filterData()
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
