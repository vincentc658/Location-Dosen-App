package com.app.dosen.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.dosen.R
import com.app.dosen.databinding.ItemDosenBinding
import com.app.dosen.model.DosenModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class DosenAdapter(
    private var list: List<DosenModel>,
    private val onItemClick: (DosenModel) -> Unit
) : RecyclerView.Adapter<DosenAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemDosenBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DosenModel) {
            binding.tvNamaDosen.text = item.nama
            binding.tvProdiDosen.text = item.prodi
            // Handle click
            binding.root.setOnClickListener {
                onItemClick(item)
            }
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
