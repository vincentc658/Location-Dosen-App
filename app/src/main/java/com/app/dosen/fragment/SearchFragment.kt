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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.dosen.DosenDataManager
import com.app.dosen.RuangKerjaActivity
import com.app.dosen.adapter.DosenAdapter
import com.app.dosen.databinding.FragmentSearchBinding
import com.app.dosen.model.DosenModel
import com.app.dosen.util.BaseFragment

class SearchFragment : BaseFragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: DosenAdapter
    private lateinit var fullList: List<DosenModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val prodiList = listOf("Semua",
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
            "Teknik Kimia")
        val spinnerAdapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, prodiList)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerProdi.adapter = spinnerAdapter
        fullList = DosenDataManager().generateDosenModels()
        adapter = DosenAdapter(fullList) { dosen ->
            val bundle = Bundle()
            bundle.putParcelable("data", dosen)
            goToPage(RuangKerjaActivity::class.java, bundle)
        }
        binding.recyclerDosen.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerDosen.adapter = adapter

        setupSearchFilter()
    }

    private fun setupSearchFilter() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                filterData()
            }
        })

        binding.spinnerProdi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: View, position: Int, id: Long
            ) {
                filterData()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

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
