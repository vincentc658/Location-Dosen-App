package com.app.dosen.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.dosen.R
import com.app.dosen.databinding.ItemDosenPageBinding
import com.app.dosen.model.DosenModel

class DosenPagerAdapter(
    private val context: Context,
    private val pages: List<List<DosenModel>>,
    private val onClick: (DosenModel) -> Unit
) : RecyclerView.Adapter<DosenPagerAdapter.PageViewHolder>() {

    inner class PageViewHolder(val binding: ItemDosenPageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_dosen_page, parent, false)
        val binding = ItemDosenPageBinding.bind(view)
        return PageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val pageData = pages[position]
        val adapter = DosenAdapter(pageData) { dosen -> onClick(dosen) }
        holder.binding.recyclerPageDosen.layoutManager = LinearLayoutManager(context)
        holder.binding.recyclerPageDosen.adapter = adapter
    }

    override fun getItemCount(): Int = pages.size
}
