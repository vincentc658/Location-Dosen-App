package com.app.dosen.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.dosen.databinding.ItemDosenBinding
import com.app.dosen.model.DosenModel

class DosenAdapter(private var list: List<DosenModel>) :
    RecyclerView.Adapter<DosenAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemDosenBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DosenModel) {
            binding.tvNamaDosen.text = item.nama
            binding.tvProdiDosen.text = item.prodi
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDosenBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size
    fun updateData(newList: List<DosenModel>) {
        list = newList
        notifyDataSetChanged()
    }
}
